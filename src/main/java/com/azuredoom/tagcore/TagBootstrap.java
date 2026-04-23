package com.azuredoom.tagcore;

import com.azuredoom.hytalecustomassetloader.*;
import com.azuredoom.hytalecustomassetloader.model.AssetReloadResult;
import com.azuredoom.hytalecustomassetloader.model.AssetSource;
import com.azuredoom.hytalecustomassetloader.model.AssetSourceKind;
import com.azuredoom.hytalecustomassetloader.spi.AssetLogger;
import com.azuredoom.hytalecustomassetloader.spi.ReloadableAssetRegistrar;
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
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

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

    private final TagRegistry registry;

    private final AssetLoader<TagDefinition> loader;

    private final ReloadableAssetRegistrar<TagDefinition> registrar;

    public TagBootstrap(JavaPlugin plugin) {
        this.registry = new TagRegistry(
            collectValidItemIds(),
            collectValidBlockIds(),
            collectValidEntityIds(),
            collectValidBiomeIds(),
            collectValidEffectIds(),
            collectValidFluidIds(),
            collectValidDamageTypeIds()
        );

        var options = new AssetDiscoveryOptions(
            "tags",
            ".json",
            Paths.get("mods").toAbsolutePath().normalize(),
            true,
            true,
            true,
            true,
            true,
            Duration.ofMillis(250)
        );

        this.loader = new AssetLoader<>(
            plugin.getClass().getClassLoader(),
            options,
            this::loadTag,
            TagDefinition::canonicalId,
            new AssetLogger() {

                @Override
                public void info(String message) {
                    TagCoreMod.infoLog(message);
                }

                @Override
                public void warn(String message) {
                    TagCoreMod.warnLog(message);
                }
            }
        );

        this.registrar = new ReloadableAssetRegistrar<>() {

            @Override
            public void add(String id, TagDefinition asset) {
                registry.register(asset);
                TagCoreMod.infoLog("Added tag: " + id);
            }

            @Override
            public void update(String id, TagDefinition previousAsset, TagDefinition currentAsset) {
                registry.remove(id);
                registry.register(currentAsset);
                TagCoreMod.infoLog("Updated tag: " + id);
            }

            @Override
            public void remove(String id, TagDefinition asset) {
                registry.remove(id);
                TagCoreMod.infoLog("Removed tag: " + id);
            }
        };
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
        var result = loader.loadAll();

        for (var entry : result.snapshot().mergedAssets().entrySet()) {
            registrar.add(entry.getKey(), entry.getValue());
        }

        validateAllTags();
        TagCoreMod.infoLog("Loaded " + result.snapshot().mergedAssets().size() + " tags.");
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
     * Reloads all tag assets, applies incremental changes, and validates the resulting registry.
     * <p>
     * This method delegates to the underlying asset loader to compute a reload result, applies the diff to the
     * registrar, and then performs a full validation pass over all registered tags.
     * </p>
     *
     * @return the result of the reload operation, including the updated snapshot and diff
     */
    public AssetReloadResult<TagDefinition> reload() {
        var result = loader.reload();
        for (var entry : result.currentSnapshot().mergedAssets().entrySet()) {
            TagCoreMod.infoLog(
                "Tag loaded: " + entry.getKey() + " from " + entry.getValue().source()
            );
        }
        registrar.applyReload(result);
        validateAllTags();

        TagCoreMod.infoLog(
            "Reload complete. Added=" + result.diff().added().size()
                + ", Updated=" + result.diff().updated().size()
                + ", Removed=" + result.diff().removed().size()
        );

        return result;
    }

    /**
     * Returns the current tag registry.
     *
     * @return the active tag registry
     */
    public TagRegistry registry() {
        return registry;
    }

    /**
     * Validates all registered tags by attempting to resolve each one.
     * <p>
     * Any issues encountered during resolution are logged. Tags that fail to resolve due to circular references,
     * invalid content, missing definitions, or invalid identifiers will produce warning logs.
     * </p>
     */
    private void validateAllTags() {
        for (var definition : registry.all()) {
            var resultResolve = registry.resolve(definition.canonicalId());

            for (var issue : resultResolve.issues()) {
                TagCoreMod.warnLog(
                    "While resolving tag '" + definition.canonicalId() + "': " + issue.detail()
                );
            }

            if (
                resultResolve.status() == TagQueryStatus.CIRCULAR_REFERENCE
                    || resultResolve.status() == TagQueryStatus.INVALID_CONTENT
                    || resultResolve.status() == TagQueryStatus.NOT_FOUND
                    || resultResolve.status() == TagQueryStatus.INVALID_TAG_ID
            ) {
                TagCoreMod.warnLog(
                    "Failed to resolve tag '" + definition.canonicalId() + "' with status " + resultResolve.status()
                );
            }
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
    private TagDefinition loadTag(InputStream stream, String sourceName, AssetSourceKind sourceKind) {
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
                new AssetSource(sourceKind, sourceName)
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed to load tag resource " + sourceName, e);
        }
    }
}
