package com.azuredoom.tagcore;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;

import com.azuredoom.tagcore.api.TagService;
import com.azuredoom.tagcore.command.ReloadTagsCommand;
import com.azuredoom.tagcore.compat.DynamicTooltipsLibCompat;
import com.azuredoom.tagcore.data.ReloadSummary;
import com.azuredoom.tagcore.data.TagRegistry;

public class TagCoreMod extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private TagBootstrap tagBootstrap;

    private TagRegistry tagRegistry;

    private static TagService tagService;

    public TagCoreMod(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        infoLog("Starting TagCore!");
        this.tagBootstrap = new TagBootstrap(this);
        this.tagRegistry = this.tagBootstrap.bootstrap();

        if (TagCoreMod.tagService == null) {
            TagCoreMod.tagService = new TagService(this.tagRegistry);
        } else {
            TagCoreMod.tagService.swapRegistry(this.tagRegistry);
        }
        infoLog("TagCore loaded " + tagRegistry.all().size() + " tags.");
        if (PluginManager.get().getPlugin(new PluginIdentifier("org.herolias", "DynamicTooltipsLib")) != null) {
            DynamicTooltipsLibCompat.register();
            infoLog("DynamicTooltipsLib Compat registered");
        }
        infoLog("TagCore initialized!");
    }

    @Override
    protected void setup() {
        infoLog("Setting up TagCore!");
        getCommandRegistry().registerCommand(new ReloadTagsCommand(this));
        infoLog("TagCore reload command registered!");
    }

    @Override
    protected void shutdown() {
        infoLog("Shutting down TagCore!");
    }

    /**
     * Logs a message at the SEVERE level using the application's logger.
     *
     * @param message the message to log at the SEVERE level
     */
    @SuppressWarnings("unused")
    public static void severeLog(String message) {
        LOGGER.atSevere().log(message);
    }

    /**
     * Logs a message at the INFO level using the application's logger.
     *
     * @param message the message to log at the INFO level
     */
    public static void infoLog(String message) {
        LOGGER.atInfo().log(message);
    }

    /**
     * Logs a message at the WARNING level using the application's logger.
     *
     * @param message the message to log at the WARNING level
     */
    public static void warnLog(String message) {
        LOGGER.atWarning().log(message);
    }

    /**
     * Returns the tag service populated during {@link #start()}.
     * <p>
     * The service is the recommended API entry point for other plugins, providing type-safe lookup and membership
     * checks over the underlying {@link TagRegistry}.
     *
     * @return the {@link TagService}; never {@code null} after {@link #start()} completes
     */
    public static TagService getTagService() {
        return tagService;
    }

    /**
     * Reloads all tag assets and updates the active tag registry and service.
     *
     * @return a summary describing the total loaded tags and the number of added, updated, and removed tags detected
     *         during reload
     * @throws IllegalStateException if the tag bootstrap has not been initialized
     */
    public synchronized ReloadSummary reloadTags() {
        if (this.tagBootstrap == null) {
            throw new IllegalStateException("Tag bootstrap was not initialized.");
        }

        var result = this.tagBootstrap.reload();
        this.tagRegistry = this.tagBootstrap.registry();

        if (TagCoreMod.tagService == null) {
            TagCoreMod.tagService = new TagService(this.tagRegistry);
        } else {
            TagCoreMod.tagService.swapRegistry(this.tagRegistry);
        }

        if (PluginManager.get().getPlugin(new PluginIdentifier("org.herolias", "DynamicTooltipsLib")) != null) {
            DynamicTooltipsLibCompat.refresh();
        }

        return new ReloadSummary(
            result.currentSnapshot().mergedAssets().size(),
            result.diff().added().size(),
            result.diff().updated().size(),
            result.diff().removed().size()
        );
    }
}
