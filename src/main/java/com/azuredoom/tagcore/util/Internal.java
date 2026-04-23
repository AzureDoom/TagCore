package com.azuredoom.tagcore.util;

import java.lang.annotation.*;

/**
 * Marks APIs that are internal to TagCore and should not be used by external plugins.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(
    {
        ElementType.TYPE,
        ElementType.METHOD,
        ElementType.CONSTRUCTOR,
        ElementType.FIELD
    }
)
public @interface Internal {

    String value() default "Internal API. Do not use.";
}
