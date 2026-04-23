package com.azuredoom.tagcore.data;

/**
 * Enumerates the supported source categories that tag definitions may be loaded from.
 */
public enum TagSourceKind {

    /**
     * A tag definition loaded from an exploded classpath {@code tags/} directory.
     */
    CLASSPATH_DIRECTORY,

    /**
     * A tag definition loaded from a packaged JAR on the classpath.
     */
    CLASSPATH_JAR,

    /**
     * A tag definition loaded from an external ZIP asset pack.
     */
    EXTERNAL_ZIP,

    /**
     * A tag definition loaded from an external JAR asset pack.
     */
    EXTERNAL_JAR
}
