package com.azuredoom.tagcore.data;

/**
 * Immutable description of a single issue encountered while looking up or resolving a tag.
 *
 * @param type   the category of issue that occurred
 * @param tagId  the tag ID associated with the issue, when available
 * @param detail the human-readable detail message describing the issue
 */
public record TagResolveIssue(
    TagResolveIssueType type,
    String tagId,
    String detail
) {}
