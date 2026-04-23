package com.azuredoom.tagcore;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

import com.azuredoom.tagcore.api.TagService;
import com.azuredoom.tagcore.data.TagDefinition;
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
        TagCoreMod.tagService = new TagService(tagRegistry);
        infoLog("TagCore loaded " + tagRegistry.all().size() + " tags.");
        infoLog("TagCore initialized!");
    }

    @Override
    protected void setup() {
        infoLog("Setting up TagCore!");
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
     * Returns the tag registry populated during {@link #start()}.
     * <p>
     * The registry provides direct, low-level access to all registered {@link TagDefinition} instances and their
     * resolved value sets.
     *
     * @return the loaded {@link TagRegistry}; never {@code null} after {@link #start()} completes
     */
    @SuppressWarnings("unused")
    public TagRegistry getTagRegistry() {
        return tagRegistry;
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
}
