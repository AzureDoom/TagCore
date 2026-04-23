package com.azuredoom.tagcore;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.npc.NPCPlugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.JarURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.azuredoom.tagcore.data.*;

/**
 * Responsible for discovering, loading, and registering all tag definitions into a {@link TagRegistry} at server
 * startup.
 * <p>
 * Tags are loaded from two sources, in order:
 * <ol>
 * <li><b>Classpath resources</b> — any {@code tags/} directory found on the plugin's classloader (from both file-system
 * directories and JARs).</li>
 * <li><b>External asset packs</b> — {@code .zip} or {@code .jar} files placed in the {@code mods/} directory adjacent
 * to the server, processed in alphabetical order so that pack authors can control override priority by filename.</li>
 * </ol>
 * <p>
 * Tags from external packs may override built-in classpath tags of the same ID. Duplicate built-in tags (same ID
 * appearing in multiple classpath sources) are skipped with a warning rather than replaced.
 * <p>
 * After all definitions are collected, their {@code #reference} values are recursively resolved via
 * {@link TagRegistry#resolve(String)} to detect circular references or missing tag links early.
 */
public final class TagBootstrap {

    private static final Gson GSON = new Gson();

    private final JavaPlugin plugin;

    public TagBootstrap(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Executes the full bootstrap sequence and returns a fully populated {@link TagRegistry}.
     * <p>
     * This method loads tags from all configured sources, registers them, and then eagerly resolves all tag value
     * references to surface any configuration errors at startup rather than at runtime.
     *
     * @return a new, fully initialized {@link TagRegistry} containing all discovered tags
     * @throws RuntimeException if any tag file cannot be parsed or if reference resolution fails
     */
    public TagRegistry bootstrap() {
        var registry = new TagRegistry(
            collectValidItemIds(),
            collectValidBlockIds(),
            collectValidEntityIds(),
            collectValidBiomeIds(),
            collectValidEffectIds(),
            collectValidFluidIds(),
            collectValidDamageTypeIds()
        );
        loadAllTags(registry);
        return registry;
    }

    /**
     * Collects all valid item identifiers from the loaded item asset registry.
     * <p>
     * Only non-null, non-blank IDs are included. The resulting set preserves the iteration order of the underlying
     * asset map.
     *
     * @return a set of valid item identifiers; never {@code null}
     */
    private Set<String> collectValidItemIds() {
        Set<String> ids = new LinkedHashSet<>();

        for (var item : Item.getAssetMap().getAssetMap().values()) {
            if (item == null) {
                continue;
            }

            var id = item.getId();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }

        TagCoreMod.infoLog("Collected " + ids.size() + " valid item ids.");
        return ids;
    }

    /**
     * Collects all valid block identifiers from the loaded block asset registry.
     * <p>
     * Only non-null, non-blank IDs are included. The resulting set preserves the iteration order of the underlying
     * asset map.
     *
     * @return a set of valid block identifiers; never {@code null}
     */
    private Set<String> collectValidBlockIds() {
        Set<String> ids = new LinkedHashSet<>();

        for (var block : BlockType.getAssetMap().getAssetMap().values()) {
            if (block == null) {
                continue;
            }

            var id = block.getId();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }

        TagCoreMod.infoLog("Collected " + ids.size() + " valid block ids.");
        return ids;
    }

    /**
     * Collects all valid entity identifiers from the NPC plugin.
     * <p>
     * If the NPC plugin is unavailable or an error occurs during retrieval, an empty set is returned and a warning is
     * logged.
     *
     * @return a set of valid entity identifiers; never {@code null}
     */
    private Set<String> collectValidEntityIds() {
        Set<String> ids = new LinkedHashSet<>();

        try {
            var npcPlugin = NPCPlugin.get();
            if (npcPlugin == null) {
                TagCoreMod.warnLog("NPCPlugin was null while collecting valid entity ids.");
                return ids;
            }

            for (var id : npcPlugin.getRoleTemplateNames(false)) {
                if (id != null && !id.isBlank()) {
                    ids.add(id);
                }
            }
        } catch (Exception e) {
            TagCoreMod.warnLog("Failed to collect valid entity ids: " + e.getMessage());
        }

        TagCoreMod.infoLog("Collected " + ids.size() + " valid entity ids.");
        return ids;
    }

    /**
     * Collects all valid biome identifiers from the loaded biome asset registry.
     * <p>
     * Only non-null, non-blank IDs are included. The resulting set preserves the iteration order of the underlying
     * asset map.
     *
     * @return a set of valid biome identifiers; never {@code null}
     */
    private Set<String> collectValidBiomeIds() {
        Set<String> ids = new LinkedHashSet<>();

        for (var biome : BiomeAsset.getAssetStore().getAssetMap().getAssetMap().values()) {
            if (biome == null) {
                continue;
            }

            var id = biome.getId();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }
        TagCoreMod.infoLog("Collected " + ids.size() + " valid biome ids.");
        return ids;
    }

    /**
     * Collects all valid effect identifiers from the loaded entity effect registry.
     * <p>
     * Only non-null, non-blank IDs are included. The resulting set preserves the iteration order of the underlying
     * asset map.
     *
     * @return a set of valid effect identifiers; never {@code null}
     */
    private Set<String> collectValidEffectIds() {
        Set<String> ids = new LinkedHashSet<>();

        for (var effect : EntityEffect.getAssetMap().getAssetMap().values()) {
            if (effect == null) {
                continue;
            }

            var id = effect.getId();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }

        TagCoreMod.infoLog("Collected " + ids.size() + " valid effect ids.");
        return ids;
    }

    /**
     * Collects all valid fluid identifiers from the loaded fluid asset registry.
     * <p>
     * Only non-null, non-blank IDs are included. The resulting set preserves the iteration order of the underlying
     * asset map.
     *
     * @return a set of valid fluid identifiers; never {@code null}
     */
    private Set<String> collectValidFluidIds() {
        Set<String> ids = new LinkedHashSet<>();

        for (var fluid : Fluid.getAssetMap().getAssetMap().values()) {
            if (fluid == null) {
                continue;
            }

            var id = fluid.getId();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }

        TagCoreMod.infoLog("Collected " + ids.size() + " valid fluid ids.");
        return ids;
    }

    /**
     * Collects all valid damage type identifiers from the loaded damage cause registry.
     * <p>
     * Only non-null, non-blank IDs are included. The resulting set preserves the iteration order of the underlying
     * asset map.
     *
     * @return a set of valid damage type identifiers; never {@code null}
     */
    private Set<String> collectValidDamageTypeIds() {
        Set<String> ids = new LinkedHashSet<>();

        for (var damageType : DamageCause.getAssetMap().getAssetMap().values()) {
            if (damageType == null) {
                continue;
            }

            var id = damageType.getId();
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }

        TagCoreMod.infoLog("Collected " + ids.size() + " valid damage types.");
        return ids;
    }

    /**
     * Orchestrates loading from all sources into a single merged definition map, then registers every definition and
     * eagerly resolves all references.
     *
     * @param registry the registry to populate
     * @throws RuntimeException wrapping any underlying I/O or parse failure
     */
    private void loadAllTags(TagRegistry registry) {
        try {
            Map<String, TagDefinition> mergedDefinitions = new LinkedHashMap<>();

            loadAllClasspathTags(mergedDefinitions);
            loadExternalZipAssetPacks(mergedDefinitions);

            for (var definition : mergedDefinitions.values()) {
                registry.register(definition);
            }

            for (var definition : registry.all()) {
                var result = registry.resolve(definition.canonicalId());

                for (var issue : result.issues()) {
                    TagCoreMod.warnLog(
                        "While resolving tag '" + definition.canonicalId() + "': " + issue.detail()
                    );
                }

                if (
                    result.status() == TagQueryStatus.CIRCULAR_REFERENCE
                        || result.status() == TagQueryStatus.INVALID_CONTENT
                        || result.status() == TagQueryStatus.NOT_FOUND
                        || result.status() == TagQueryStatus.INVALID_TAG_ID
                ) {
                    throw new IllegalStateException(
                        "Failed to fully resolve tag '" + definition.canonicalId() + "' with status " + result.status()
                    );
                }
            }

            TagCoreMod.infoLog("Loaded " + mergedDefinitions.size() + " tags.");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tag definitions", e);
        }
    }

    /**
     * Scans all {@code tags/} resources on the plugin classloader and loads their JSON tag definitions into
     * {@code sink}.
     * <p>
     * Supports both {@code file://} (exploded directory) and {@code jar://} (packaged JAR) URL protocols. Any other
     * protocol is skipped with a warning.
     *
     * @param sink the map to accumulate loaded definitions into
     * @throws Exception if resource enumeration or any individual file load fails
     */
    private void loadAllClasspathTags(Map<String, TagDefinition> sink) throws Exception {
        var classLoader = plugin.getClass().getClassLoader();
        Enumeration<URL> resources = classLoader.getResources("tags");

        if (!resources.hasMoreElements()) {
            TagCoreMod.warnLog("No tags resource folder found on classpath.");
            return;
        }

        while (resources.hasMoreElements()) {
            var resourceUrl = resources.nextElement();
            var protocol = resourceUrl.getProtocol();

            if ("file".equals(protocol)) {
                loadTagsFromDirectory(sink, resourceUrl);
            } else if ("jar".equals(protocol)) {
                loadTagsFromJar(sink, resourceUrl);
            } else {
                TagCoreMod.warnLog(
                    "Skipping unsupported tags resource protocol: " + protocol + " (" + resourceUrl + ")"
                );
            }
        }
    }

    /**
     * Recursively walks a file-system {@code tags/} directory and loads every {@code .json} file found as a
     * {@link TagDefinition}.
     *
     * @param sink        the map to accumulate loaded definitions into
     * @param resourceUrl a {@code file://} URL pointing to the {@code tags/} directory
     * @throws Exception if directory traversal or any file read fails
     */
    private void loadTagsFromDirectory(Map<String, TagDefinition> sink, URL resourceUrl) throws Exception {
        var tagsPath = Paths.get(resourceUrl.toURI());

        try (var stream = Files.walk(tagsPath)) {
            stream.filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(path -> {
                    var relative = tagsPath.relativize(path).toString().replace('\\', '/');
                    var sourceName = "tags/" + relative;

                    try (var input = Files.newInputStream(path)) {
                        var definition = loadTag(input, sourceName, TagSourceKind.CLASSPATH_DIRECTORY);
                        putDefinition(sink, definition, false, sourceName);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load tag resource " + sourceName, e);
                    }
                });
        }
    }

    /**
     * Iterates over a JAR file's entries and loads any {@code .json} files found under the {@code tags/} prefix as
     * {@link TagDefinition} instances.
     *
     * @param sink        the map to accumulate loaded definitions into
     * @param resourceUrl a {@code jar://} URL pointing to the {@code tags/} entry inside a JAR
     * @throws Exception if the JAR cannot be opened or any entry read fails
     */
    private void loadTagsFromJar(Map<String, TagDefinition> sink, URL resourceUrl) throws Exception {
        var connection = (JarURLConnection) resourceUrl.openConnection();

        try (var jarFile = connection.getJarFile()) {
            var entries = jarFile.entries();

            while (entries.hasMoreElements()) {
                var entry = entries.nextElement();
                var name = entry.getName();

                if (!entry.isDirectory() && name.startsWith("tags/") && name.endsWith(".json")) {
                    try (var input = jarFile.getInputStream(entry)) {
                        var definition = loadTag(input, name, TagSourceKind.EXTERNAL_JAR);
                        putDefinition(sink, definition, false, name);
                    }
                }
            }
        }
    }

    /**
     * Scans the external asset pack directory ({@code mods/}) for {@code .zip} and {@code .jar} files and loads tag
     * definitions from each, in alphabetical filename order.
     * <p>
     * Asset pack tags may override classpath-defined tags with the same ID, allowing server administrators or content
     * creators to replace built-in tag data without modifying the plugin JAR.
     *
     * @param sink the map to accumulate loaded definitions into
     * @throws Exception if directory listing or any pack load fails
     */
    private void loadExternalZipAssetPacks(Map<String, TagDefinition> sink) throws Exception {
        var assetPackDir = resolveAssetPackDirectory();

        if (!Files.exists(assetPackDir) || !Files.isDirectory(assetPackDir)) {
            return;
        }

        try (var stream = Files.list(assetPackDir)) {
            stream.filter(Files::isRegularFile)
                .filter(path -> {
                    var name = path.getFileName().toString().toLowerCase();
                    return name.endsWith(".zip") || name.endsWith(".jar");
                })
                .sorted()
                .forEach(path -> {
                    try {
                        loadTagsFromZip(sink, path);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to load asset pack " + path, e);
                    }
                });
        }
    }

    /**
     * Opens a ZIP or JAR asset pack at the given path and loads any {@code tags/*.json} entries it contains as
     * {@link TagDefinition} instances.
     * <p>
     * Definitions loaded from asset packs are treated as overrides; if a tag with the same ID already exists in
     * {@code sink} it will be replaced.
     *
     * @param sink    the map to accumulate loaded definitions into
     * @param zipPath the path to the {@code .zip} or {@code .jar} asset pack
     * @throws Exception if the archive cannot be opened or any entry fails to parse
     */
    private void loadTagsFromZip(Map<String, TagDefinition> sink, Path zipPath) throws Exception {
        try (var zipFile = new ZipFile(zipPath.toFile())) {
            var entries = zipFile.entries();

            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                var name = entry.getName();

                if (!entry.isDirectory() && name.startsWith("tags/") && name.endsWith(".json")) {
                    try (var input = zipFile.getInputStream(entry)) {
                        var definition = loadTag(
                            input,
                            zipPath.getFileName() + "!/" + name,
                            TagSourceKind.EXTERNAL_ZIP
                        );
                        putDefinition(sink, definition, true, zipPath + "!/" + name);
                    }
                }
            }
        }
    }

