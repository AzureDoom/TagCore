package com.azuredoom.tagcore.compat;

import org.herolias.tooltips.api.DynamicTooltipsApiProvider;

import com.azuredoom.tagcore.TagCoreMod;

/**
 * Integration layer for registering TagCore tooltip providers with DynamicTooltipsLib.
 * <p>
 * This class handles provider registration and refresh behavior, ensuring that tag-based tooltips are properly exposed
 * and updated for players.
 * </p>
 */
public class DynamicTooltipsLibCompat {

    private static boolean registered = false;

    /**
     * Registers the TagCore tooltip provider with DynamicTooltipsLib.
     * <p>
     * This method is safe to call multiple times but will only register the provider once.
     * </p>
     * <p>
     * If DynamicTooltipsLib is not available, registration is skipped and an error is logged.
     * </p>
     */
    public static synchronized void register() {
        if (registered) {
            return;
        }

        var api = DynamicTooltipsApiProvider.get();
        if (api == null) {
            TagCoreMod.severeLog("DynamicTooltipsLib is not installed!");
            return;
        }

        api.registerProvider(new TagTooltipProvider());
        registered = true;
        TagCoreMod.infoLog("Registered TagCore DynamicTooltipsLib provider.");
    }

    /**
     * Refreshes all tooltips for all players.
     * <p>
     * This invalidates cached tooltip data and forces clients to rebuild tooltips using the latest tag state.
     * </p>
     */
    public static void refresh() {
        var api = DynamicTooltipsApiProvider.get();
        if (api == null) {
            return;
        }

        api.invalidateAll();
        api.refreshAllPlayers();
    }
}
