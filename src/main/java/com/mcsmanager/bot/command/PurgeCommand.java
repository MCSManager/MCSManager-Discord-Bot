package com.mcsmanager.bot.command;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Slash command for deleting all messages from a user.
 * Can delete messages from the last N days or all messages if no days specified.
 * Requires moderator permissions.
 *
 * @author SkyKing_PX
 */
public class PurgeCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("purge")) return;

        // Check if user has moderator permissions
        if (!isModerator(event)) {
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ You don't have permission to use this command."))
                    .setEphemeral(true).queue();
            return;
        }

        // Get the target user to delete messages from
        User targetUser = event.getOption("user", null, net.dv8tion.jda.api.interactions.commands.OptionMapping::getAsUser);
        if (targetUser == null) {
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ User not found."))
                    .setEphemeral(true).queue();
            return;
        }

        // Get the days argument (optional)
        Integer daysArg = event.getOption("days", null, opt -> opt.getAsInt() > 0 ? opt.getAsInt() : null);

        // Get the channel argument (optional)
        Channel channelArg = event.getOption("channel", null, net.dv8tion.jda.api.interactions.commands.OptionMapping::getAsChannel);
        final TextChannel targetChannel;
        if (channelArg instanceof TextChannel) {
            targetChannel = (TextChannel) channelArg;
        } else {
            targetChannel = null;
        }

        // Get the count argument (optional)
        final Integer countArg = event.getOption("count", null, opt -> opt.getAsInt() > 0 ? opt.getAsInt() : null);

        event.deferReply().queue(hook -> {
            try {
                Guild guild = event.getGuild();
                if (guild == null) {
                    event.getHook().editOriginalEmbeds(EmbedUtils.createSimpleError("❌ Guild not found.")).queue();
                    return;
                }
                deleteUserMessagesAsync(guild, targetUser, daysArg, targetChannel, countArg, deletedCount -> {
                    net.dv8tion.jda.api.EmbedBuilder embed = EmbedUtils.createSuccess()
                            .setTitle("✅ Messages Deleted")
                            .setDescription("Successfully deleted messages from **" + targetUser.getAsMention() + "**")
                            .addField("Messages Deleted", String.valueOf(deletedCount), false);

                    if (targetChannel != null) {
                        embed.addField("Channel", targetChannel.getAsMention(), false);
                    }

                    if (daysArg != null) {
                        embed.addField("Time Range", "Last " + daysArg + " day(s)", false);
                    } else {
                        embed.addField("Time Range", "All messages", false);
                    }

                    if (countArg != null) {
                        embed.addField("Limit", countArg + " messages", false);
                    }

                    event.getHook().editOriginalEmbeds(embed.build()).queue();
                    LogUtils.logInfo("Deleted " + deletedCount + " messages from " + targetUser.getAsTag() +
                            (daysArg != null ? " from last " + daysArg + " days" : " (all messages)") +
                            (targetChannel != null ? " in channel " + targetChannel.getName() : "") +
                            (countArg != null ? " (limit: " + countArg + ")" : ""));
                });

            } catch (Exception e) {
                LogUtils.logException("Error deleting messages", e);
                event.getHook().editOriginalEmbeds(EmbedUtils.createSimpleError("❌ An error occurred while deleting messages.")).queue();
            }
        });
    }

    /**
     * Deletes all messages from a specific user in the guild.
     *
     * @param guild The guild to search in
     * @param targetUser The user whose messages to delete
     * @param daysArg The number of days to go back, or null to delete all messages
     * @param targetChannel The specific channel to scan, or null to scan all channels
     * @param countArg The maximum number of messages to delete, or null for no limit
     * @param onComplete Callback to execute when deletion is complete
     */
    private void deleteUserMessagesAsync(Guild guild, User targetUser, Integer daysArg, TextChannel targetChannel, Integer countArg, java.util.function.Consumer<Integer> onComplete) {
        Thread deletionThread = new Thread(() -> {
            int totalDeleted = 0;
            OffsetDateTime cutoffTime = null;
            int deletionLimit = countArg != null ? countArg : Integer.MAX_VALUE;

            // Calculate cutoff time if days are specified
            if (daysArg != null && daysArg > 0) {
                cutoffTime = OffsetDateTime.now().minusDays(daysArg);
            }

            // Determine which channels to scan
            List<TextChannel> channelsToScan = new ArrayList<>();
            if (targetChannel != null) {
                // Only scan the specified channel
                channelsToScan.add(targetChannel);
            } else {
                // Scan all text channels
                channelsToScan.addAll(guild.getTextChannels());
            }

            // Iterate through the channels to scan
            for (TextChannel channel : channelsToScan) {
                try {
                    LogUtils.logDebug("Searching for messages in channel: " + channel.getName());

                    // Keep retrieving messages batch by batch
                    Message lastMessage = null;
                    boolean hasRetrievedMessages = false;

                    while (true) {
                        // Check if we've reached the deletion limit
                        if (totalDeleted >= deletionLimit) {
                            LogUtils.logDebug("Reached deletion limit of " + deletionLimit);
                            break;
                        }

                        List<Message> messages;

                        try {
                            if (!hasRetrievedMessages) {
                                // First retrieval - get the most recent messages
                                messages = channel.getHistory().retrievePast(100).complete();
                                hasRetrievedMessages = true;
                            } else {
                                // Subsequent retrieval - get messages before the last one
                                if (lastMessage == null) {
                                    // No more messages to retrieve
                                    break;
                                }
                                messages = channel.getHistoryBefore(lastMessage, 100).complete().getRetrievedHistory();
                            }
                        } catch (Exception e) {
                            // If we can't retrieve messages, skip this channel
                            LogUtils.logDebug("Could not retrieve messages from channel " + channel.getName() + ": " + e.getMessage());
                            break;
                        }

                        if (messages.isEmpty()) {
                            break; // No more messages in this channel
                        }

                        int deletedInBatch = 0;
                        boolean shouldContinue = true;

                        for (Message message : messages) {
                            // Check if we've reached the deletion limit
                            if (totalDeleted >= deletionLimit) {
                                shouldContinue = false;
                                break;
                            }

                            // Check if we've gone past the cutoff time
                            if (cutoffTime != null && message.getTimeCreated().isBefore(cutoffTime)) {
                                shouldContinue = false;
                                break;
                            }

                            // Check if message is from target user
                            if (message.getAuthor().getId().equals(targetUser.getId())) {
                                try {
                                    // Use complete() inside a separate thread, not a callback
                                    message.delete().complete();
                                    totalDeleted++;
                                    deletedInBatch++;
                                    LogUtils.logDebug("Deleted message " + message.getId() + " from " + channel.getName());
                                } catch (Exception e) {
                                    LogUtils.logDebug("Could not delete message " + message.getId() + " in channel " + channel.getName() + ": " + e.getMessage());
                                }
                            }
                        }

                        if (deletedInBatch > 0) {
                            LogUtils.logDebug("Deleted " + deletedInBatch + " messages from channel " + channel.getName());
                        }

                        // Stop if we've gone past the cutoff time, hit deletion limit, or this was the last batch
                        if (!shouldContinue || messages.size() < 100) {
                            break;
                        }

                        // Update lastMessage for next iteration
                        lastMessage = messages.getLast();
                    }

                } catch (Exception e) {
                    LogUtils.logDebug("Error accessing channel " + channel.getName() + ": " + e.getMessage());
                }
            }

            // Call the callback with the result
            onComplete.accept(totalDeleted);
        });

        deletionThread.setName("PurgeCommand-Thread");
        deletionThread.setDaemon(true);
        deletionThread.start();
    }

    /**
     * Checks if the user has moderator permissions.
     *
     * @param event The slash command interaction event
     * @return true if user is a moderator, false otherwise
     */
    private boolean isModerator(SlashCommandInteractionEvent event) {
        if (event.getMember() == null) {
            return false;
        }

        String[] modRoleIds;
        try {
            modRoleIds = Config.get().getRoles().getModerators();
        } catch (Exception e) {
            LogUtils.logException("Error loading mod roles", e);
            return false;
        }

        List<String> modRolesList = List.of(modRoleIds);
        return event.getMember().getRoles().stream()
                .anyMatch(role -> modRolesList.contains(role.getId()));
    }
}

