package com.azuredoom.tagcore.api;

import java.util.*;

import com.azuredoom.tagcore.TagCoreMod;
import com.azuredoom.tagcore.data.*;
import com.azuredoom.tagcore.util.Internal;

/**
 * Public API facade over the internal {@link TagRegistry}, intended as the primary entry point for other plugins and
 * systems that need to query tag data.
 * <p>
 * All methods delegate directly to the underlying {@link TagRegistry}. Using {@code TagService} rather than accessing
 * the registry directly ensures a stable, intention-revealing API surface and insulates callers from internal registry
 * details.
 */
public final class TagService {

    private volatile TagRegistry registry;

    /**
     * Constructs a new {@code TagService} backed by the given registry.
     *
     * @param registry the registry to delegate all queries to; must not be {@code null}
     * @throws NullPointerException if {@code registry} is {@code null}
     */
    public TagService(TagRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    @Internal("Used only during reload lifecycle")
    public void swapRegistry(TagRegistry registry) {
        this.registry = Objects.requireNonNull(registry, "registry");
    }

    /**
     * Returns the current backing {@link TagRegistry} instance used by this service.
     * <p>
     * This method is intended for internal use only and provides direct access to the underlying registry for
     * delegation purposes.
     *
     * @return the active {@link TagRegistry}; never {@code null}
     */
    @Internal("Internal accessor for the backing TagRegistry; not part of public API")
    private TagRegistry registry() {
        return this.registry;
    }

    /**
     * Returns the globally available {@code TagService} instance, if it has been initialized.
     * <p>
     * This method provides safe access to the shared service without requiring callers to depend directly on internal
     * initialization state.
     *
     * @return an {@code Optional} containing the active {@link TagService} instance, or an empty {@code Optional} if
     *         the service is not currently available
     */
    public static Optional<TagService> getTagService() {
        return Optional.ofNullable(TagCoreMod.getTagService());
    }

    /**
     * Returns {@code true} if a tag with the given ID is registered.
     *
     * @param tagId the tag identifier to check
     * @return {@code true} if the tag exists; {@code false} otherwise
     */
    public boolean hasTag(String tagId) {
        return registry().contains(tagId);
    }

    /**
     * Returns an unmodifiable view of every registered {@link TagDefinition}, in the order they were registered.
     *
     * @return an unmodifiable collection of all tag definitions; never {@code null}
     */
    public Collection<TagDefinition> allTags() {
        return registry().all();
    }

    /**
     * Returns {@code true} if the given value is a member of the specified tag's resolved value set, regardless of tag
     * type.
     *
     * @param tagId  the ID of the tag to check against
     * @param itemId the value to test for membership
     * @return {@code true} if {@code value} is in the resolved set of {@code tagId}
     */
    public TagQueryResult<Boolean> isInTag(String tagId, String itemId) {
        return registry().containsValue(tagId, itemId);
    }

    /**
     * Returns {@code true} if the given item identifier is a member of the specified {@link TagType#ITEM} tag's
     * resolved value set.
     *
     * @param tagId  the ID of the item tag to check against
     * @param itemId the item identifier to test for membership
     * @return {@code true} if {@code itemId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInItemTag(String tagId, String itemId) {
        return registry().containsValue(tagId, TagType.ITEM, itemId);
    }

    /**
     * Returns {@code true} if the given block identifier is a member of the specified {@link TagType#BLOCK} tag's
     * resolved value set.
     *
     * @param tagId   the ID of the block tag to check against
     * @param blockId the block identifier to test for membership
     * @return {@code true} if {@code blockId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInBlockTag(String tagId, String blockId) {
        return registry().containsValue(tagId, TagType.BLOCK, blockId);
    }

    /**
     * Returns {@code true} if the given entity identifier is a member of the specified {@link TagType#ENTITY} tag's
     * resolved value set.
     *
     * @param tagId    the ID of the entity tag to check against
     * @param entityId the entity identifier to test for membership
     * @return {@code true} if {@code entityId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInEntityTag(String tagId, String entityId) {
        return registry().containsValue(tagId, TagType.ENTITY, entityId);
    }

    /**
     * Returns {@code true} if the given entity identifier is a member of the specified {@link TagType#BIOME} tag's
     * resolved value set.
     *
     * @param tagId   the ID of the biome tag check against
     * @param biomeId the biome identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInBiomeTag(String tagId, String biomeId) {
        return registry().containsValue(tagId, TagType.BIOME, biomeId);
    }

    /**
     * Returns {@code true} if the given entity identifier is a member of the specified {@link TagType#EFFECT} tag's
     * resolved value set.
     *
     * @param tagId    the ID of the effect tag check against
     * @param effectId the effect identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInEffectTag(String tagId, String effectId) {
        return registry().containsValue(tagId, TagType.EFFECT, effectId);
    }

    /**
     * Returns {@code true} if the given fluid identifier is a member of the specified {@link TagType#FLUID} tag's
     * resolved value set.
     *
     * @param tagId   the ID of the fluid tag check against
     * @param fluidId the fluid identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInFluidTag(String tagId, String fluidId) {
        return registry().containsValue(tagId, TagType.FLUID, fluidId);
    }

    /**
     * Returns {@code true} if the given damage type identifier is a member of the specified {@link TagType#DAMAGE_TYPE}
     * tag's resolved value set.
     *
     * @param tagId        the ID of the damage type tag check against
     * @param damageTypeId the damage type identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInDamageTypeTag(String tagId, String damageTypeId) {
        return registry().containsValue(tagId, TagType.DAMAGE_TYPE, damageTypeId);
    }

    /**
     * Returns {@code true} if the given sound identifier is a member of the specified {@link TagType#SOUND} tag's
     * resolved value set.
     *
     * @param tagId   the ID of the sound tag check against
     * @param soundId the sound identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInSoundTag(String tagId, String soundId) {
        return registry().containsValue(tagId, TagType.SOUND, soundId);
    }

    /**
     * Returns {@code true} if the given environment identifier is a member of the specified {@link TagType#ENVIRONMENT}
     * tag's resolved value set.
     *
     * @param tagId         the ID of the environment tag check against
     * @param environmentId the environment identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInEnvironmentTag(String tagId, String environmentId) {
        return registry().containsValue(tagId, TagType.ENVIRONMENT, environmentId);
    }

    /**
     * Returns {@code true} if the given item category identifier is a member of the specified
     * {@link TagType#ITEM_CATEGORY} tag's resolved value set.
     *
     * @param tagId          the ID of the item category tag check against
     * @param itemCategoryId the item category identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInItemCategoryTag(String tagId, String itemCategoryId) {
        return registry().containsValue(tagId, TagType.ITEM_CATEGORY, itemCategoryId);
    }

    /**
     * Returns {@code true} if the given root interaction identifier is a member of the specified
     * {@link TagType#ROOT_INTERACTION} tag's resolved value set.
     *
     * @param tagId             the ID of the root interaction tag check against
     * @param rootInteractionId the root interaction identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInRootInteractionTag(String tagId, String rootInteractionId) {
        return registry().containsValue(tagId, TagType.ROOT_INTERACTION, rootInteractionId);
    }

    /**
     * Returns {@code true} if the given emote identifier is a member of the specified {@link TagType#EMOTE} tag's
     * resolved value set.
     *
     * @param tagId   the ID of the emote tag check against
     * @param emoteId the emote identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInEmoteTag(String tagId, String emoteId) {
        return registry().containsValue(tagId, TagType.EMOTE, emoteId);
    }

    /**
     * Returns {@code true} if the given entity stat type identifier is a member of the specified
     * {@link TagType#ENTITY_STAT_TYPE} tag's resolved value set.
     *
     * @param tagId            the ID of the entity stat type tag check against
     * @param entityStatTypeId the entity stat type identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInEntityStatTypeTag(String tagId, String entityStatTypeId) {
        return registry().containsValue(tagId, TagType.ENTITY_STAT_TYPE, entityStatTypeId);
    }

    /**
     * Returns {@code true} if the given particle identifier is a member of the specified {@link TagType#PARTICLE} tag's
     * resolved value set.
     *
     * @param tagId      the ID of the particle tag check against
     * @param particleId the particle identifier to test for membership
     * @return {@code true} if {@code tagId} is in the tag's resolved set
     */
    public TagQueryResult<Boolean> isInParticleTag(String tagId, String particleId) {
        return registry().containsValue(tagId, TagType.PARTICLE, particleId);
    }

    /**
     * Returns the fully resolved set of item identifiers for the specified {@link TagType#ITEM} tag.
     *
     * @param tagId the ID of the item tag to resolve
     * @return an unmodifiable set of resolved item identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveItemTag(String tagId) {
        return registry().resolve(tagId, TagType.ITEM);
    }

    /**
     * Returns the fully resolved set of block identifiers for the specified {@link TagType#BLOCK} tag.
     *
     * @param tagId the ID of the block tag to resolve
     * @return an unmodifiable set of resolved block identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveBlockTag(String tagId) {
        return registry().resolve(tagId, TagType.BLOCK);
    }

    /**
     * Returns the fully resolved set of entity identifiers for the specified {@link TagType#ENTITY} tag.
     *
     * @param tagId the ID of the entity tag to resolve
     * @return an unmodifiable set of resolved entity identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveEntityTag(String tagId) {
        return registry().resolve(tagId, TagType.ENTITY);
    }

    /**
     * Returns the fully resolved set of biome identifiers for the specified {@link TagType#BIOME} tag.
     *
     * @param tagId the ID of the biome tag to resolve
     * @return an unmodifiable set of resolved biome identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveBiomeTag(String tagId) {
        return registry().resolve(tagId, TagType.BIOME);
    }

    /**
     * Returns the fully resolved set of effect identifiers for the specified {@link TagType#EFFECT} tag.
     *
     * @param tagId the ID of the effect tag to resolve
     * @return an unmodifiable set of resolved effect identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveEffectTag(String tagId) {
        return registry().resolve(tagId, TagType.EFFECT);
    }

    /**
     * Returns the fully resolved set of fluid identifiers for the specified {@link TagType#FLUID} tag.
     *
     * @param tagId the ID of the fluid tag to resolve
     * @return an unmodifiable set of resolved fluid identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveFluidTag(String tagId) {
        return registry().resolve(tagId, TagType.FLUID);
    }

    /**
     * Returns the fully resolved set of damage type identifiers for the specified {@link TagType#DAMAGE_TYPE} tag.
     *
     * @param tagId the ID of the damage type tag to resolve
     * @return an unmodifiable set of resolved damage type identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveDamageTypeTag(String tagId) {
        return registry().resolve(tagId, TagType.DAMAGE_TYPE);
    }

    /**
     * Returns the fully resolved set of sound identifiers for the specified {@link TagType#SOUND} tag.
     *
     * @param tagId the ID of the sound tag to resolve
     * @return an unmodifiable set of resolved sound identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveSoundTag(String tagId) {
        return registry().resolve(tagId, TagType.SOUND);
    }

    /**
     * Returns the fully resolved set of environment identifiers for the specified {@link TagType#ENVIRONMENT} tag.
     *
     * @param tagId the ID of the environment tag to resolve
     * @return an unmodifiable set of resolved environment identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveEnvironmentTag(String tagId) {
        return registry().resolve(tagId, TagType.ENVIRONMENT);
    }

    /**
     * Returns the fully resolved set of item category identifiers for the specified {@link TagType#ITEM_CATEGORY} tag.
     *
     * @param tagId the ID of the item category tag to resolve
     * @return an unmodifiable set of resolved item category identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveItemCategoryTag(String tagId) {
        return registry().resolve(tagId, TagType.ITEM_CATEGORY);
    }

    /**
     * Returns the fully resolved set of root interaction identifiers for the specified {@link TagType#ROOT_INTERACTION}
     * tag.
     *
     * @param tagId the ID of the root interaction tag to resolve
     * @return an unmodifiable set of resolved root interaction identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveRootInteractionTag(String tagId) {
        return registry().resolve(tagId, TagType.ROOT_INTERACTION);
    }

    /**
     * Returns the fully resolved set of emote identifiers for the specified {@link TagType#EMOTE} tag.
     *
     * @param tagId the ID of the emote tag to resolve
     * @return an unmodifiable set of resolved emote identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveEmoteTag(String tagId) {
        return registry().resolve(tagId, TagType.EMOTE);
    }

    /**
     * Returns the fully resolved set of entity stat type identifiers for the specified {@link TagType#ENTITY_STAT_TYPE}
     * tag.
     *
     * @param tagId the ID of the entity stat type tag to resolve
     * @return an unmodifiable set of resolved entity stat type identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveEntityStatTypeTag(String tagId) {
        return registry().resolve(tagId, TagType.ENTITY_STAT_TYPE);
    }

    /**
     * Returns the fully resolved set of particle identifiers for the specified {@link TagType#PARTICLE} tag.
     *
     * @param tagId the ID of the particle tag to resolve
     * @return an unmodifiable set of resolved particle identifiers; never {@code null}
     */
    public TagQueryResult<Set<String>> resolveParticleTag(String tagId) {
        return registry().resolve(tagId, TagType.PARTICLE);
    }

    /**
     * Returns the canonical IDs of all registered tags whose raw value lists directly reference the specified tag.
     * <p>
     * Only direct {@code #reference} entries are considered. This method does not resolve nested references and does
     * not require the referenced tag to exist.
     *
     * @param tagId the tag ID to search references for
     * @return a query result containing the matching canonical tag IDs
     */
    public TagQueryResult<Set<String>> getTagsReferencing(String tagId) {
        var canonicalId = registry().normalizeLookupId(tagId);
        if (canonicalId == null) {
            return TagQueryResult.of(
                TagQueryStatus.INVALID_TAG_ID,
                Set.of(),
                null,
                List.of(
                    new TagResolveIssue(
                        TagResolveIssueType.INVALID_LOOKUP_ID,
                        String.valueOf(tagId),
                        "Invalid tag id"
                    )
                )
            );
        }

        Set<String> matches = new LinkedHashSet<>();

        for (var definition : registry().all()) {
            for (var rawValue : definition.values()) {
                if (rawValue == null || rawValue.isBlank() || !rawValue.startsWith("#")) {
                    continue;
                }

                String referencedCanonical;
                try {
                    referencedCanonical = TagId.parse(rawValue.substring(1).trim()).canonical();
                } catch (IllegalArgumentException e) {
                    continue;
                }

                if (referencedCanonical.equals(canonicalId)) {
                    matches.add(definition.canonicalId());
                    break;
                }
            }
        }

        return TagQueryResult.of(
            matches.isEmpty() ? TagQueryStatus.EMPTY : TagQueryStatus.SUCCESS,
            Collections.unmodifiableSet(matches),
            null,
            List.of()
        );
    }

    /**
     * Returns the canonical IDs of all registered tags of the given type whose resolved value sets contain the
     * specified value.
     *
     * @param type  the tag type to search within
     * @param value the concrete value to match
     * @return a query result containing the matching canonical tag IDs
     */
    public TagQueryResult<Set<String>> getTagsForValue(TagType type, String value) {
        if (type == null) {
            throw new NullPointerException("type");
        }

        if (value == null || value.isBlank()) {
            return TagQueryResult.of(
                TagQueryStatus.INVALID_CONTENT,
                Set.of(),
                null,
                List.of(
                    new TagResolveIssue(
                        TagResolveIssueType.INVALID_VALUE,
                        null,
                        "Value cannot be null or blank"
                    )
                )
            );
        }

        Set<String> matches = new LinkedHashSet<>();
        List<TagResolveIssue> issues = new ArrayList<>();
        var hadFailures = false;

        for (var definition : registry().all()) {
            if (definition.type() != type) {
                continue;
            }

            var resolved = registry().resolve(definition.canonicalId(), type);

            if (!resolved.isSuccess()) {
                hadFailures = true;
                issues.addAll(resolved.issues());
                continue;
            }

            if (resolved.value().contains(value)) {
                matches.add(definition.canonicalId());
            }
        }

        TagQueryStatus status;
        if (!matches.isEmpty()) {
            status = TagQueryStatus.SUCCESS;
        } else if (hadFailures) {
            status = TagQueryStatus.INVALID_CONTENT;
        } else {
            status = TagQueryStatus.EMPTY;
        }

        return TagQueryResult.of(
            status,
            Collections.unmodifiableSet(matches),
            null,
            issues
        );
    }

    /**
     * Returns {@code true} if the specified value is contained in the resolved value set of any of the supplied tag
     * IDs.
     * <p>
     * Only tags matching the requested {@link TagType} are considered. Unknown tag IDs, tags of the wrong type, and
     * {@code null} entries are ignored.
     *
     * @param tagType the required tag type
     * @param value   the concrete value to test
     * @param tagIds  the tag IDs to check
     * @return {@code true} if {@code value} matches at least one compatible tag; {@code false} otherwise
     */
    public boolean matchesAny(TagType tagType, String value, Collection<String> tagIds) {
        if (tagType == null || value == null || value.isBlank() || tagIds == null || tagIds.isEmpty()) {
            return false;
        }

        for (var tagId : tagIds) {
            if (tagId == null || tagId.isBlank()) {
                continue;
            }

            var definition = registry().get(tagId);
            if (definition == null || definition.type() != tagType) {
                continue;
            }

            if (registry().resolve(tagId, tagType).value().contains(value)) {
                return true;
            }
        }

        return false;
    }
}
