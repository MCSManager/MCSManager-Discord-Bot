package com.mcsmanager.bot;

import com.mcsmanager.bot.faq.FaqEntry;
import com.mcsmanager.bot.util.LogUtils;
import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Configuration management class for the MCSM Discord Bot application.
 * Uses YAML format for configuration storage with automatic default generation.
 * Implements singleton pattern for global configuration access.
 * 
 * @author SkyKing_PX
 */
public class Config {
    /** Path to the configuration file */
    private static final Path CONFIG_PATH = Paths.get("config.yml");
    /** Singleton instance of the configuration */
    private static Config instance;

    /** Bot configuration settings */
    private Bot bot;
    /** Logging configuration */
    private Logging logging;
    /** Voting system configuration */
    private Voting voting;
    /** Role-based permissions configuration */
    private Roles roles;
    /** Bug report system configuration */
    private BugReport bugReport;
    /** Support system configuration */
    private Support support;
    /** FAQ system configuration */
    private Faq faq;
    /** Embed system configuration */
    private Embeds embeds;

    /**
     * Gets the singleton instance of the configuration.
     * Creates and loads the configuration if it doesn't exist.
     * 
     * @return The configuration instance
     * @throws IOException If there is an error loading the configuration file
     */
    public static Config get() throws IOException {
        if (instance == null) {
            instance = new Config();
            instance.load();
        }
        return instance;
    }

    /**
     * Loads the configuration from the YAML file.
     * Creates a default configuration if none exists.
     * 
     * @throws IOException If there is an error reading or parsing the configuration file
     */
    private void load() throws IOException {
        if (Files.notExists(CONFIG_PATH)) {
            createDefaultConfig();
        }

        try (InputStream input = Files.newInputStream(CONFIG_PATH)) {
            LoaderOptions options = new LoaderOptions();
            Constructor constructor = new Constructor(Config.class, options);
            Yaml yaml = new Yaml(constructor);
            Config loaded = yaml.loadAs(input, Config.class);

            this.bot = loaded.bot;
            this.logging = loaded.logging;
            this.voting = loaded.voting;
            this.roles = loaded.roles;
            this.bugReport = loaded.bugReport;
            this.support = loaded.support;
            this.faq = loaded.faq;
            this.embeds = loaded.embeds;
        }
    }

    /**
     * Reloads the configuration from the YAML file.
     * Creates a new instance and replaces the current singleton.
     * 
     * @throws IOException If there is an error loading the configuration file
     */
    public static void reload() throws IOException {
        instance = new Config();
        instance.load();
    }

    /**
     * Creates a default configuration file with template values.
     * 
     * @throws IOException If there is an error writing the configuration file
     */
    private void createDefaultConfig() throws IOException {
        String defaultConfig = """
            bot:
              token: "YOUR_BOT_TOKEN"
              activity: ""
              owner_id: "YOUR_DISCORD_USER_ID"
              guild_id: "YOUR_DISCORD_GUILD_ID"

            logging:
              logChannelId: "0000000000000000000"
              fatalLogChannelId: "0000000000000000000"

            voting:
              suggestions_forum_id: "0000000000000000000"
            
            support:
              support_forum_id: "0000000000000000000"
            
            bugReport:
              bugReport_forum_id: "0000000000000000000"
            
            roles:
              moderators: ["0000000000000000000"]
              
            faq:
              faq_channel_id: "0000000000000000000"
              faq_entries:
                - question: "Q: ?"
                  answer: "**A:** ."
                  imageUrl: ""
                  thumbnailUrl: ""
              
            embeds:
              defaultColor: "#454040"
              successColor: "#00ff33"
              errorColor: "#ff0000"
              warningColor: "#ff9900"
              infoColor: "#ffcc33"
              footerText: "MCSManager Bot | Version {Version}"
            """;

        Files.writeString(CONFIG_PATH, defaultConfig);
        LogUtils.logConfig("Created default config file at: " + CONFIG_PATH.toAbsolutePath());
    }

    // --- Configuration Getters and Setters ---

    /** @return Bot configuration */
    public Bot getBot() { return bot; }
    /** @param bot Bot configuration to set */
    public void setBot(Bot bot) { this.bot = bot; }

    /** @return Logging configuration */
    public Logging getLogging() { return logging; }
    /** @param logging Logging configuration to set */
    public void setLogging(Logging logging) { this.logging = logging; }

    /** @return Voting system configuration */
    public Voting getVoting() { return voting; }
    /** @param voting Voting configuration to set */
    public void setVoting(Voting voting) { this.voting = voting; }

    /** @return Roles configuration */
    public Roles getRoles() { return roles; }
    /** @param roles Roles configuration to set */
    public void setRoles(Roles roles) { this.roles = roles; }

    /** @return Bug report system configuration */
    public BugReport getBugReport() { return bugReport; }
    /** @param bugReport Bug report configuration to set */
    public void setBugReport(BugReport bugReport) { this.bugReport = bugReport; }

    /** @return Support system configuration */
    public Support getSupport() { return support; }
    /** @param support Support configuration to set */
    public void setSupport(Support support) { this.support = support; }

    /** @return FAQ system configuration */
    public Faq getFaq() { return faq; }
    /** @param faq FAQ configuration to set */
    public void setFaq(Faq faq) { this.faq = faq; }

    /** @return Embed system configuration */
    public Embeds getEmbeds() { return embeds; }
    /** @param embeds Embed configuration to set */
    public void setEmbeds(Embeds embeds) { this.embeds = embeds; }

