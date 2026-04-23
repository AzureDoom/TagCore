package com.azuredoom.tagcore.data;

/**
 * Represents the category of game object that a tag applies to.
 * <p>
 * Every {@link TagDefinition} must declare exactly one {@code TagType}, and tags may only reference other tags of the
 * same type. For example, an {@code ITEM} tag cannot include a reference ({@code #prefix}) to a {@code BLOCK} tag.
 * <p>
 * The string names used in JSON ({@code "item"}, {@code "block"}, {@code "entity"}) are case-insensitive and
 * leading/trailing whitespace is ignored during parsing.
 */
public enum TagType {

    ITEM,
    BLOCK,
    ENTITY,
    BIOME,
    EFFECT,
    FLUID,
    DAMAGE_TYPE;

    /**
     * Parses a {@code TagType} from its JSON string representation.
     * <p>
     * Accepted values (case-insensitive, whitespace-trimmed):
     * <ul>
     * <li>{@code "item"} → {@link #ITEM}</li>
     * <li>{@code "block"} → {@link #BLOCK}</li>
     * <li>{@code "entity"} → {@link #ENTITY}</li>
     * <li>{@code "biome"} → {@link #BIOME}</li>
     * <li>{@code "effect"} → {@link #EFFECT}</li>
     * <li>{@code "fluid"} → {@link #FLUID}</li>
     * <li>{@code "damage_type"} → {@link #DAMAGE_TYPE}</li>
     * </ul>
     *
     * @param value the raw string value read from a tag JSON file
     * @return the matching {@code TagType}
     * @throws IllegalArgumentException if {@code value} is {@code null} or does not match any known type name
     */
    public static TagType fromJson(String value) {
        if (value == null) {
            throw new IllegalArgumentException("Tag type cannot be null");
        }

        return switch (value.trim().toLowerCase()) {
            case "item" -> ITEM;
            case "block" -> BLOCK;
            case "entity" -> ENTITY;
            case "biome" -> BIOME;
            case "effect" -> EFFECT;
            case "fluid" -> FLUID;
            case "damage_type" -> DAMAGE_TYPE;
            default -> throw new IllegalArgumentException("Unknown tag type: " + value);
        };
    }
}
