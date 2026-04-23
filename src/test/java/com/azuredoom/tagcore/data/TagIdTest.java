package com.azuredoom.tagcore.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TagIdTest {

    @Test
    void parseBareIdUsesDefaultNamespace() {
        TagId id = TagId.parse("Fish_Eel_Moray_Item");

        assertEquals(TagId.DEFAULT_NAMESPACE, id.namespace());
        assertEquals("Fish_Eel_Moray_Item", id.path());
        assertEquals("hytale:Fish_Eel_Moray_Item", id.canonical());
    }

    @Test
    void parseNamespacedIdNormalizesNamespaceAndTrimsWhitespace() {
        TagId id = TagId.parse("  AzureDoom:logs  ");

        assertEquals("azuredoom", id.namespace());
        assertEquals("logs", id.path());
        assertEquals("azuredoom:logs", id.toString());
    }

    @Test
    void parseRejectsBlankInput() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> TagId.parse("   "));
        assertTrue(ex.getMessage().toLowerCase().contains("blank"));
    }
}
