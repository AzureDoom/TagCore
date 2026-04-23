package com.azuredoom.tagcore.data;

/**
 * Summary of a tag reload operation.
 *
 * @param total   the total number of tags present after reload
 * @param added   the number of tags added during reload
 * @param updated the number of tags updated during reload
 * @param removed the number of tags removed during reload
 */
public record ReloadSummary(
    int total,
    int added,
    int updated,
    int removed
) {}