    /**
     * Inserts a {@link TagDefinition} into {@code sink}, applying override rules.
     * <p>
     * If {@code overrideExisting} is {@code true} and a definition with the same ID already exists, it will be replaced
     * and a log message emitted. If {@code overrideExisting} is {@code false} and a duplicate is encountered, the
     * incoming definition is discarded with a warning.
     *
     * @param sink             the map to insert or update the definition in
     * @param definition       the definition to insert; must not be {@code null}
     * @param overrideExisting {@code true} if this source is allowed to replace previously loaded definitions with the
     *                         same ID
     * @param sourceName       human-readable source path used in log and error messages
     * @throws NullPointerException  if {@code definition} is {@code null}
     * @throws IllegalStateException if the definition's {@code id} is null or blank
     */
    private void putDefinition(
        Map<String, TagDefinition> sink,
        TagDefinition definition,
        boolean overrideExisting,
        String sourceName
    ) {
        Objects.requireNonNull(definition, "definition");

        var canonicalId = definition.canonicalId();
        if (canonicalId.isBlank()) {
            throw new IllegalStateException("Tag definition from " + sourceName + " has null or blank id");
        }

        var existing = sink.get(canonicalId);
        if (existing == null) {
            sink.put(canonicalId, definition);
            TagCoreMod.infoLog("Loaded tag '" + canonicalId + "' from " + sourceName);
            return;
        }

        if (overrideExisting) {
            sink.put(canonicalId, definition);
            TagCoreMod.infoLog("Overrode tag '" + canonicalId + "' from " + sourceName);
        } else {
            TagCoreMod.warnLog(
                "Skipping duplicate built-in/classpath tag '" + canonicalId + "' from " + sourceName +
                    "' because one was already loaded"
            );
        }
    }

