package com.azuredoom.tagcore.data;

import java.util.*;

/**
 * Central store for all registered {@link TagDefinition} instances, providing registration, lookup, type-safe access,
 * and recursive value resolution.
 * <p>
 * Tags are keyed by their canonical string ID. Once registered, a tag's resolution result (including its complete,
 * flattened set of concrete identifiers with all {@code #reference} values expanded) is computed on first access and
 * cached for subsequent calls.
 * <h2>Tag References</h2>
 * <p>
 * A tag value beginning with {@code #} (e.g. {@code "#somemod:logs"}) is treated as a reference to another registered
 * tag. During resolution:
 * <ul>
 * <li>The referenced tag must exist in this registry.</li>
 * <li>The referenced tag must be the same {@link TagType} as the referencing tag.</li>
 * <li>Circular references (A → B → A) are detected and reported immediately.</li>
 * </ul>
 */
public final class TagRegistry {

    private final Map<String, TagDefinition> tags = new LinkedHashMap<>();

    private final Map<String, TagQueryResult<Set<String>>> resolvedCache = new LinkedHashMap<>();

    private final Set<String> validItemIds;

    private final Set<String> validBlockIds;

    private final Set<String> validEntityIds;

    private final Set<String> validBiomeIds;

    private final Set<String> validEffectIds;

    private final Set<String> validFluidIds;

    private final Set<String> validDamageTypeIds;

    public TagRegistry(
        Set<String> validItemIds,
        Set<String> validBlockIds,
        Set<String> validEntityIds,
        Set<String> validBiomeIds,
        Set<String> validEffectIds,
        Set<String> validFluidIds,
        Set<String> validDamageTypeIds
    ) {
        this.validItemIds = validItemIds != null ? Set.copyOf(validItemIds) : Collections.emptySet();
        this.validBlockIds = validBlockIds != null ? Set.copyOf(validBlockIds) : Collections.emptySet();
        this.validEntityIds = validEntityIds != null ? Set.copyOf(validEntityIds) : Collections.emptySet();
        this.validBiomeIds = validBiomeIds != null ? Set.copyOf(validBiomeIds) : Collections.emptySet();
        this.validEffectIds = validEffectIds != null ? Set.copyOf(validEffectIds) : Collections.emptySet();
        this.validFluidIds = validFluidIds != null ? Set.copyOf(validFluidIds) : Collections.emptySet();
        this.validDamageTypeIds = validDamageTypeIds != null ? Set.copyOf(validDamageTypeIds) : Collections.emptySet();
    }

    /**
     * Registers a {@link TagDefinition} in this registry.
     * <p>
     * If a tag with the same ID is already registered it will be silently replaced. The resolved-value cache is cleared
     * on every registration to ensure stale resolutions are not returned for tags that may reference the newly added
     * one.
     *
     * @param definition the definition to register; must not be {@code null}
     * @throws NullPointerException if {@code definition} is {@code null}
     */
    public void register(TagDefinition definition) {
        Objects.requireNonNull(definition, "definition");

        var canonicalId = definition.canonicalId();
        if (canonicalId.isBlank()) {
            throw new IllegalStateException("Tag definition has null or blank canonical id");
        }

        tags.put(canonicalId, definition);
        resolvedCache.clear();
    }

