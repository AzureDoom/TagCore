package com.azuredoom.tagcore.command;

import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.server.core.command.system.CommandContext;
import com.hypixel.hytale.server.core.command.system.basecommands.AbstractPlayerCommand;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import org.checkerframework.checker.nullness.compatqual.NonNullDecl;

import com.azuredoom.tagcore.TagCoreMod;
import com.azuredoom.tagcore.data.TranslationKeys;
import com.azuredoom.tagcore.util.TranslationUtil;

public class ReloadTagsCommand extends AbstractPlayerCommand {

    private final TagCoreMod plugin;

    public ReloadTagsCommand(TagCoreMod plugin) {
        super("reload_tags", "Reloads the tags from the server");
        this.plugin = plugin;
        //this.requirePermission("tagcore.command.reloadtags");
    }

    @Override
    protected void execute(
        @NonNullDecl CommandContext commandContext,
        @NonNullDecl Store<EntityStore> store,
        @NonNullDecl Ref<EntityStore> ref,
        @NonNullDecl PlayerRef playerRef,
        @NonNullDecl World world
    ) {
        try {
            var summary = this.plugin.reloadTags();

            commandContext.sendMessage(
                TranslationUtil.translate(
                    TranslationKeys.RELOAD_SUCCESS,
                    msg -> msg.param("total", summary.total())
                        .param("added", summary.added())
                        .param("updated", summary.updated())
                        .param("removed", summary.removed())
                )
            );
        } catch (Exception e) {
            TagCoreMod.warnLog("Failed to reload tags: " + e.getMessage());
            commandContext.sendMessage(
                TranslationUtil.translate(TranslationKeys.RELOAD_FAILED, msg -> msg.param("error", e.getMessage()))
            );
        }
    }
}
