package com.mcsmanager.bot.util;

import com.mcsmanager.bot.Config;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

/**
 * Slash command for reloading the bot's configuration.
 * Restricted to bot owner only.
 * 
 * @author SkyKing_PX
 */
public class Reload extends ListenerAdapter {


    /**
     * Handles the /reloadconfig slash command.
     * Validates bot owner permissions and reloads the configuration.
     * 
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("reloadconfig")) return;

        try {
            String ownerId = Config.get().getBot().getOwner_id();
            if (!event.getUser().getId().equals(ownerId)) {
                LogUtils.logCommandFailure("reloadconfig", event.getUser().getId(), "Unauthorized access attempt");
                event.replyEmbeds(EmbedUtils.createSimpleError("❌ You are not authorized to use this command."))
                        .setEphemeral(true).queue();
                return;
            }

            LogUtils.logCommand("reloadconfig", event.getUser().getId());
            event.deferReply(true).queue(hook -> {
                try {
                    Config.reload();
                    LogUtils.logConfig("Configuration reloaded successfully");
                    hook.editOriginalEmbeds(EmbedUtils.createSimpleSuccess("✅ Config reloaded successfully!")).queue();
                } catch (Exception e) {
                    LogUtils.logException("Failed to reload configuration", e);
                    hook.editOriginalEmbeds(EmbedUtils.createSimpleError("❌ Failed to reload config: `" + e.getMessage() + "`")).queue();
                }
            });

        } catch (Exception e) {
            LogUtils.logException("Failed to access configuration for reload command", e);
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ Failed to access config: `" + e.getMessage() + "`"))
                    .setEphemeral(true).queue();
        }
    }
}
