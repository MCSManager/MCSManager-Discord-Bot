package com.mcsmanager.bot.util;

import com.mcsmanager.bot.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTagSnowflake;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Timer;
import java.util.TimerTask;
import java.util.List;

/**
 * Handles checking for inactive forum posts and automatically closing them after 30 days.
 * Runs daily at 12 PM to check Bug Report and Support forums for inactive threads.
 *
 * @author SkyKing_PX
 */
public class InactivityChecker extends ListenerAdapter {

    private static Timer timer;
    private static JDA jda;

    /**
     * Starts the inactivity checker timer.
     * Schedules the task to run daily at 12 PM.
     *
     * @param jdaInstance The JDA instance
     */
    public static void start(JDA jdaInstance) {
        jda = jdaInstance;
        timer = new Timer("InactivityChecker", true);
        scheduleNextRun();
        LogUtils.logInfo("Inactivity Checker started. Will run daily at 12:00 PM.");
    }

    /**
     * Schedules the next run of the inactivity checker.
     * Calculates the time until the next 12 PM and schedules accordingly.
     */
    public static void scheduleNextRun() {
        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime next12PM = now.withHour(12).withMinute(0).withSecond(0).withNano(0);

        // If it's already past 12 PM today, schedule for tomorrow
        if (now.isAfter(next12PM)) {
            next12PM = next12PM.plusDays(1);
        }

        long delayMillis = next12PM.toInstant().toEpochMilli() - System.currentTimeMillis();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    checkInactiveThreads();
                } catch (Exception e) {
                    LogUtils.logException("Error during inactivity check", e);
                }
                // Reschedule for the next day
                scheduleNextRun();
            }
        }, delayMillis);

        LogUtils.logInfo("Inactivity Checker scheduled to run at: " + next12PM);
    }

    /**
     * Checks both Bug Report and Support forums for inactive threads.
     *
     * @throws IOException If there is an error accessing configuration
     */
    public static void checkInactiveThreads() throws IOException {
        if (jda == null || jda.getStatus() != JDA.Status.CONNECTED) {
            LogUtils.logWarning("JDA not connected, skipping inactivity check");
            return;
        }

        Guild guild = jda.getGuildById(Config.get().getBot().getGuild_id());
        if (guild == null) {
            LogUtils.logWarning("Guild not found, skipping inactivity check");
            return;
        }

        // Check Bug Report Forum
        checkForumInactivity(guild, Config.get().getBugReport().getBugReport_forum_id(), "Bug Report");

        // Check Support Forum
        checkForumInactivity(guild, Config.get().getSupport().getSupport_forum_id(), "Support");
    }

    /**
     * Checks a specific forum for inactive threads.
     *
     * @param guild The Discord guild
     * @param forumId The forum channel ID
     * @param forumName The name of the forum (for logging)
     */
    private static void checkForumInactivity(Guild guild, String forumId, String forumName) {
        ForumChannel forum = guild.getForumChannelById(forumId);
        if (forum == null) {
            LogUtils.logWarning("Forum " + forumName + " not found");
            return;
        }

        forum.getThreadChannels().forEach(thread -> {
            if (!thread.isArchived()) {
                processThread(thread, forumName);
            }
        });
    }

    /**
     * Processes a single thread to check for inactivity.
     * Sends a reminder at 7 days and closes at 30 days of inactivity.
     *
     * @param thread The thread channel to check
     * @param forumName The name of the forum
     */
    private static void processThread(ThreadChannel thread, String forumName) {
        try {
            // Skip pinned posts
            if (thread.isPinned()) {
                return;
            }

            // Skip posts that are already closed
            boolean isClosed = thread.getAppliedTags().stream()
                    .anyMatch(tag -> tag.getName().toLowerCase().contains("closed"));
            if (isClosed) {
                return;
            }

            // Get the latest message in the thread
            thread.getHistory().retrievePast(100).queue(messages -> {
                if (messages == null || messages.isEmpty()) {
                    return; // No messages to check
                }

                // For reminder check: find the latest message (including bot messages)
                Message latestMessage = null;
                for (Message message : messages) {
                    latestMessage = message;
                    break;
                }

                // For auto-close check: find the latest non-bot user message
                Message latestUserMessage = null;
                for (Message message : messages) {
                    if (!message.getAuthor().isBot()) {
                        latestUserMessage = message;
                        break;
                    }
                }

                if (latestUserMessage == null) {
                    return; // No user messages found
                }

                OffsetDateTime userMessageTime = latestUserMessage.getTimeCreated();
                long daysSinceLastUserMessage = calculateDaysSince(userMessageTime);

                // For reminders: check if the last message (any message) is older than 7 days
                if (latestMessage != null) {
                    OffsetDateTime latestMessageTime = latestMessage.getTimeCreated();
                    long daysSinceLastMessage = calculateDaysSince(latestMessageTime);

                    // Between 7 and 29 days since ANY message: send reminder
                    if (daysSinceLastMessage >= 7 && daysSinceLastMessage < 30) {
                        sendReminderMessage(thread);
                    }
                }

                // At 30 days or more since last USER message: auto-close the thread
                if (daysSinceLastUserMessage >= 30) {
                    autoCloseThread(thread, forumName);
                }
            }, error -> {
                LogUtils.logException("Error retrieving message history for thread " + thread.getName(), error);
            });
        } catch (Exception e) {
            LogUtils.logException("Error processing thread " + thread.getName(), e);
        }
    }

    /**
     * Calculates the number of days between a given time and now.
     *
     * @param offsetDateTime The time to compare
     * @return Number of days elapsed (rounded down)
     */
    private static long calculateDaysSince(OffsetDateTime offsetDateTime) {
        Instant messageInstant = offsetDateTime.toInstant();
        Instant now = Instant.now();
        long secondsDiff = now.getEpochSecond() - messageInstant.getEpochSecond();
        return secondsDiff / (24 * 60 * 60);
    }

    /**
     * Sends a reminder message to the thread asking if the issue was resolved.
     *
     * @param thread The thread to send the reminder to
     */
    private static void sendReminderMessage(ThreadChannel thread) {
        try {
            // Ping the original post creator (thread owner), not the last message author
            long threadCreatorId = thread.getOwnerIdLong();
            MessageEmbed reminder = EmbedUtils.createWarning().addField("Inactivity notice", "It looks like your issue hasn't received a reply in the last 7 days.\nHas your issue been resolved? If so, please close this post using the `/close` command.\nIf not, please try to provide more information or ping the moderators.\n\n> Note: If this post stays inactive for a total of 30 days it will be closed automatically.", false).build();

            thread.sendMessage("<@" + threadCreatorId + ">").addEmbeds(reminder).queue(
                    success -> LogUtils.logInfo("Sent reminder to thread: " + thread.getName()),
                    failure -> LogUtils.logException("Failed to send reminder to thread: " + thread.getName(), failure)
            );
        } catch (Exception e) {
            LogUtils.logException("Error sending reminder message", e);
        }
    }

    /**
     * Automatically closes a thread after 30 days of inactivity.
     *
     * @param thread The thread to close
     * @param forumName The name of the forum
     */
    private static void autoCloseThread(ThreadChannel thread, String forumName) {
        try {
            // Apply closed tag
            String closedTagName = "closed";
            var closedTag = thread.getParentChannel().asForumChannel().getAvailableTags().stream()
                    .filter(tag -> tag.getName().toLowerCase().contains(closedTagName))
                    .findFirst()
                    .orElse(null);

            List<ForumTagSnowflake> updatedTags = new java.util.ArrayList<>(thread.getAppliedTags());
            if (closedTag != null && !updatedTags.contains(closedTag)) {
                updatedTags.add(closedTag);
            }

            // Lock and archive the thread
            thread.getManager()
                    .setAppliedTags(updatedTags.stream().map(tag -> (net.dv8tion.jda.api.entities.channel.forums.ForumTag) tag).toList())
                    .queue(
                            success -> {
                                // Send closure message with embed
                                MessageEmbed closureEmbed = EmbedUtils.createError()
                                        .setTitle("❗ Post closed")
                                        .setDescription("This post has been automatically closed due to inactivity (30+ days with no user response).\n\n" +
                                                "If you still need help, feel free to create a new post in the " + forumName + " forum.\n\n" +
                                            "> Note: This feature is still in the testing phase. If you feel that your post was closed due to an error, please ping @skyking_px.")
                                        .build();

                                thread.sendMessageEmbeds(closureEmbed).queue(
                                        msgSuccess -> LogUtils.logInfo("Auto-closed and notified: " + thread.getName()),
                                        msgFailure -> LogUtils.logException("Failed to send closure message for: " + thread.getName(), msgFailure)
                                );
                            },
                            failure -> LogUtils.logException("Failed to close thread: " + thread.getName(), failure)
                    );

            thread.getManager()
                .setLocked(true)
                .setArchived(true)
                .queue();
        } catch (Exception e) {
            LogUtils.logException("Error auto-closing thread: " + thread.getName(), e);
        }
    }

    /**
     * Stops the inactivity checker.
     */
    public static void stop() {
        if (timer != null) {
            timer.cancel();
            LogUtils.logInfo("Inactivity Checker stopped.");
        }
    }
}
