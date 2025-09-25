package com.mcsmanager.bot.listener;

import com.mcsmanager.bot.Bot;
import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.storage.VoteStorage;
import com.mcsmanager.bot.util.LogUtils;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.IOException;

/**
 * Event listener for thread deletion cleanup.
 * Removes vote data when suggestion threads are deleted.
 * 
 * @author SkyKing_PX
 */
public class ThreadDeleteListener extends ListenerAdapter {

    /** Vote storage instance for cleanup operations */
    private static final VoteStorage storage = Bot.getVoteStorage();

    /**
     * Handles thread deletion events.
     * Removes vote data for deleted suggestion threads to prevent data accumulation.
     * 
     * @param event The channel deletion event
     */
    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (!event.getChannel().getType().isThread()) {
            return;
        }

        try {
            if (event.getChannel().asThreadChannel().getParentChannel().getId().equals(Config.get().getVoting().getSuggestions_forum_id())) {
                String threadID = event.getChannel().asThreadChannel().getId();
                storage.removeAllVotes(threadID);
            }
        } catch (IOException e) {
            LogUtils.logException("Couldn't remove Votes from Thread \"" + event.getChannel().asThreadChannel().getName() + "\" with ID " + event.getChannel().asThreadChannel().getId(), e);
        }
    }
}
