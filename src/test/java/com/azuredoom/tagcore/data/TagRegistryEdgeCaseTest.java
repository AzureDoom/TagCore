package com.azuredoom.tagcore.data;

import com.azuredoom.hytalecustomassetloader.model.AssetSource;
import com.azuredoom.hytalecustomassetloader.model.AssetSourceKind;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class TagRegistryEdgeCaseTest {

    @Test
    void resolveReturnsEmptyStatusForEmptyTag() {
        TagRegistry registry = new TagRegistry(
            Set.of("iron_ingot"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );

        registry.register(tag("empty_tag", List.of()));

        TagQueryResult<Set<String>> result = registry.resolve("empty_tag");

        assertEquals(TagQueryStatus.EMPTY, result.status());
        assertTrue(result.value().isEmpty());
        assertTrue(result.issues().isEmpty());
    }

    @Test
    void resolveReturnsSuccessButReportsIssueWhenNestedReferenceContainsMissingTag() {
        TagRegistry registry = new TagRegistry(
            Set.of("iron_ingot"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );

        registry.register(tag("child", List.of("iron_ingot", "#missing_child")));
        registry.register(tag("parent", List.of("#child")));

        TagQueryResult<Set<String>> result = registry.resolve("parent");

        assertEquals(TagQueryStatus.SUCCESS, result.status());
        assertEquals(Set.of("iron_ingot"), result.value());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.type() == TagResolveIssueType.UNKNOWN_TAG));
        assertTrue(result.issues().stream().anyMatch(issue -> issue.detail().contains("missing_child")));
    }

    @Test
    void resolveReturnsInvalidContentWhenAllNestedReferencesAreMissing() {
        TagRegistry registry = new TagRegistry(
            Set.of("iron_ingot"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );

        registry.register(tag("child", List.of("#missing_child")));
        registry.register(tag("parent", List.of("#child")));

        TagQueryResult<Set<String>> result = registry.resolve("parent");

        assertEquals(TagQueryStatus.INVALID_CONTENT, result.status());
        assertTrue(result.value().isEmpty());
        assertTrue(result.issues().stream().anyMatch(issue -> issue.type() == TagResolveIssueType.UNKNOWN_TAG));
    }

    private static TagDefinition tag(String id, List<String> values) {
        return new TagDefinition(
            id,
            TagId.parse(id),
            TagType.ITEM,
            values,
            new AssetSource(AssetSourceKind.CLASSPATH_DIRECTORY, "tests/" + id + ".json")
        );
    }
}
