package com.azuredoom.tagcore.data;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagRegistryTest {

    @Test
    void resolveFlattensNestedReferences() {
        TagRegistry registry = new TagRegistry(
            Set.of("oak_log", "birch_log", "spruce_log"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );

        registry.register(tag("common_logs", TagType.ITEM, List.of("oak_log", "birch_log")));
        registry.register(tag("all_logs", TagType.ITEM, List.of("#common_logs", "spruce_log")));

        TagQueryResult<Set<String>> result = registry.resolve("all_logs");

        assertTrue(result.isSuccess());
        assertEquals(Set.of("oak_log", "birch_log", "spruce_log"), result.value());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void resolveReturnsInvalidContentForWrongTypeReference() {
        TagRegistry registry = new TagRegistry(
            Set.of("oak_log"),
            Set.of("oak_log_block"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );

        registry.register(tag("block_logs", TagType.BLOCK, List.of("oak_log_block")));
        registry.register(tag("item_logs", TagType.ITEM, List.of("#block_logs")));

        TagQueryResult<Set<String>> result = registry.resolve("item_logs");

        assertEquals(TagQueryStatus.INVALID_CONTENT, result.status());
        assertTrue(result.value().isEmpty());
        assertTrue(
            result.issues().stream().anyMatch(issue -> issue.type() == TagResolveIssueType.WRONG_TYPE_REFERENCE)
        );
    }

    @Test
    void resolveDetectsCircularReferences() {
        TagRegistry registry = new TagRegistry(
            Set.of("oak_log"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );

        registry.register(tag("a", TagType.ITEM, List.of("#b")));
        registry.register(tag("b", TagType.ITEM, List.of("#a")));

        TagQueryResult<Set<String>> result = registry.resolve("a");

        assertEquals(TagQueryStatus.CIRCULAR_REFERENCE, result.status());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.type() == TagResolveIssueType.CIRCULAR_REFERENCE));
    }

    @Test
    void containsValueWithExpectedTypeRejectsWrongTagType() {
        TagRegistry registry = new TagRegistry(
            Set.of(),
            Set.of("oak_log_block"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );

        registry.register(tag("log_blocks", TagType.BLOCK, List.of("oak_log_block")));

        TagQueryResult<Boolean> result = registry.containsValue("log_blocks", TagType.ITEM, "oak_log_block");

        assertEquals(TagQueryStatus.WRONG_TYPE, result.status());
        assertFalse(result.value());
    }

    private static TagDefinition tag(String id, TagType type, List<String> values) {
        return new TagDefinition(
            id,
            TagId.parse(id),
            type,
            values,
            new TagSource(TagSourceKind.CLASSPATH_DIRECTORY, "tests/" + id + ".json")
        );
    }
}
