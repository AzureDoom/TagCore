package com.azuredoom.tagcore.data;

/**
 * Immutable description of the source a {@link TagDefinition} was loaded from.
 *
 * @param kind     the category of source the definition originated from
 * @param location the human-readable source location, such as a resource path or archive entry
 */
public record TagSource(
    TagSourceKind kind,
    String location
) {}
