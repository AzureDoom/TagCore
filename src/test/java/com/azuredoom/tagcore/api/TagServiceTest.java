package com.azuredoom.tagcore.api;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import com.azuredoom.tagcore.data.TagDefinition;
import com.azuredoom.tagcore.data.TagId;
import com.azuredoom.tagcore.data.TagQueryResult;
import com.azuredoom.tagcore.data.TagQueryStatus;
import com.azuredoom.tagcore.data.TagRegistry;
import com.azuredoom.tagcore.data.TagSource;
import com.azuredoom.tagcore.data.TagSourceKind;
import com.azuredoom.tagcore.data.TagType;

import static org.junit.jupiter.api.Assertions.*;

class TagServiceTest {

    @Test
    void resolveItemTagUsesTypedResolution() {
        TagRegistry registry = new TagRegistry(
            Set.of("oak_log", "birch_log"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );
        registry.register(tag("logs", TagType.ITEM, List.of("oak_log", "birch_log")));

        TagService service = new TagService(registry);
        TagQueryResult<Set<String>> result = service.resolveItemTag("logs");

        assertEquals(TagQueryStatus.SUCCESS, result.status());
        assertEquals(Set.of("oak_log", "birch_log"), result.value());
    }

    @Test
    void isInBlockTagChecksMembership() {
        TagRegistry registry = new TagRegistry(
            Set.of(),
            Set.of("oak_log_block"),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of()
        );
        registry.register(tag("logs", TagType.BLOCK, List.of("oak_log_block")));

        TagService service = new TagService(registry);
        TagQueryResult<Boolean> result = service.isInBlockTag("logs", "oak_log_block");

        assertEquals(TagQueryStatus.SUCCESS, result.status());
        assertTrue(result.value());
    }

    @Test
    void isInEffectTagUsesEffectType() {
        TagRegistry registry = new TagRegistry(
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of(),
            Set.of("speed"),
            Set.of(),
            Set.of()
        );
        registry.register(tag("beneficial_effects", TagType.EFFECT, List.of("speed")));

        TagService service = new TagService(registry);
        TagQueryResult<Boolean> result = service.isInEffectTag("beneficial_effects", "speed");

        assertEquals(TagQueryStatus.SUCCESS, result.status());
        assertTrue(result.value());
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
