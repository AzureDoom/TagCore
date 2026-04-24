package com.azuredoom.tagcore.data;

import org.checkerframework.checker.nullness.compatqual.NonNullType;

import java.util.Locale;

/**
 * Immutable identifier for a tag, consisting of a namespace and path.
 * <p>
 * Tag IDs may be written either in fully qualified form (for example, {@code "somemod:logs"}) or as bare IDs (for
 * example, {@code "Fish_Eel_Moray_Item"}). Bare IDs are normalized using {@link #DEFAULT_NAMESPACE}.
 *
 * @param namespace the namespace component of the tag ID
 * @param path      the path component of the tag ID
 */
public record TagId(
    String namespace,
    String path
) {

    public static final String DEFAULT_NAMESPACE = "hytale";

    /**
     * Canonical compact constructor that validates the path and normalizes the namespace and path values.
     * <p>
     * A null or blank namespace is replaced with {@link #DEFAULT_NAMESPACE}. Non-blank namespaces are trimmed and
     * normalized to lowercase. The path is trimmed but otherwise preserved.
     *
     * @throws IllegalArgumentException if {@code path} is {@code null} or blank
     */
    public TagId {
        if (path == null || path.isBlank()) {
            throw new IllegalArgumentException("Tag path cannot be null or blank");
        }

        namespace = namespace == null || namespace.isBlank()
            ? DEFAULT_NAMESPACE
            : namespace.trim().toLowerCase(Locale.ROOT);

        path = path.trim();
    }

    /**
     * Parses a raw tag ID string into a {@link TagId}.
     * <p>
     * If the input does not contain a namespace separator ({@code :}), the returned ID uses {@link #DEFAULT_NAMESPACE}.
     *
     * @param raw the raw tag ID string to parse
     * @return the parsed {@link TagId}
     * @throws IllegalArgumentException if {@code raw} is {@code null} or blank
     */
    public static TagId parse(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Tag id cannot be null");
        }

        var value = raw.trim();
        if (value.isEmpty()) {
            throw new IllegalArgumentException("Tag id cannot be blank");
        }

        var colon = value.indexOf(':');
        if (colon < 0) {
            return new TagId(DEFAULT_NAMESPACE, value);
        }

        var namespace = value.substring(0, colon).trim();
        var path = value.substring(colon + 1).trim();
        return new TagId(namespace, path);
    }

    /**
     * Returns the canonical string form of this tag ID.
     *
     * @return the canonical {@code namespace:path} representation
     */
    public String canonical() {
        return namespace + ":" + path;
    }

    /**
     * Returns whether the supplied raw input string was written without an explicit namespace separator.
     *
     * @param raw the raw tag ID string to inspect
     * @return {@code true} if {@code raw} is non-null and does not contain {@code :}; {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public boolean wasBareInput(String raw) {
        return raw != null && !raw.contains(":");
    }

    /**
     * Returns the canonical string form of this tag ID.
     *
     * @return the canonical {@code namespace:path} representation
     */
    @Override
    @NonNullType
    public String toString() {
        return canonical();
    }
}