    /**
     * Bot-specific configuration settings.
     */
    public static class Bot {
        /** Discord bot token for authentication */
        private String token;
        /** Activity status displayed by the bot */
        private String activity;
        /** Discord user ID of the bot owner */
        private String owner_id;
        /** Discord guild ID where the bot operates */
        private String guild_id;

        public String getToken() { return token; }
        public void setToken(String token) { this.token = token; }

        public String getActivity() { return activity; }
        public void setActivity(String activity) { this.activity = activity; }

        public String getOwner_id() { return owner_id; }
        public void setOwner_id(String owner_id) { this.owner_id = owner_id; }

        public String getGuild_id() { return guild_id; }
        public void setGuild_id(String guild_id) { this.guild_id = guild_id; }
    }

    /**
     * Logging configuration settings.
     */
    public static class Logging {
        /** Discord channel ID for bot logs */
        private String logChannelId;
        private String fatalLogChannelId;
        /** @return Channel ID for logs */
        public String getLogChannelId() { return logChannelId; }
        /** @param logChannelId Channel ID to set for logs */
        public void setLogChannelId(String logChannelId) { this.logChannelId = logChannelId; }
        /** @return Channel ID for fatal bot errors */
        public String getFatalLogChannelId() { return fatalLogChannelId; }
        /** @param fatalLogChannelId Channel ID to set for fatal bot errors */
        public void setFatalLogChannelId(String fatalLogChannelId) { this.fatalLogChannelId = fatalLogChannelId; }
    }

    /**
     * Voting system configuration settings.
     */
    public static class Voting {
        /** Discord forum channel ID for suggestions */
        private String suggestions_forum_id;
        /** @return Forum ID for suggestions */
        public String getSuggestions_forum_id() { return suggestions_forum_id; }
        /** @param suggestions_forum_id Forum ID to set for suggestions */
        public void setSuggestions_forum_id(String suggestions_forum_id) { this.suggestions_forum_id = suggestions_forum_id; }
    }

    /**
     * Role-based permission configuration.
     */
    public static class Roles {
        /** Array of Discord role IDs for moderators */
        private String[] moderators;
        /** @return Array of moderator role IDs */
        public String[] getModerators() { return moderators; }
        /** @param moderators Array of moderator role IDs to set */
        public void setModerators(String[] moderators) { this.moderators = moderators; }
    }

    /**
     * Bug report system configuration.
     */
    public static class BugReport {
        /** Discord forum channel ID for bug reports */
        private String bugReport_forum_id;
        /** @return Forum ID for bug reports */
        public String getBugReport_forum_id() { return bugReport_forum_id; }
        /** @param bugReport_forum_id Forum ID to set for bug reports */
        public void setBugReport_forum_id(String bugReport_forum_id) { this.bugReport_forum_id = bugReport_forum_id; }
    }

    /**
     * Support system configuration.
     */
    public static class Support {
        /** Discord forum channel ID for support requests */
        private String support_forum_id;
        /** @return Forum ID for support requests */
        public String getSupport_forum_id() { return support_forum_id; }
        /** @param support_forum_id Forum ID to set for support requests */
        public void setSupport_forum_id(String support_forum_id) { this.support_forum_id = support_forum_id; }
    }

    /**
     * FAQ system configuration.
     */
    public static class Faq {
        /** Discord channel ID for FAQ messages */
        private String faq_channel_id;
        /** Array of FAQ entries to display */
        private FaqEntry[] faq_entries;
        /** @return Channel ID for FAQ */
        public String getFaq_channel_id() { return faq_channel_id; }
        /** @param faq_channel_id Channel ID to set for FAQ */
        public void setFaq_channel_id(String faq_channel_id) { this.faq_channel_id = faq_channel_id; }
        /** @return Array of FAQ entries */
        public FaqEntry[] getFaq_entries() { return faq_entries; }
        /** @param faq_entries Array of FAQ entries to set */
        public void setFaq_entries(FaqEntry[] faq_entries) { this.faq_entries = faq_entries; }
    }

    /**
     * Embed system configuration.
     */
    public static class Embeds {
        /** Embed colors */
        private String defaultColor;
        private String successColor;
        private String errorColor;
        private String warningColor;
        private String infoColor;
        /** Embed footer text */
        private String footerText;
        /** @return Default embed color in HEX format */
        public String getDefaultColor() { return defaultColor; }
        /** @param defaultColor Sets default embed color in HEX format */
        public void setDefaultColor(String defaultColor) { this.defaultColor = defaultColor; }
        /** @return Success embed color in HEX format */
        public String getSuccessColor() { return successColor; }
        /** @param successColor Sets success embed color in HEX format */
        public void setSuccessColor(String successColor) { this.successColor = successColor; }
        /** @return Error embed color in HEX format */
        public String getErrorColor() { return errorColor; }
        /** @param errorColor Sets error embed color in HEX format */
        public void setErrorColor(String errorColor) { this.errorColor = errorColor; }
        /** @return Warning embed color in HEX format */
        public String getWarningColor() { return warningColor; }
        /** @param warningColor Sets warning embed color in HEX format */
        public void setWarningColor(String warningColor) { this.warningColor = warningColor; }
        /** @return Info embed color in HEX format */
        public String getInfoColor() { return infoColor; }
        /** @param infoColor Sets info embed color in HEX format */
        public void setInfoColor(String infoColor) { this.infoColor = infoColor; }
        /** @return Default embed Footer Text ({Version} - Bot Version) */
        public String getFooterText() { return footerText; }
        /** @param footerText Sets default embed Footer Text */
        public void setFooterText(String footerText) { this.footerText = footerText; }
    }
}
