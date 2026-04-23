package com.azuredoom.tagcore.compat;

import com.hypixel.hytale.server.core.asset.type.item.config.Item;
import org.herolias.tooltips.api.DynamicTooltipsApiProvider;

import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import com.azuredoom.tagcore.TagCoreMod;
import com.azuredoom.tagcore.api.TagService;
import com.azuredoom.tagcore.data.TagType;

public class DynamicTooltipsLibCompat {

    private static boolean registered = false;

    private static final Timer SCAN_TIMER = new Timer("tagscore-dynamic-tooltips", true);

    private static final Set<String> processedItems = ConcurrentHashMap.newKeySet();

    /**
     * Registers the compatibility layer for the DynamicTooltipsLib API.
     * <p>
     * This method initializes a delayed background scan that iterates over all known item assets and attaches tag-based
     * tooltip lines using the DynamicTooltipsLib API.
     * <p>
     * If DynamicTooltipsLib is not installed or the {@link TagService} is not available, the registration process is
     * aborted and an error is logged.
     * <p>
     * Each item is processed at most once, and only items with resolved {@link TagType#ITEM} tag memberships will
     * receive tooltip entries.
     */
    public static void register() {
        if (registered)
            return;
        registered = true;

        var api = DynamicTooltipsApiProvider.get();
        if (api == null) {
            TagCoreMod.severeLog("DynamicTooltipsLib is not installed!");
            return;
        }
        var service = TagCoreMod.getTagService();
        if (service == null) {
            TagCoreMod.severeLog("TagService is not available!");
            return;
        }

        SCAN_TIMER.schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    var allItems = Item.getAssetMap().getAssetMap().values();

                    for (var item : allItems) {
                        var itemId = item.getId();
                        if (itemId == null || itemId.isBlank() || processedItems.contains(itemId)) {
                            continue;
                        }

                        var result = service.getTagsForValue(TagType.ITEM, itemId);
                        if (!result.isSuccess() || result.value().isEmpty()) {
                            processedItems.add(itemId);
                            continue;
                        }

                        api.addGlobalLine(
                            itemId,
                            buildTagsTooltip(result.value())
                        );

                        processedItems.add(itemId);
                    }
                } catch (Exception e) {
                    TagCoreMod.severeLog("Failed to register DynamicTooltipsLib item tag tooltips: " + e.getMessage());
                }
            }
        }, 10000L);
    }

    /**
     * Builds a formatted tooltip string listing the provided tag identifiers.
     * <p>
     * The returned string is formatted using color tags compatible with the DynamicTooltipsLib rendering system and is
     * intended to be appended to item tooltips.
     *
     * @param tags the set of tag identifiers to include in the tooltip
     * @return a formatted tooltip string containing all provided tags
     */
    private static String buildTagsTooltip(Set<String> tags) {
        var sb = new StringBuilder();

        sb.append("\n<color is=\"#b5a077\">Tags:</color>");

        for (var tag : tags) {
            sb.append("\n<color is=\"#d4c08a\">")
                .append(tag)
                .append("</color>");
        }

        return sb.toString();
    }
}
