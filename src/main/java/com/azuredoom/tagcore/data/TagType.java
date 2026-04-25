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
    DAMAGE_TYPE,
    SOUND,
    ENVIRONMENT,
    ITEM_CATEGORY,
    ROOT_INTERACTION,
    EMOTE,
    ENTITY_STAT_TYPE,
    PARTICLE;

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
     * <li>{@code "sound"} → {@link #SOUND}</li>
     * <li>{@code "enviorment"} → {@link #ENVIRONMENT}</li>
     * <li>{@code "item_category"} → {@link #ITEM_CATEGORY}</li>
     * <li>{@code "root_interaction"} → {@link #ROOT_INTERACTION}</li>
     * <li>{@code "emote"} → {@link #EMOTE}</li>
     * <li>{@code "entity_stat_type"} → {@link #ENTITY_STAT_TYPE}</li>
     * <li>{@code "particle"} → {@link #PARTICLE}</li>
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
            case "sound" -> SOUND;
            case "environment" -> ENVIRONMENT;
            case "item_category" -> ITEM_CATEGORY;
            case "root_interaction" -> ROOT_INTERACTION;
            case "emote" -> EMOTE;
            case "entity_stat_type" -> ENTITY_STAT_TYPE;
            case "particle" -> PARTICLE;
            default -> throw new IllegalArgumentException("Unknown tag type: " + value);
        };
    }
}
