package com.mcsmanager.bot.command;

import com.mcsmanager.bot.Bot;
import com.mcsmanager.bot.Listener;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.MessageHandler;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.time.Instant;

/**
 * Slash command that displays bot information including version and uptime.
 * 
 * @author SkyKing_PX
 */
public class InfoCommand extends ListenerAdapter {
    /**
     * Handles the /info slash command.
     * Displays bot version, uptime, and supported modpack version.
     * 
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("info")) return;
        event.deferReply().queue();
        Instant currentTime = Instant.now();
        long seconds = Duration.between(Listener.START_TIME, currentTime).getSeconds();

        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long remainingSeconds = seconds % 60;

        MessageEmbed embed = EmbedUtils.createDefault()
                .setTitle("MCSManager Bot")
                .setThumbnail("https://cdn.discordapp.com/avatars/1418568057755930745/ccc107fadae2d4627054b0b2f8992e51.webp?size=1024")
                .addField("General Information", "**Bot Version:** `" + Bot.VERSION + "`\n**Uptime:** " + hours + "h " + minutes + "min " + remainingSeconds + "sec", false)
                .build();
        MessageHandler.sendPreparedMessage(event, embed);

    }
}