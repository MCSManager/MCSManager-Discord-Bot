package com.mcsmanager.bot.command;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import com.mcsmanager.bot.util.MessageHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * Slash command that directs users to the FAQ channel.
 *
 * @author SkyKing_PX
 */
public class FAQCommand extends ListenerAdapter {
    /**
     * Handles the /faq slash command.
     * Sends an embed with a link to the FAQ channel.
     *
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("faq")) return;
        LogUtils.logCommand("faq", event.getUser().getId());
        event.deferReply().queue(null, error -> LogUtils.logException("Failed to defer /faq reply", error));
        try {
            MessageEmbed embed = EmbedUtils.createDefault()
                .addField("Frequently Asked Questions", "You can find the FAQ here: <#" + Config.get().getFaq().getFaq_channel_id() + ">\nIt contains information and documentations that you should read before asking for help.", false)
                .build();
            MessageHandler.sendPreparedMessage(event, embed);
            LogUtils.logInfo("FAQ response prepared", "user=" + event.getUser().getId());
        } catch (Exception e) {
            LogUtils.logException("Failed to get FAQ Channel ID", e);
        }
    }
}
