package com.azuredoom.tagcore;

import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.azuredoom.tagcore.data.TagDefinition;
import com.azuredoom.tagcore.data.TagId;
import com.azuredoom.tagcore.data.TagSourceKind;
import com.azuredoom.tagcore.data.TagType;

import static org.junit.jupiter.api.Assertions.*;

class TagBootstrapTest {

    @Test
    void loadTagParsesJsonFromTestResources() throws Exception {
        TagBootstrap bootstrap = bootstrap();

        try (InputStream in = getClass().getResourceAsStream("/tags/item-tools.json")) {
            assertNotNull(in, "Missing test resource /tags/item-tools.json");

            TagDefinition definition = (TagDefinition) invoke(
                bootstrap,
                "loadTag",
                new Class<?>[] { InputStream.class, String.class, TagSourceKind.class },
                in,
                "tags/item-tools.json",
                TagSourceKind.CLASSPATH_DIRECTORY
            );

            assertEquals("tagcore:test_tools", definition.canonicalId());
            assertEquals(TagType.ITEM, definition.type());
            assertEquals(List.of("iron_pickaxe", "diamond_pickaxe"), definition.values());
            assertEquals(TagSourceKind.CLASSPATH_DIRECTORY, definition.source().kind());
            assertEquals("tags/item-tools.json", definition.source().location());
        }
    }

    @Test
    void loadTagRejectsMalformedJson() throws Exception {
        TagBootstrap bootstrap = bootstrap();

        try (InputStream in = getClass().getResourceAsStream("/tags/malformed-json.json")) {
            assertNotNull(in, "Missing test resource /tags/malformed-json.json");

            RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> invoke(
                    bootstrap,
                    "loadTag",
                    new Class<?>[] { InputStream.class, String.class, TagSourceKind.class },
                    in,
                    "tags/malformed-json.json",
                    TagSourceKind.CLASSPATH_DIRECTORY
                )
            );

            assertTrue(ex.getMessage().contains("Failed to load tag resource tags/malformed-json.json"));
        }
    }

    @Test
    void loadTagRejectsMissingRequiredFields() throws Exception {
        TagBootstrap bootstrap = bootstrap();

        try (InputStream in = getClass().getResourceAsStream("/tags/missing-type.json")) {
            assertNotNull(in, "Missing test resource /tags/missing-type.json");

            RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> invoke(
                    bootstrap,
                    "loadTag",
                    new Class<?>[] { InputStream.class, String.class, TagSourceKind.class },
                    in,
                    "tags/missing-type.json",
                    TagSourceKind.CLASSPATH_DIRECTORY
                )
            );

            assertTrue(ex.getMessage().contains("Failed to load tag resource tags/missing-type.json"));
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause().getMessage().contains("Missing 'type'"));
        }
    }

    private static TagBootstrap bootstrap() {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            java.lang.reflect.Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            Object unsafe = f.get(null);

            java.lang.reflect.Method allocateInstance =
                unsafeClass.getMethod("allocateInstance", Class.class);

            return (TagBootstrap) allocateInstance.invoke(unsafe, TagBootstrap.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to instantiate TagBootstrap for tests", e);
        }
    }

    private static TagDefinition tag(String id, List<String> values, TagSourceKind kind, String location) {
        String namespaced = id.contains(":") ? id : "tagcore:" + id;
        return new TagDefinition(
            id,
            TagId.parse(namespaced),
            TagType.ITEM,
            values,
            new com.azuredoom.tagcore.data.TagSource(kind, location)
        );
    }

    private static Object invoke(
        Object target,
        String methodName,
        Class<?>[] parameterTypes,
        Object... args
    ) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName, parameterTypes);
        method.setAccessible(true);
        try {
            return method.invoke(target, args);
        } catch (java.lang.reflect.InvocationTargetException e) {
            if (e.getCause() instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }
    }

    private static void writeArchiveEntry(Path archive, String entryName, String json) throws Exception {
        try (ZipOutputStream out = new ZipOutputStream(Files.newOutputStream(archive), StandardCharsets.UTF_8)) {
            out.putNextEntry(new ZipEntry(entryName));
            out.write(json.getBytes(StandardCharsets.UTF_8));
            out.closeEntry();
        }
    }
}