    /**
     * Normalizes an external tag lookup ID into its canonical string form.
     * <p>
     * This method accepts both fully qualified namespaced IDs (for example, {@code "somemod:logs"}) and bare IDs (for
     * example, {@code "Fish_Eel_Moray_Item"}). Bare IDs are normalized using the default namespace defined by
     * {@link TagId}.
     *
     * @param tagId the raw tag ID to normalize
     * @return the canonical tag ID, or {@code null} if {@code tagId} is {@code null}, blank, or invalid
     */
    public String normalizeLookupId(String tagId) {
        if (tagId == null || tagId.isBlank()) {
            return null;
        }

        try {
            return TagId.parse(tagId).canonical();
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Returns the registered {@link TagDefinition} for the given ID if it exists and matches the expected
     * {@link TagType}.
     * <p>
     * The supplied ID is normalized to its canonical form before lookup. If the ID is invalid, no matching tag exists,
     * or the registered tag is of a different type, the returned {@link TagQueryResult} describes the failure.
     *
     * @param tagId        the tag ID to look up
     * @param expectedType the required tag type; must not be {@code null}
     * @return a {@link TagQueryResult} containing the matching definition when successful
     * @throws NullPointerException if {@code expectedType} is {@code null}
     */
    public TagQueryResult<TagDefinition> getTyped(String tagId, TagType expectedType) {
        if (expectedType == null) {
            throw new NullPointerException("expectedType");
        }

        String canonicalId = normalizeLookupId(tagId);
        if (canonicalId == null) {
            return TagQueryResult.of(
                TagQueryStatus.INVALID_TAG_ID,
                null,
                null,
                List.of(new TagResolveIssue(TagResolveIssueType.INVALID_LOOKUP_ID, tagId, "Invalid tag lookup id"))
            );
        }

        TagDefinition definition = tags.get(canonicalId);
        if (definition == null) {
            return TagQueryResult.of(
                TagQueryStatus.NOT_FOUND,
                null,
                null,
                List.of(new TagResolveIssue(TagResolveIssueType.UNKNOWN_TAG, canonicalId, "Unknown tag"))
            );
        }

        if (definition.type() != expectedType) {
            return TagQueryResult.of(
                TagQueryStatus.WRONG_TYPE,
                null,
                definition,
                List.of(
                    new TagResolveIssue(
                        TagResolveIssueType.WRONG_TYPE_REFERENCE,
                        canonicalId,
                        "Expected " + expectedType + " but found " + definition.type()
                    )
                )
            );
        }

        return TagQueryResult.of(TagQueryStatus.SUCCESS, definition, definition, List.of());
    }

    /**
     * Resolves the tag with the given ID into its fully flattened set of concrete values.
     * <p>
     * All {@code #reference} values are expanded recursively. The supplied ID is normalized to its canonical form
     * before lookup. The resulting {@link TagQueryResult} distinguishes successful resolution from invalid IDs, missing
     * tags, circular references, and invalid content.
     * <p>
     * Successful results are cached after the first resolution. Subsequent calls for the same canonical ID return the
     * cached result without re-traversal.
     *
     * @param tagId the tag ID to resolve
     * @return a {@link TagQueryResult} containing the resolved value set and any issues encountered during resolution
     */
    public TagQueryResult<Set<String>> resolve(String tagId) {
        String canonicalId = normalizeLookupId(tagId);
        if (canonicalId == null) {
            return TagQueryResult.of(
                TagQueryStatus.INVALID_TAG_ID,
                Set.of(),
                null,
                List.of(
                    new TagResolveIssue(TagResolveIssueType.INVALID_LOOKUP_ID, String.valueOf(tagId), "Invalid tag id")
                )
            );
        }

        TagQueryResult<Set<String>> cached = resolvedCache.get(canonicalId);
        if (cached != null) {
            return cached;
        }

        TagQueryResult<Set<String>> result = resolveInternal(canonicalId, new LinkedHashSet<>());
        resolvedCache.put(canonicalId, result);
        return result;
    }

    /**
     * Resolves the tag with the given ID into its fully flattened set of concrete values, requiring that the tag match
     * the specified {@link TagType}.
     * <p>
     * The supplied ID is normalized to its canonical form before lookup. If the tag does not exist, the ID is invalid,
     * or the registered tag is of a different type, the returned {@link TagQueryResult} describes the failure.
     *
     * @param tagId        the tag ID to resolve
     * @param expectedType the required tag type; must not be {@code null}
     * @return a {@link TagQueryResult} containing the resolved value set when successful
     * @throws NullPointerException if {@code expectedType} is {@code null}
     */
    public TagQueryResult<Set<String>> resolve(String tagId, TagType expectedType) {
        Objects.requireNonNull(expectedType, "expectedType");

        var typed = getTyped(tagId, expectedType);
        if (!typed.isSuccess()) {
            return TagQueryResult.of(typed.status(), Set.of(), typed.definition(), typed.issues());
        }

        return resolve(typed.definition().canonicalId());
    }

    /**
     * Internal recursive resolver that expands all {@code #reference} values for the specified canonical tag ID.
     * <p>
     * The {@code visiting} set tracks the current resolution path and is used to detect circular references. Missing
     * referenced tags, invalid references, cross-type references, and invalid concrete values are collected as
     * {@link TagResolveIssue} instances and returned in the resulting {@link TagQueryResult}.
     * <p>
     * This method expects {@code canonicalId} to already be normalized into canonical form.
     *
     * @param canonicalId the canonical tag ID currently being resolved
     * @param visiting    the set of canonical tag IDs on the current resolution path, used for cycle detection
     * @return a {@link TagQueryResult} containing the resolved concrete values and any issues encountered during
     *         resolution
     */
    private TagQueryResult<Set<String>> resolveInternal(String canonicalId, Set<String> visiting) {
        var definition = tags.get(canonicalId);
        if (definition == null) {
            return TagQueryResult.of(
                TagQueryStatus.NOT_FOUND,
                Set.of(),
                null,
                List.of(new TagResolveIssue(TagResolveIssueType.UNKNOWN_TAG, canonicalId, "Unknown tag"))
            );
        }

        if (!visiting.add(canonicalId)) {
            return TagQueryResult.of(
                TagQueryStatus.CIRCULAR_REFERENCE,
                Set.of(),
                definition,
                List.of(
                    new TagResolveIssue(
                        TagResolveIssueType.CIRCULAR_REFERENCE,
                        canonicalId,
                        "Circular tag reference detected: " + visiting + " -> " + canonicalId
                    )
                )
            );
        }

        Set<String> resolved = new LinkedHashSet<>();
        List<TagResolveIssue> issues = new ArrayList<>();
        var circular = false;
        var invalidContent = false;

        for (var rawValue : definition.values()) {
            if (rawValue == null || rawValue.isBlank()) {
                continue;
            }

            if (rawValue.startsWith("#")) {
                var refRaw = rawValue.substring(1).trim();
                TagId refId;

                try {
                    refId = TagId.parse(refRaw);
                } catch (IllegalArgumentException e) {
                    issues.add(
                        new TagResolveIssue(
                            TagResolveIssueType.INVALID_REFERENCE,
                            canonicalId,
                            "Invalid reference '" + rawValue + "'"
                        )
                    );
                    invalidContent = true;
                    continue;
                }

                var referencedCanonical = refId.canonical();
                var referenced = tags.get(referencedCanonical);

                if (referenced == null) {
                    issues.add(
                        new TagResolveIssue(
                            TagResolveIssueType.UNKNOWN_TAG,
                            canonicalId,
                            "Unknown referenced tag '#" + referencedCanonical + "'"
                        )
                    );
                    invalidContent = true;
                    continue;
                }

                if (referenced.type() != definition.type()) {
                    issues.add(
                        new TagResolveIssue(
                            TagResolveIssueType.WRONG_TYPE_REFERENCE,
                            canonicalId,
                            "Tag '" + canonicalId + "' of type " + definition.type()
                                + " cannot reference '" + referencedCanonical + "' of type " + referenced.type()
                        )
                    );
                    invalidContent = true;
                    continue;
                }

                TagQueryResult<Set<String>> nested = resolveInternal(referencedCanonical, visiting);
                issues.addAll(nested.issues());

                if (nested.status() == TagQueryStatus.CIRCULAR_REFERENCE) {
                    circular = true;
                    continue;
                }

                if (nested.status() == TagQueryStatus.INVALID_CONTENT) {
                    invalidContent = true;
                }

                resolved.addAll(nested.value());
            } else {
                if (isValidGameId(definition.type(), rawValue)) {
                    resolved.add(rawValue);
                } else {
                    issues.add(
                        new TagResolveIssue(
                            TagResolveIssueType.INVALID_VALUE,
                            canonicalId,
                            "Invalid " + definition.type() + " id '" + rawValue + "'"
                        )
                    );
                    invalidContent = true;
                }
            }
        }

        visiting.remove(canonicalId);

        TagQueryStatus status;
        if (circular) {
            status = TagQueryStatus.CIRCULAR_REFERENCE;
        } else if (invalidContent) {
            status = resolved.isEmpty() ? TagQueryStatus.INVALID_CONTENT : TagQueryStatus.SUCCESS;
        } else if (resolved.isEmpty()) {
            status = TagQueryStatus.EMPTY;
        } else {
            status = TagQueryStatus.SUCCESS;
        }

        return TagQueryResult.of(
            status,
            Collections.unmodifiableSet(resolved),
            definition,
            issues
        );
    }

    /**
     * Returns the raw {@link TagDefinition} for the given ID, or {@code null} if no such tag has been registered.
     *
     * @param id the tag identifier to look up
     * @return the matching {@link TagDefinition}, or {@code null}
     */
    public TagDefinition get(String id) {
        var canonicalId = normalizeLookupId(id);
        if (canonicalId == null) {
            return null;
        }
        return tags.get(canonicalId);
    }

    /**
     * Returns {@code true} if a tag with the given ID is registered.
     *
     * @param id the tag identifier to check
     * @return {@code true} if the tag exists; {@code false} otherwise
     */
    public boolean contains(String id) {
        var canonicalId = normalizeLookupId(id);
        return canonicalId != null && tags.containsKey(canonicalId);
    }

    /**
     * Returns an unmodifiable view of all registered {@link TagDefinition} instances, in insertion order.
     *
     * @return an unmodifiable collection of all tag definitions; never {@code null}
     */
    public Collection<TagDefinition> all() {
        return Collections.unmodifiableCollection(tags.values());
    }

    /**
     * Returns whether the given concrete value is contained in the resolved value set of the specified tag.
     * <p>
     * The supplied tag ID is normalized to its canonical form before lookup. If the tag cannot be resolved
     * successfully, the returned {@link TagQueryResult} preserves the corresponding failure status and issues.
     *
     * @param tagId the tag ID to test against
     * @param value the concrete value to look up; must not be {@code null}
     * @return a {@link TagQueryResult} containing {@code true} if the value is present in the resolved tag set
     * @throws NullPointerException if {@code value} is {@code null}
     */
    public TagQueryResult<Boolean> containsValue(String tagId, String value) {
        Objects.requireNonNull(value, "value");

        TagQueryResult<Set<String>> resolved = resolve(tagId);
        if (!resolved.isSuccess()) {
            return TagQueryResult.of(resolved.status(), false, resolved.definition(), resolved.issues());
        }

        return TagQueryResult.of(
            resolved.status(),
            resolved.value().contains(value),
            resolved.definition(),
            resolved.issues()
        );
    }

    /**
     * Returns whether the given concrete value is contained in the resolved value set of the specified tag, requiring
     * that the tag match the supplied {@link TagType}.
     * <p>
     * The supplied tag ID is normalized to its canonical form before lookup. If the tag cannot be resolved
     * successfully, or the registered tag is of a different type, the returned {@link TagQueryResult} preserves the
     * corresponding failure status and issues.
     *
     * @param tagId        the tag ID to test against
     * @param expectedType the required tag type; must not be {@code null}
     * @param value        the concrete value to look up; must not be {@code null}
     * @return a {@link TagQueryResult} containing {@code true} if the value is present in the resolved tag set
     * @throws NullPointerException if {@code expectedType} or {@code value} is {@code null}
     */
    public TagQueryResult<Boolean> containsValue(String tagId, TagType expectedType, String value) {
        Objects.requireNonNull(expectedType, "expectedType");
        Objects.requireNonNull(value, "value");

        TagQueryResult<Set<String>> resolved = resolve(tagId, expectedType);
        if (!resolved.isSuccess()) {
            return TagQueryResult.of(resolved.status(), false, resolved.definition(), resolved.issues());
        }

        return TagQueryResult.of(
            resolved.status(),
            resolved.value().contains(value),
            resolved.definition(),
            resolved.issues()
        );
    }

    /**
     * Removes a tag by its identifier.
     * <p>
     * The provided ID is normalized before removal. If the ID cannot be normalized to a known canonical form, no action
     * is taken.
     * </p>
     *
     * @param id the tag identifier to remove
     */
    public synchronized void remove(String id) {
        var canonicalId = normalizeLookupId(id);
        if (canonicalId == null) {
            return;
        }

        tags.remove(canonicalId);
        resolvedCache.clear();
    }

    /**
     * Validates whether the given ID is a known valid game identifier for the specified tag type.
     *
     * @param type the tag type used to determine the valid ID set
     * @param id   the identifier to validate
     * @return {@code true} if the ID is non-blank and present in the corresponding valid ID set; {@code false}
     *         otherwise
     */
    private boolean isValidGameId(TagType type, String id) {
        if (id == null || id.isBlank()) {
            return false;
        }

        return switch (type) {
            case ITEM -> validItemIds.contains(id);
            case BLOCK -> validBlockIds.contains(id);
            case ENTITY -> validEntityIds.contains(id);
            case BIOME -> validBiomeIds.contains(id);
            case EFFECT -> validEffectIds.contains(id);
            case FLUID -> validFluidIds.contains(id);
            case DAMAGE_TYPE -> validDamageTypeIds.contains(id);
        };
    }
}
