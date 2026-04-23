package com.azuredoom.tagcore.data;

import java.util.List;
import java.util.Objects;

/**
 * Immutable data record representing a single tag definition as loaded from a JSON source.
 * <p>
 * A tag definition preserves both the original raw ID string read from disk and its parsed canonical {@link TagId}. It
 * also stores the declared {@link TagType}, the raw ordered list of values, and the {@link TagSource} the definition
 * was loaded from.
 * <p>
 * Values are either plain concrete identifiers (for example, {@code "Fish_Eel_Moray_Item"}) or tag references prefixed
 * with {@code #} (for example, {@code "#somemod:logs"}), which are resolved later by {@link TagRegistry}.
 *
 * @param rawId  the original tag ID string as read from the source data
 * @param id     the parsed canonical tag ID
 * @param type   the category of game object this tag describes
 * @param values the raw ordered list of concrete values and/or tag references declared by the source
 * @param source the source the tag definition was loaded from
 */
public record TagDefinition(
    String rawId,
    TagId id,
    TagType type,
    List<String> values,
    TagSource source
) {

    /**
     * Canonical compact constructor that validates required fields and stores an unmodifiable copy of {@code values}.
     *
     * @throws NullPointerException if {@code rawId}, {@code id}, {@code type}, or {@code source} is {@code null}
     */
    public TagDefinition {
        Objects.requireNonNull(rawId, "rawId");
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(type, "type");
        values = values == null ? List.of() : List.copyOf(values);
        Objects.requireNonNull(source, "source");
    }

    /**
     * Returns the canonical string form of this definition's {@link TagId}.
     *
     * @return the canonical tag ID string
     */
    public String canonicalId() {
        return id.canonical();
    }
}
