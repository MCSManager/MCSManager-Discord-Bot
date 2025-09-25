package com.mcsmanager.bot.faq;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import com.mcsmanager.bot.util.MessageHandler;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Handles the /sendfaq command for posting FAQ entries to the configured channel.
 * Restricted to bot owner only.
 * 
 * @author SkyKing_PX
 */
public class FaqHandler extends ListenerAdapter {


    /**
     * Handles the /sendfaq slash command.
     * Posts all configured FAQ entries to the FAQ channel.
     * 
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (event.getName().equals("sendfaq")) {
            // Disabled until needed
//            event.deferReply(true).queue();
//            try {
//                String ownerId = Config.get().getBot().getOwner_id();
//                if (!event.getUser().getId().equals(ownerId)) {
//                    LogUtils.logCommandFailure("sendfaq", event.getUser().getId(), "Unauthorized access attempt");
//                    event.getHook().sendMessageEmbeds(EmbedUtils.createSimpleError("❌ You are not authorized to use this command."))
//                            .setEphemeral(true).queue();
//                    return;
//                }
//            } catch (IOException e) {
//                LogUtils.logException("Error while executing /sendfaq", e);
//                return;
//            }
//
//            try {
//                Config config = Config.get();
//                TextChannel faqChannel = event.getGuild().getTextChannelById(config.getFaq().getFaq_channel_id());
//
//                if (faqChannel == null) {
//                    LogUtils.logWarning("FAQ Channel not found");
//                    event.getHook().sendMessageEmbeds(EmbedUtils.createSimpleError("❌ FAQ channel not found!"))
//                            .setEphemeral(true).queue();
//                    return;
//                }
//
//                LogUtils.logCommand("sendfaq", event.getUser().getId());
//                for (FaqEntry entry : config.getFaq().getFaq_entries()) {
//                    var embed = EmbedUtils.createDefault()
//                            .setTitle(MessageHandler.parseEmojis(event.getJDA(), entry.getQuestion()));
//
//                    if (entry.getAnswer() != null && !entry.getAnswer().isEmpty()) {
//                        embed.setDescription(MessageHandler.parseEmojis(event.getJDA(), entry.getAnswer()));
//                    }
//
//                    if (entry.getThumbnailUrl() != null && !entry.getThumbnailUrl().isEmpty()) {
//                        embed.setThumbnail(entry.getThumbnailUrl());
//                    } else if (entry.getImageUrl() != null && !entry.getImageUrl().isEmpty()) {
//                        embed.setImage(entry.getImageUrl());
//                    }
//
//                    faqChannel.sendMessageEmbeds(embed.build()).queue();
//                }
//            } catch (IOException e) {
//                LogUtils.logException("Error while sending FAQ messages", e);
//                event.getHook().sendMessageEmbeds(EmbedUtils.createSimpleError("❌ Failed to send FAQ messages."))
//                        .setEphemeral(true).queue();
//                return;
//            }
//            event.getHook().sendMessageEmbeds(EmbedUtils.createSimpleSuccess("✅ FAQ messages sent!"))
//                    .setEphemeral(true).queue();
        }
    }
}

