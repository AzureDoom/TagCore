package com.azuredoom.tagcore.data;

/**
 * Enumerates the possible outcomes of a tag lookup, resolution, or membership query.
 */
public enum TagQueryStatus {

    /**
     * The query completed successfully and produced one or more resolved values.
     */
    SUCCESS,

    /**
     * The query completed successfully but produced no resolved values.
     */
    EMPTY,

    /**
     * No registered tag matched the requested ID.
     */
    NOT_FOUND,

    /**
     * A matching tag was found, but its declared {@link TagType} did not match the expected type.
     */
    WRONG_TYPE,

    /**
     * The supplied tag ID was null, blank, or otherwise invalid and could not be normalized.
     */
    INVALID_TAG_ID,

    /**
     * The tag definition or one of its references contained invalid content.
     */
    INVALID_CONTENT,

    /**
     * A circular reference was detected while resolving the tag.
     */
    CIRCULAR_REFERENCE
}