    /**
     * Parses a single tag JSON file from the given {@link InputStream} and returns the resulting {@link TagDefinition}.
     * <p>
     * Values prefixed with {@code #} are treated as tag references and will be resolved later by
     * {@link TagRegistry#resolve(String)}.
     *
     * @param stream     the input stream of the JSON file; closed by this method
     * @param sourceName human-readable source path used in error messages
     * @return the parsed {@link TagDefinition}
     * @throws RuntimeException if the stream cannot be read, JSON is malformed, or required fields ({@code id},
     *                          {@code type}) are missing
     */
    private TagDefinition loadTag(InputStream stream, String sourceName, TagSourceKind sourceKind) {
        try (var reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            var root = GSON.fromJson(reader, JsonObject.class);

            if (root == null) {
                throw new IllegalStateException("Tag JSON was empty: " + sourceName);
            }

            if (!root.has("id")) {
                throw new IllegalStateException("Missing 'id' in " + sourceName);
            }

            if (!root.has("type")) {
                throw new IllegalStateException("Missing 'type' in " + sourceName);
            }

            var rawId = root.get("id").getAsString();
            var tagId = TagId.parse(rawId);
            var type = TagType.fromJson(root.get("type").getAsString());

            var values = new ArrayList<String>();
            var valuesJson = root.getAsJsonArray("values");
            if (valuesJson != null) {
                for (var element : valuesJson) {
                    values.add(element.getAsString());
                }
            }

            return new TagDefinition(
                rawId,
                tagId,
                type,
                values,
                new TagSource(sourceKind, sourceName)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tag resource " + sourceName, e);
        }
    }

    /**
     * Resolves the absolute path of the external asset pack directory.
     * <p>
     * Currently returns the {@code mods/} directory relative to the current working directory (i.e., the server root).
     *
     * @return the absolute, normalized path to the asset pack directory
     */
    private Path resolveAssetPackDirectory() {
        return Paths.get("mods").toAbsolutePath().normalize();
    }
}
