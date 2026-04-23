package com.azuredoom.tagcore;

import com.hypixel.hytale.common.plugin.PluginIdentifier;
import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;
import com.hypixel.hytale.server.core.plugin.PluginManager;

import com.azuredoom.tagcore.api.TagService;
import com.azuredoom.tagcore.command.ReloadTagsCommand;
import com.azuredoom.tagcore.compat.DynamicTooltipsLibCompat;
import com.azuredoom.tagcore.data.TagRegistry;

public class TagCoreMod extends JavaPlugin {

    public static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    private TagRegistry tagRegistry;

    private static TagService tagService;

    public TagCoreMod(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void start() {
        infoLog("Starting TagCore!");
        this.tagRegistry = new TagBootstrap(this).bootstrap();
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
     * Rebuilds the {@link TagRegistry} from all available tag sources and updates the active {@link TagService} to use
     * the newly constructed registry.
     * <p>
     * This method performs a full reload of tag data by invoking {@link TagBootstrap#bootstrap()}, replacing the
     * current registry instance, and swapping the backing registry used by the shared {@link TagService}. Any existing
     * references to {@link TagService} will continue to function and will observe the updated tag data after the swap.
     * <p>
     * This method is synchronized to ensure that registry replacement is atomic and safe when accessed concurrently.
     *
     * @return the total number of tags loaded into the new registry
     */
    public synchronized int reloadTags() {
        var newRegistry = new TagBootstrap(this).bootstrap();
        this.tagRegistry = newRegistry;

        if (TagCoreMod.tagService == null) {
            TagCoreMod.tagService = new TagService(newRegistry);
        } else {
            TagCoreMod.tagService.swapRegistry(newRegistry);
        }

        var count = newRegistry.all().size();
        infoLog("Reloaded " + count + " tags.");
        return count;
    }
}
