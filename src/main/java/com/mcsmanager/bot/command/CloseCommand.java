package com.mcsmanager.bot.command;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.util.CloseHandler;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * Slash command for closing forum threads (support, bug reports, suggestions).
 * Validates thread permissions and delegates to CloseHandler for confirmation.
 * 
 * @author SkyKing_PX
 */
public class CloseCommand extends ListenerAdapter {



    /**
     * Handles the /close slash command.
     * Validates that the command is used in an appropriate forum thread,
     * then delegates to CloseHandler for confirmation.
     * 
     * @param event The slash command interaction event
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("close")) return;

        if (!event.isFromGuild() || !event.getChannel().getType().isThread()) {
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ This command can only be used inside a forum thread."))
                    .setEphemeral(true).queue();
            return;
        }

        try {
            String supportForumId = Config.get().getSupport().getSupport_forum_id();
            String bugReportForumId = Config.get().getBugReport().getBugReport_forum_id();
            String suggestionForumId = Config.get().getVoting().getSuggestions_forum_id();
            String parentId = event.getChannel().asThreadChannel().getParentChannel().getId();

            if (!parentId.equals(supportForumId) && !parentId.equals(bugReportForumId) && !parentId.equals(suggestionForumId)) {
                event.replyEmbeds(EmbedUtils.createSimpleError("❌ This command can only be used inside selected forum threads."))
                        .setEphemeral(true).queue();
                return;
            }
        } catch (IOException e) {
            LogUtils.logException("Error while executing /close command", e);
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ Config error. Please try again later."))
                    .setEphemeral(true).queue();
            return;
        }

        ThreadChannel thread = event.getChannel().asThreadChannel();
        Member invoker = event.getMember();
        Guild guild = event.getGuild();

        // Defer the reply to show confirmation buttons (handled inside CloseHandler)
        event.deferReply(true).queue(hook -> CloseHandler.sendConfirmation(thread, invoker, guild, hook));
    }
}