package com.azuredoom.tagcore.data;

/**
 * Enumerates the categories of issues that may be encountered while looking up or resolving tags.
 */
public enum TagResolveIssueType {

    /**
     * A referenced or requested tag could not be found.
     */
    UNKNOWN_TAG,

    /**
     * A tag reference pointed to a tag of a different {@link TagType}.
     */
    WRONG_TYPE_REFERENCE,

    /**
     * A concrete value in a tag definition was invalid for the tag's declared {@link TagType}.
     */
    INVALID_VALUE,

    /**
     * A circular reference was detected while resolving one or more tag references.
     */
    CIRCULAR_REFERENCE,

    /**
     * A supplied external lookup ID was invalid and could not be normalized.
     */
    INVALID_LOOKUP_ID,

    /**
     * A raw {@code #reference} entry was malformed and could not be parsed as a tag ID.
     */
    INVALID_REFERENCE
}
