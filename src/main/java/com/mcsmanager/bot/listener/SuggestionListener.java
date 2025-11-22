package com.mcsmanager.bot.listener;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.storage.VoteStorage;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import com.mcsmanager.bot.util.MessageHandler;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Event listener for suggestion forum thread management.
 * Handles voting system for feature suggestions with persistent vote storage.
 * 
 * @author SkyKing_PX
 */
public class SuggestionListener extends ListenerAdapter {


    /** Vote storage instance for persistent data */
    private final VoteStorage storage;

    /**
     * Constructs a new SuggestionListener with vote storage.
     * Loads existing vote data from storage into memory.
     * 
     * @param storage VoteStorage instance for persistent vote data
     * @throws IOException If there is an error loading existing vote data
     */
    public SuggestionListener(VoteStorage storage) throws IOException {
        this.storage = storage;
        Map<String, int[]> votes = storage.loadAllVotes();
        votes.forEach((id, pair) -> {
            yesVotes.put(id, pair[0]);
            noVotes.put(id, pair[1]);
        });
    }

    /**
     * Handles new thread creation in suggestion forums.
     * Automatically adds voting buttons to new suggestion threads.
     * 
     * @param event The channel creation event
     */
    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        try {
            ThreadChannel thread = event.getChannel().asThreadChannel();
            ForumChannel parent = thread.getParentChannel().asForumChannel();
            if (!parent.getId().equals(Config.get().getVoting().getSuggestions_forum_id())) return;
        } catch (IOException e) {
            LogUtils.logException("Couldn't get Suggestions Forum ID", e);
            return;
        } catch (IllegalStateException e) {
            LogUtils.logWarning("Incorrect channel type", event.getChannel().getId());
            return;
        }

        MessageEmbed embed = EmbedUtils.createSuccess()
                .addField("Vote for this Feature", "You are able to vote either **for** or **against** this feature.\nCast your vote below!\n\n> Note: This only improves the chances of this request being **prioritised**. It can still get denied because of various reasons.", false)
                .build();

        // Slight delay to ensure thread is fully ready
        event.getJDA().getRateLimitPool().schedule(() -> {
            event.getChannel().asThreadChannel().sendMessageEmbeds(embed)
                    .addComponents(ActionRow.of(
                            Button.success("vote:up:" + event.getChannel().getId(), "Upvote").withEmoji(Emoji.fromUnicode("👍")),
                            Button.danger("vote:down:" + event.getChannel().getId(), "Downvote").withEmoji(Emoji.fromUnicode("👎"))
                    )).queue();
        }, 2, TimeUnit.SECONDS);
    }

    /** In-memory cache of upvotes per thread */
    private final Map<String, Integer> yesVotes = new HashMap<>();
    /** In-memory cache of downvotes per thread */
    private final Map<String, Integer> noVotes = new HashMap<>();

    /**
     * Handles voting button interactions.
     * Processes upvotes and downvotes, prevents duplicate voting,
     * and updates vote counts with persistent storage.
     * 
     * @param event The button interaction event
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("vote:")) return;

        // Check if thread is locked and archived
        if (event.getChannel().asThreadChannel().isLocked() && event.getChannel().asThreadChannel().isArchived()) {
            User user = event.getUser();
            user.openPrivateChannel()
                    .flatMap(channel -> channel.sendMessage("**You tried to vote on an already closed post in** `" + event.getGuild().getName() + "`\nThe vote has not been registered."))
                    .queue();
            return;
        }

        String threadID = event.getChannel().asThreadChannel().getId();
        String userId = event.getUser().getId();
        boolean isUpvote = event.getComponentId().startsWith("vote:up:");

        int yes = yesVotes.getOrDefault(threadID, 0);
        int no = noVotes.getOrDefault(threadID, 0);

        String previousVote = storage.getUserVote(threadID, userId);

        try {
            // Defer reply for ephemeral feedback
            event.deferReply(true).queue(); // true = ephemeral

            if (previousVote == null) {
                if (isUpvote) yes++;
                else no++;
                yesVotes.put(threadID, yes);
                noVotes.put(threadID, no);

                storage.setVoteCount(threadID, yes, no);
                storage.saveUserVote(threadID, userId, isUpvote ? "up" : "down");

                MessageEmbed embed = EmbedUtils.createLogEmbed("Vote Added", 
                        "**Vote added** by <@" + userId + "> to post " + event.getChannel().asThreadChannel().getJumpUrl() + " - 👍 " + yes + " | 👎 " + no);
                MessageHandler.logToChannel(event.getGuild(), embed);

                // Send ephemeral confirmation
                event.getHook().sendMessage("✅ **Your vote has been registered, thank you!**").queue();

            } else if (!previousVote.equals(isUpvote ? "up" : "down")) {
                if (isUpvote) {
                    yes++;
                    no--;
                } else {
                    no++;
                    yes--;
                }
                yesVotes.put(threadID, yes);
                noVotes.put(threadID, no);

                storage.setVoteCount(threadID, yes, no);
                storage.saveUserVote(threadID, userId, isUpvote ? "up" : "down");

                MessageEmbed embed = EmbedUtils.createLogEmbed("Vote Updated", 
                        "**Vote updated** by <@" + userId + "> in post " + event.getChannel().asThreadChannel().getJumpUrl() + " - 👍 " + yes + " | 👎 " + no);
                MessageHandler.logToChannel(event.getGuild(), embed);

                event.getHook().sendMessage("✅ **Your vote has been changed.**").queue();

            } else {
                event.getHook().sendMessage("❌ **You already voted this way.**").queue();
                return;
            }

            MessageEmbed edited = EmbedUtils.createSuccess()
                    .addField("Vote for this Feature", "You are able to vote either **for** or **against** this feature.\nCast your vote below!", false)
                    .addField("Current Vote Count", "👍 Upvotes: **" + yes + "**\n👎 Downvotes: **" + no + "**", false)
                    .build();

            event.getMessage().editMessageEmbeds(edited)
                    .setComponents(ActionRow.of(
                            Button.success("vote:up:" + event.getChannel().getId(), "Upvote").withEmoji(Emoji.fromUnicode("👍")),
                            Button.danger("vote:down:" + event.getChannel().getId(), "Downvote").withEmoji(Emoji.fromUnicode("👎"))
                    )).queue();

        } catch (Exception e) {
            LogUtils.logException("Failed to process vote", userId, e);
            event.getHook().sendMessage("❌ An error occurred while processing your vote.").queue();
        }
    }
}