package com.mcsmanager.bot.util;

import com.mcsmanager.bot.Bot;
import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.storage.VoteStorage;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.channel.forums.ForumTag;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Utility class for handling thread closure operations.
 * Provides confirmation dialogs and permission checking for closing forum threads.
 * 
 * @author SkyKing_PX
 */
public class CloseHandler extends ListenerAdapter {

    /** Vote storage for cleanup operations */
    private static final VoteStorage storage = Bot.getVoteStorage();

    /**
     * Sends a confirmation dialog for thread closure.
     * 
     * @param thread The thread to potentially close
     * @param invoker The member requesting closure
     * @param guild The Discord guild
     * @param hook The interaction hook for sending the confirmation
     */
    public static void sendConfirmation(ThreadChannel thread, Member invoker, Guild guild, InteractionHook hook) {
        MessageEmbed confirmation = EmbedUtils.createConfirmation("Are you sure you want to close this post?",
                "You won't be able to contact anyone through this post anymore and it will be archived permanently.");

        Button confirmButton = Button.success("close_confirm:" + thread.getId(), "✅ Confirm");
        Button cancelButton = Button.danger("close_cancel:" + thread.getId(), "❌ Cancel");

        hook.sendMessageEmbeds(confirmation)
                .addActionRow(confirmButton, cancelButton)
                .setEphemeral(true)
                .queue();
    }

    /**
     * Closes a forum thread with proper permission checking and cleanup.
     * 
     * @param thread The thread to close
     * @param invoker The member requesting closure
     * @param guild The Discord guild
     * @param reply Consumer for sending the result embed
     * @throws IOException If there is an error accessing configuration
     */
    public static void closeThread(ThreadChannel thread, Member invoker, Guild guild, Consumer<MessageEmbed> reply) throws IOException {
        ForumChannel parent = thread.getParentChannel().asForumChannel();

        String threadOwnerId = thread.getOwnerId();
        String[] modRoleIds;
        try {
            modRoleIds = Config.get().getRoles().getModerators();
        } catch (Exception e) {
            LogUtils.logException("Error loading mod role", e);
            reply.accept(EmbedUtils.createSimpleError("❌ Config error"));
            return;
        }

        boolean isOwner = invoker.getId().equals(threadOwnerId);
        List<String> modRolesList = List.of(modRoleIds);
        boolean isModerator = invoker.getRoles().stream()
                .anyMatch(role -> modRolesList.contains(role.getId()));

        if (!isOwner && !isModerator) {
            reply.accept(EmbedUtils.createSimpleError("❌ Only the post creator or a moderator can close this post."));
            return;
        }

        ForumTag closedTag = parent.getAvailableTags().stream()
                .filter(tag -> tag.getName().toLowerCase().contains("closed"))
                .findFirst().orElse(null);

        if (closedTag == null) {
            reply.accept(EmbedUtils.createError()
                    .setDescription("❌ Could not find a 'Closed' tag")
                    .build());
            return;
        }

        List<ForumTag> updatedTags = new ArrayList<>(thread.getAppliedTags());
        if (!updatedTags.contains(closedTag)) {
            updatedTags.add(closedTag);
        }

        thread.getManager()
                .setAppliedTags(updatedTags)
                .setLocked(true)
                .setArchived(true)
                .queue(
                        success -> {
                            MessageEmbed successEmbed = EmbedUtils.createSuccess()
                                    .addField("✅ Post Closed", "This post has been successfully closed", false)
                                    .build();

                            reply.accept(successEmbed);

                            MessageEmbed logEmbed = EmbedUtils.createSuccess()
                                    .addField("✅ Post Closed",
                                            "Name: " + thread.getName() +
                                                    "\n Parent: " + thread.getParentChannel().getJumpUrl() +
                                                    "\nLink: " + thread.getJumpUrl() +
                                                    "\nClosed by: " + invoker.getUser().getName(),
                                            false)
                                    .build();

                            MessageHandler.logToChannel(guild, logEmbed);
                            try {
                                String threadId = thread.getId();
                                storage.removeAllVotes(threadId);
                            } catch (IOException e) {
                                LogUtils.logException("[BOT] Error removing votes from thread \"" + thread.getName() + "\"", e);
                            }
                        },
                        failure -> {
                            MessageEmbed failureEmbed = EmbedUtils.createError()
                                    .setDescription("❌ Failed to close the post.")
                                    .build();

                            reply.accept(failureEmbed);
                        }
                );
    }

    /**
     * Handles button interactions for thread closure confirmation.
     * 
     * @param event The button interaction event
     */
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("close_confirm:") && !event.getComponentId().startsWith("close_cancel:")) return;

        String id = event.getComponentId();
        String threadId = id.split(":")[1];
        Guild guild = event.getGuild();
        Member member = event.getMember();

        if (guild == null || member == null) {
            event.reply("❌ Guild or Member not found.").setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = guild.getThreadChannelById(threadId);
        if (thread == null) {
            event.reply("❌ Could not find the thread.").setEphemeral(true).queue();
            return;
        }

        if (id.startsWith("close_confirm:")) {
            event.deferEdit().queue();

            try {
                closeThread(thread, member, guild, embed -> {
                    event.getHook().editOriginalEmbeds(embed).setComponents().queue();
                });
            } catch (IOException e) {
                event.getHook().editOriginal("❌ Error closing the thread.").queue();
            }
        } else if (id.startsWith("close_cancel:")) {
            // Defer edit and then edit the message
            event.deferEdit().queue();

            event.getHook().editOriginalEmbeds(new EmbedBuilder()
                            .setColor(Color.YELLOW)
                            .setDescription("❌ Post closure cancelled.")
                            .build())
                    .setComponents()
                    .queue();
        }
    }
}
