package com.mcsmanager.bot.util;

import com.mcsmanager.bot.Bot;
import com.mcsmanager.bot.Config;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;

import java.awt.*;
import java.io.IOException;

/**
 * Utility class for creating standardized Discord embeds.
 * Provides common embed templates and styling to ensure consistency across the bot.
 * 
 * @author SkyKing_PX
 */
public class EmbedUtils {
    
    /** Default bot color - Blue */
    public static Color DEFAULT_COLOR = Color.GRAY;
    static {
        try {
            DEFAULT_COLOR = Color.decode(Config.get().getEmbeds().getDefaultColor());
        } catch (IOException e) {
            LogUtils.logException("Error while getting/decoding default color", e);
        }
    }

    /** Success color - Green */
    public static Color SUCCESS_COLOR = Color.GRAY;
    static {
        try {
            SUCCESS_COLOR = Color.decode(Config.get().getEmbeds().getSuccessColor());
        } catch (IOException e) {
            LogUtils.logException("Error while getting/decoding success color", e);
        }
    }

    /** Error color - Red */
    public static Color ERROR_COLOR = Color.GRAY;
    static {
        try {
            ERROR_COLOR = Color.decode(Config.get().getEmbeds().getErrorColor());
        } catch (IOException e) {
            LogUtils.logException("Error while getting/decoding error color", e);
        }
    }

    /** Warning color - Orange */
    public static Color WARNING_COLOR = Color.GRAY;
    static {
        try {
            WARNING_COLOR = Color.decode(Config.get().getEmbeds().getWarningColor());
        } catch (IOException e) {
            LogUtils.logException("Error while getting/decoding warning color", e);
        }
    }

    /** Info color - Yellow */
    public static Color INFO_COLOR = Color.GRAY;
    static {
        try {
            INFO_COLOR = Color.decode(Config.get().getEmbeds().getInfoColor());
        } catch (IOException e) {
            LogUtils.logException("Error while getting/decoding info color", e);
        }
    }
    
    /** Standard bot footer text */
    private static String FOOTER_TEXT = "Footer Text not set";
    static {
        try {
            FOOTER_TEXT = Config.get().getEmbeds().getFooterText();
            /** Replace {Version} placeholder with bot version */
            FOOTER_TEXT = FOOTER_TEXT.replace("{Version}", Bot.VERSION);
        } catch (IOException e) {
            LogUtils.logException("Error while getting footer text", e);
        }
    }
    
    /**
     * Creates a basic embed with the default bot styling.
     * 
     * @return EmbedBuilder with default color and footer
     */
    public static EmbedBuilder createDefault() {
        return new EmbedBuilder()
                .setColor(DEFAULT_COLOR)
                .setFooter(FOOTER_TEXT);
    }

    /**
     * Creates a basic embed with the default bot styling.
     *
     * @return EmbedBuilder with default color and footer
     */
    public static EmbedBuilder createFooter() {
        return new EmbedBuilder()
                .setFooter(FOOTER_TEXT);
    }
    
    /**
     * Creates a success embed with green color.
     * 
     * @return EmbedBuilder configured for success messages
     */
    public static EmbedBuilder createSuccess() {
        return new EmbedBuilder()
                .setColor(SUCCESS_COLOR)
                .setFooter(FOOTER_TEXT);
    }
    
    /**
     * Creates an error embed with red color.
     * 
     * @return EmbedBuilder configured for error messages
     */
    public static EmbedBuilder createError() {
        return new EmbedBuilder()
                .setColor(ERROR_COLOR)
                .setFooter(FOOTER_TEXT);
    }
    
    /**
     * Creates a warning embed with orange color.
     * 
     * @return EmbedBuilder configured for warning messages
     */
    public static EmbedBuilder createWarning() {
        return new EmbedBuilder()
                .setColor(WARNING_COLOR)
                .setFooter(FOOTER_TEXT);
    }
    
    /**
     * Creates an info embed with yellow color.
     * 
     * @return EmbedBuilder configured for info messages
     */
    public static EmbedBuilder createInfo() {
        return new EmbedBuilder()
                .setColor(INFO_COLOR)
                .setFooter(FOOTER_TEXT);
    }
    
    /**
     * Creates a success embed with title and description.
     * 
     * @param title The embed title
     * @param description The embed description
     * @return Complete success embed
     */
    public static MessageEmbed createSuccessEmbed(String title, String description) {
        return createSuccess()
                .setTitle(title)
                .setDescription(description)
                .build();
    }
    
    /**
     * Creates an error embed with title and description.
     * 
     * @param title The embed title
     * @param description The embed description
     * @return Complete error embed
     */
    public static MessageEmbed createErrorEmbed(String title, String description) {
        return createError()
                .setTitle(title)
                .setDescription(description)
                .build();
    }
    
    /**
     * Creates a simple error embed with just a description.
     * 
     * @param description The error description
     * @return Complete error embed
     */
    public static MessageEmbed createSimpleError(String description) {
        return createError()
                .setDescription(description)
                .build();
    }
    
    /**
     * Creates a simple success embed with just a description.
     * 
     * @param description The success description
     * @return Complete success embed
     */
    public static MessageEmbed createSimpleSuccess(String description) {
        return createSuccess()
                .setDescription(description)
                .build();
    }
    
    /**
     * Creates a confirmation embed for user actions.
     * 
     * @param title The confirmation title
     * @param description The confirmation description
     * @return Complete warning embed for confirmations
     */
    public static MessageEmbed createConfirmation(String title, String description) {
        return createError() // Use red color for confirmations to indicate caution
                .setTitle(title)
                .setDescription(description)
                .build();
    }
    
    /**
     * Creates a log embed for administrative actions.
     * 
     * @param title The log entry title
     * @param description The log entry description
     * @return Complete log embed
     */
    public static MessageEmbed createLogEmbed(String title, String description) {
        return createSuccess()
                .setTitle(title)
                .setDescription(description)
                .build();
    }
    
    /**
     * Creates a ticket-related embed with consistent styling.
     * 
     * @param title The ticket embed title
     * @param description The ticket embed description
     * @return Complete ticket embed
     */
    public static MessageEmbed createTicketEmbed(String title, String description) {
        return createDefault()
                .setTitle(title)
                .setDescription(description)
                .build();
    }
}