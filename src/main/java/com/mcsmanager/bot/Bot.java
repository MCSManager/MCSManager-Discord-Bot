package com.mcsmanager.bot;

import com.mcsmanager.bot.command.CloseCommand;
import com.mcsmanager.bot.command.FAQCommand;
import com.mcsmanager.bot.command.InfoCommand;
import com.mcsmanager.bot.faq.FaqHandler;
import com.mcsmanager.bot.listener.BugReportListener;
import com.mcsmanager.bot.listener.SuggestionListener;
import com.mcsmanager.bot.listener.SupportListener;
import com.mcsmanager.bot.listener.ThreadDeleteListener;
import com.mcsmanager.bot.util.CloseHandler;
import com.mcsmanager.bot.util.LogUploader;
import com.mcsmanager.bot.util.LogUtils;
import com.mcsmanager.bot.util.Reload;
import com.mcsmanager.bot.storage.VoteStorage;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;

import java.io.IOException;

/**
 * Main Bot class for the MCSM Discord Bot Discord application.
 * This class initializes the bot, registers event listeners, and configures JDA.
 * 
 * @author SkyKing_PX
 */
public class Bot {
    /** Current version of the bot */
    public static final String VERSION = "1.0.1";


    /** Storage for vote data across suggestion forums */
    private static VoteStorage voteStorage;

    /**
     * Initializes the storage systems for votes and tickets.
     * This method must be called before accessing any storage-related functionality.
     * 
     * @throws IOException If there is an error initializing the storage files
     */
    public static void initStorage() {
        LogUtils.logStorage("Initializing...", "Vote Storage");
        try {
            voteStorage = new VoteStorage();
        } catch (Exception e) {
            LogUtils.logFatalException("Error initializing vote storage", e);
        }
        LogUtils.logStorage("Initialized", "Vote Storage");
    }

    /**
     * Gets the vote storage instance for managing suggestion votes.
     * 
     * @return The vote storage instance
     */
    public static VoteStorage getVoteStorage() {
        return voteStorage;
    }

    /**
     * Main entry point for the MCSM Discord Bot application.
     * Initializes storage, configures JDA, and registers all event listeners.
     * 
     * @param args Command line arguments (not used)
     * @throws Exception If any error occurs during initialization
     */
    public static void main(String[] args) throws Exception {
        initStorage();
        String activity = "Incorrect Configuration";
        try {
            activity = Config.get().getBot().getActivity();
            activity = activity.replace("{Version}", Bot.VERSION);
        } catch (Exception e) {
            LogUtils.logException("Error while getting Bot Activity from Config. It may be corrupt.", e);
        }

        JDA api = JDABuilder.createDefault(Config.get().getBot().getToken())
                .addEventListeners(
                        new InfoCommand(),
                        new FAQCommand(),
                        new LogUploader(),
                        new Listener(),
                        new CloseCommand(),
                        new SuggestionListener(Bot.getVoteStorage()),
                        new BugReportListener(),
                        new SupportListener(),
                        new CloseHandler(),
                        new FaqHandler(),
                        new Reload(),
                        new ThreadDeleteListener())
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .setActivity(Activity.playing(activity))
                .setStatus(OnlineStatus.ONLINE)
                .build();
    }
}
