package com.mcsmanager.bot.listener;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.util.CloseHandler;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.ForumChannel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Event listener for support forum thread management.
 * Automatically adds close buttons to new support threads and handles closure.
 * 
 * @author SkyKing_PX
 */
public class SupportListener extends ListenerAdapter {


    /**
     * Handles new thread creation in support forums.
     * Automatically adds a close button to support threads.
     * 
     * @param event The channel creation event
     */
    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        try {
            ThreadChannel thread = event.getChannel().asThreadChannel();
            ForumChannel parent = thread.getParentChannel().asForumChannel();
            if (!parent.getId().equals(Config.get().getSupport().getSupport_forum_id())) return;
        } catch (IOException e) {
            LogUtils.logException("Couldn't get Support Forum ID", e);
            return;
        } catch (IllegalStateException e) {
            LogUtils.logWarning("Incorrect channel type", event.getChannel().getId());
            return;
        }

        MessageEmbed embed = EmbedUtils.createSuccess()
                .addField("Is your Issue resolved?", "When your issue is resolved, please press on the `Close` Button below.", false)
                .build();

        event.getChannel().asThreadChannel().sendMessageEmbeds(embed)
                .addComponents(ActionRow.of(
                        Button.success("support:close:" + event.getChannel().getId(), "Close").withEmoji(Emoji.fromUnicode("✅"))
                )).queue();
    }

    /**
     * Handles button interactions for support thread closure.
     * Validates the interaction and delegates to CloseHandler for confirmation.
     * 
     * @param event The button interaction event
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (!event.getComponentId().startsWith("support:close:")) return;
        if (!event.isFromGuild() || !event.getChannel().getType().isThread()) {
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ This button only works inside selected forum threads."))
                    .setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = event.getChannel().asThreadChannel();
        Member invoker = event.getMember();
        Guild guild = event.getGuild();

        event.deferReply(true).queue(hook -> CloseHandler.sendConfirmation(thread, invoker, guild, hook));
    }
}
