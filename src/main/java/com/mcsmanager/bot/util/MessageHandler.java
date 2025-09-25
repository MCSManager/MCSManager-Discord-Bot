package com.mcsmanager.bot.util;

import com.mcsmanager.bot.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for handling Discord message operations.
 * Provides methods for sending prepared messages, logging, and emoji parsing.
 * 
 * @author SkyKing_PX
 */
public class MessageHandler {


    /**
     * Sends a prepared message embed, optionally mentioning a user.
     * 
     * @param event The slash command interaction event
     * @param embed The message embed to send
     */
    public static void sendPreparedMessage(SlashCommandInteractionEvent event, MessageEmbed embed){
        if (event.getOption("user") != null) {

            User user = event.getOption("user").getAsUser();
            String mention = user.getAsMention();

            event.getHook().sendMessage(mention)
                    .addEmbeds(embed)
                    .queue();
        } else {
            event.getHook().sendMessageEmbeds(embed).queue();
        }
    }

    /**
     * Logs an embed message to the configured log channel.
     * 
     * @param guild The Discord guild where the log channel exists
     * @param embed The embed to log
     */
    public static void logToChannel(Guild guild, MessageEmbed embed) {
        TextChannel logChannel = null;
        try {
            logChannel = guild.getTextChannelById(Config.get().getLogging().getLogChannelId());
        } catch (IOException e) {
            LogUtils.logException("Error getting log channel", e);
        }
        if (logChannel != null) {
            logChannel.sendMessageEmbeds(embed).queue();
        }
    }

    /**
     * Parses custom emoji names in text and converts them to emoji mentions.
     * 
     * @param jda JDA instance for emoji lookup
     * @param text Text containing emoji names in :name: format
     * @return Text with emoji names replaced by emoji mentions
     */
    public static String parseEmojis(JDA jda, String text) {
        Pattern pattern = Pattern.compile(":(\\w+):");
        Matcher matcher = pattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String emojiName = matcher.group(1);
            RichCustomEmoji emoji = jda.getEmojisByName(emojiName, true)
                    .stream()
                    .findFirst()
                    .orElse(null);

            if (emoji != null) {
                matcher.appendReplacement(sb, emoji.getAsMention()); // Replaces :emoji: with <:emoji:id>
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }
}
