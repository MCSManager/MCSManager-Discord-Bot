package com.mcsmanager.bot.command;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.storage.VoteStorage;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.ThreadChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ListRequestsCommand extends ListenerAdapter {
    /**
     * Vote storage instance for persistent data
     */
    private final VoteStorage storage;

    /**
     * In-memory cache of upvotes per thread
     */
    private final Map<String, Integer> yesVotes = new HashMap<>();
    /**
     * In-memory cache of downvotes per thread
     */
    private final Map<String, Integer> noVotes = new HashMap<>();

    /**
     * Constructs a new SuggestionListener with vote storage.
     * Loads existing vote data from storage into memory.
     *
     * @param storage VoteStorage instance for persistent vote data
     * @throws IOException If there is an error loading existing vote data
     */
    public ListRequestsCommand(VoteStorage storage) throws IOException {
        this.storage = storage;
        Map<String, int[]> votes = storage.loadAllVotes();
        votes.forEach((id, pair) -> {
            yesVotes.put(id, pair[0]);
            noVotes.put(id, pair[1]);
        });
    }

    private static final int PAGE_SIZE = 10;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("list-requests")) return;

        try {
            String suggestionForumId = Config.get().getVoting().getSuggestions_forum_id();
            Channel channel = event.getChannel();
            Member invoker = event.getMember();
            Guild guild = event.getGuild();

            LogUtils.logCommand("list-requests", invoker.getId());
            LogUtils.logInfo("Listing feature requests", "guild=" + guild.getId());
            event.deferReply().queue(null, error -> LogUtils.logException("Failed to defer /list-requests reply", error));

            listRequests(event);
        } catch (IOException e) {
            LogUtils.logException("Error while executing /list-requests command", e);
            event.replyEmbeds(EmbedUtils.createSimpleError("❌ Config error. Please try again later."))
                .setEphemeral(true).queue();
        }
    }

    public void listRequests(SlashCommandInteractionEvent event) {
        int page = event.getOption("page") != null
            ? event.getOption("page").getAsInt()
            : 1;

        listRequests(event.getHook(), event.getGuild(), page);
    }

    private void listRequests(InteractionHook hook, Guild guild, int page) {
        LogUtils.logDebug("Building feature request page " + page, "guild=" + guild.getId());
        Map<String, int[]> votes = storage.loadAllVotes();

        List<Map.Entry<String, int[]>> sorted = votes.entrySet()
            .stream()
            .sorted((a, b) -> Integer.compare(
                b.getValue()[0],
                a.getValue()[0]
            ))
            .toList();

        int maxPage = Math.max(1, (int) Math.ceil((double) sorted.size() / PAGE_SIZE));

        if (page < 1 || page > maxPage) {
            hook.editOriginalEmbeds(
                EmbedUtils.createSimpleError(
                    "❌ Invalid page. Available pages: 1-" + maxPage
                )
            ).queue();
            return;
        }

        int start = (page - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, sorted.size());

        EmbedBuilder embed = EmbedUtils.createSuccess()
            .setTitle("Feature Requests")
            .setFooter("Page " + page + "/" + maxPage);

        if (sorted.isEmpty()) {
            embed.setDescription("No feature requests found.");
            hook.editOriginalEmbeds(embed.build()).queue();
            return;
        }

        List<String> requests = new ArrayList<>();

        for (int i = start; i < end; i++) {
            Map.Entry<String, int[]> entry = sorted.get(i);

            String threadId = entry.getKey();
            int up = entry.getValue()[0];
            int down = entry.getValue()[1];

            ThreadChannel thread = guild.getThreadChannelById(threadId);

            if (thread == null) {
                requests.add(
                    String.format(
                        "https://discord.com/channels/%s/%s - :thumbsup: %d | :thumbsdown: %d",
                        guild.getId(),
                        threadId,
                        up,
                        down
                    )
                );
                continue;
            }

            requests.add(
                String.format(
                    "%s - :thumbsup: %d | :thumbsdown: %d",
                    thread.getJumpUrl(),
                    up,
                    down
                )
            );
        }

        if (requests.isEmpty()) {
            embed.setDescription("No valid feature requests found.");
        } else {
            embed.setDescription(String.join("\n", requests));
        }

        List<Button> buttons = new ArrayList<>();

        if (page > 1) {
            buttons.add(Button.primary(
                "requests_page_" + (page - 1),
                "Previous"
            ));
        } else {
            buttons.add(Button.primary(
                "requests_page_" + page,
                "Previous"
            ).asDisabled());
        }

        if (page < maxPage) {
            buttons.add(Button.primary(
                "requests_page_" + (page + 1),
                "Next"
            ));
        } else {
            buttons.add(Button.primary(
                "requests_page_" + page,
                "Next"
            ).asDisabled());
        }

        hook.editOriginalEmbeds(embed.build())
            .setComponents(
                buttons.isEmpty()
                    ? List.of()
                    : List.of(ActionRow.of(buttons))
            )
            .queue(success -> LogUtils.logDebug("Feature request page " + page + " sent"),
                error -> LogUtils.logException("Failed to send feature request page " + page, error));
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String id = event.getComponentId();

        if (!id.startsWith("requests_page_")) {
            return;
        }

        int page = Integer.parseInt(id.replace("requests_page_", ""));
        LogUtils.logInfo("Feature request pagination button clicked", "user=" + event.getUser().getId() + ", page=" + page);

        event.deferEdit().queue(null, error -> LogUtils.logException("Failed to defer requests pagination button", error));

        // Reuse the same message instead of creating a new one
        listRequests(
            event.getHook(),
            event.getGuild(),
            page
        );
    }
}
