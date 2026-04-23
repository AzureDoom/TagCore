package com.azuredoom.tagcore.compat;

import org.checkerframework.checker.nullness.compatqual.NonNullDecl;
import org.herolias.tooltips.api.TooltipData;
import org.herolias.tooltips.api.TooltipPriority;
import org.herolias.tooltips.api.TooltipProvider;

import java.util.ArrayList;
import javax.annotation.Nonnull;

import com.azuredoom.tagcore.TagCoreMod;
import com.azuredoom.tagcore.data.TagType;

/**
 * Tooltip provider that displays item tags using the TagCore tag system.
 * <p>
 * This provider integrates with DynamicTooltipsLib and generates tooltip lines for item IDs based on the tags resolved
 * by the active {@code TagService}.
 * </p>
 */
public final class TagTooltipProvider implements TooltipProvider {

    public static final String PROVIDER_ID = "tagcore:item-tags";

    @Override
    public @Nonnull String getProviderId() {
        return PROVIDER_ID;
    }

    @Override
    public int getPriority() {
        return TooltipPriority.DEFAULT;
    }

    @Override
    public TooltipData getTooltipData(@NonNullDecl String itemId, String metadata) {
        return getTooltipData(itemId, metadata, null);
    }

    @Override
    public TooltipData getTooltipData(@NonNullDecl String itemId, String metadata, String locale) {
        if (itemId.isBlank()) {
            return null;
        }

        var service = TagCoreMod.getTagService();
        if (service == null) {
            return null;
        }

        var result = service.getTagsForValue(TagType.ITEM, itemId);
        if (!result.isSuccess() || result.value().isEmpty()) {
            return null;
        }

        var lines = new ArrayList<String>();
        lines.add("<color is=\"#b5a077\">Tags:</color>");

        for (var tag : result.value()) {
            lines.add("<color is=\"#d4c08a\">" + tag + "</color>");
        }

        return TooltipData.builder()
            .hashInput(buildHashInput(itemId, result.value()))
            .addLines(lines)
            .build();
    }

    /**
     * Builds a stable hash input string for tooltip caching.
     * <p>
     * The hash incorporates the item ID and all associated tags to ensure tooltip updates when tag membership changes.
     * </p>
     *
     * @param itemId the item identifier
     * @param tags   the associated tags
     * @return a hash input string used for tooltip caching
     */
    private String buildHashInput(String itemId, Iterable<String> tags) {
        var sb = new StringBuilder("tagcore|").append(itemId);
        for (var tag : tags) {
            sb.append('|').append(tag);
        }
        return sb.toString();
    }
}
