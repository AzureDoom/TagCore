package com.azuredoom.tagcore.data;

import java.util.List;

/**
 * Immutable result wrapper describing the outcome of a tag query or resolution operation.
 * <p>
 * A query result includes a {@link TagQueryStatus}, the resolved value when available, the matching
 * {@link TagDefinition} when one was found, and any {@link TagResolveIssue} instances encountered while processing the
 * query.
 *
 * @param <T>        the value type carried by the result
 * @param status     the overall outcome of the query
 * @param value      the query result value
 * @param definition the matching tag definition when available
 * @param issues     the issues encountered while processing the query
 */
public record TagQueryResult<T>(
    TagQueryStatus status,
    T value,
    TagDefinition definition,
    List<TagResolveIssue> issues
) {

    /**
     * Returns whether this result represents a successful query outcome.
     * <p>
     * Both {@link TagQueryStatus#SUCCESS} and {@link TagQueryStatus#EMPTY} are treated as successful outcomes.
     *
     * @return {@code true} if the query completed successfully; {@code false} otherwise
     */
    public boolean isSuccess() {
        return status == TagQueryStatus.SUCCESS || status == TagQueryStatus.EMPTY;
    }

    /**
     * Returns whether this result represents a successful query that resolved to no values.
     *
     * @return {@code true} if the result status is {@link TagQueryStatus#EMPTY}; {@code false} otherwise
     */
    @SuppressWarnings("unused")
    public boolean isEmpty() {
        return status == TagQueryStatus.EMPTY;
    }

    /**
     * Creates a new {@link TagQueryResult}, storing an unmodifiable copy of {@code issues}.
     *
     * @param status     the overall outcome of the query
     * @param value      the query result value
     * @param definition the matching tag definition when available
     * @param issues     the issues encountered while processing the query
     * @param <T>        the value type carried by the result
     * @return a new {@link TagQueryResult}
     */
    public static <T> TagQueryResult<T> of(
        TagQueryStatus status,
        T value,
        TagDefinition definition,
        List<TagResolveIssue> issues
    ) {
        return new TagQueryResult<>(status, value, definition, issues == null ? List.of() : List.copyOf(issues));
    }
}
