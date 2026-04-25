package com.azuredoom.tagcore.util;

import com.hypixel.hytale.builtin.hytalegenerator.assets.biomes.BiomeAsset;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.environment.config.Environment;
import com.hypixel.hytale.server.core.asset.type.fluid.Fluid;
import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import com.hypixel.hytale.server.core.asset.type.item.config.ItemCategory;
import com.hypixel.hytale.server.core.asset.type.particle.config.ParticleSystem;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.cosmetics.EmoteAsset;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.modules.entitystats.asset.EntityStatType;
import com.hypixel.hytale.server.core.modules.interaction.interaction.config.RootInteraction;
import com.hypixel.hytale.server.npc.NPCPlugin;

import java.util.*;
import java.util.function.Function;

import com.azuredoom.tagcore.TagCoreMod;
import com.azuredoom.tagcore.data.TagType;

/**
 * Utility class for collecting known valid game identifiers by {@link TagType}.
 * <p>
 * Each supported tag type is populated from its corresponding Hytale asset registry, with entity identifiers collected
 * from the NPC plugin. The resulting map is suitable for passing into {@link com.azuredoom.tagcore.data.TagRegistry}
 * for concrete tag value validation.
 */
public final class TagValidIds {

    private TagValidIds() {}

    /**
     * Collects all known valid game identifiers grouped by {@link TagType}.
     * <p>
     * Every {@link TagType} is guaranteed to have an entry in the returned map. Types that cannot be collected or have
     * no valid identifiers will be mapped to an empty set. Both the returned map and its contained sets are immutable.
     *
     * @return an immutable map of tag types to valid identifier sets; never {@code null}
     */
    public static Map<TagType, Set<String>> collectAll() {
        Map<TagType, Set<String>> ids = new EnumMap<>(TagType.class);

        ids.put(TagType.ITEM, collectValidIds(Item.getAssetMap().getAssetMap().values(), Item::getId, "Item"));
        ids.put(
            TagType.BLOCK,
            collectValidIds(BlockType.getAssetMap().getAssetMap().values(), BlockType::getId, "Block")
        );
        ids.put(TagType.ENTITY, collectValidEntityIds());
        ids.put(
            TagType.BIOME,
            collectValidIds(BiomeAsset.getAssetStore().getAssetMap().getAssetMap().values(), BiomeAsset::getId, "Biome")
        );
        ids.put(
            TagType.EFFECT,
            collectValidIds(EntityEffect.getAssetMap().getAssetMap().values(), EntityEffect::getId, "Effect")
        );
        ids.put(TagType.FLUID, collectValidIds(Fluid.getAssetMap().getAssetMap().values(), Fluid::getId, "Fluid"));
        ids.put(
            TagType.DAMAGE_TYPE,
            collectValidIds(DamageCause.getAssetMap().getAssetMap().values(), DamageCause::getId, "Damage Type")
        );
        ids.put(
            TagType.SOUND,
            collectValidIds(SoundEvent.getAssetMap().getAssetMap().values(), SoundEvent::getId, "Sound")
        );
        ids.put(
            TagType.ENVIRONMENT,
            collectValidIds(Environment.getAssetMap().getAssetMap().values(), Environment::getId, "Environment")
        );
        ids.put(
            TagType.ITEM_CATEGORY,
            collectValidIds(ItemCategory.getAssetMap().getAssetMap().values(), ItemCategory::getId, "Item Category")
        );
        ids.put(
            TagType.ROOT_INTERACTION,
            collectValidIds(
                RootInteraction.getAssetMap().getAssetMap().values(),
                RootInteraction::getId,
                "Root Interaction"
            )
        );
        ids.put(
            TagType.EMOTE,
            collectValidIds(EmoteAsset.getAssetMap().getAssetMap().values(), EmoteAsset::getId, "Emote")
        );
        ids.put(
            TagType.ENTITY_STAT_TYPE,
            collectValidIds(
                EntityStatType.getAssetMap().getAssetMap().values(),
                EntityStatType::getId,
                "Entity Stat Type"
            )
        );
        ids.put(
            TagType.PARTICLE,
            collectValidIds(ParticleSystem.getAssetMap().getAssetMap().values(), ParticleSystem::getId, "Particle")
        );

        for (var type : TagType.values()) {
            ids.compute(type, (k, value) -> value != null ? Set.copyOf(value) : Collections.emptySet());
        }

        return Collections.unmodifiableMap(ids);
    }

    /**
     * Collects all valid identifiers from the provided collection using the given extractor function.
     * <p>
     * Only non-null, non-blank IDs are included. The resulting set preserves the iteration order of the provided
     * collection.
     *
     * @param <T>         the type of elements in the input collection
     * @param values      the collection of values to extract identifiers from
     * @param idExtractor a function used to extract the identifier from each element
     * @param name        the descriptive name of the identifier type (used for logging)
     * @return a set of valid identifiers; never {@code null}
     */
    private static <T> Set<String> collectValidIds(
        Collection<T> values,
        Function<T, String> idExtractor,
        String name
    ) {
        Set<String> ids = new LinkedHashSet<>();

        for (T value : values) {
            if (value == null)
                continue;

            var id = idExtractor.apply(value);
            if (id != null && !id.isBlank()) {
                ids.add(id);
            }
        }

        TagCoreMod.infoLog("Collected " + ids.size() + " valid " + name + " ids.");
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
    private static Set<String> collectValidEntityIds() {
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
}
