package com.mcsmanager.bot.util;

import com.mcsmanager.bot.Bot;
import com.mcsmanager.bot.Config;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Utility class for standardized logging operations.
 * Provides consistent logging patterns and error handling across the bot.
 * 
 * @author SkyKing_PX
 */
public class LogUtils {
    /** Default Logger instance for the bot */
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    /**
     * Logs an exception at ERROR level.
     *
     * @param message Descriptive message about what was being attempted
     */
    public static void logEmptyException(String message) {
        logger.error("[BOT - ERROR] {}", message);
    }

    /**
     * Logs an exception with a descriptive message at ERROR level.
     *
     * @param message Descriptive message about what was being attempted
     * @param exception The exception that occurred
     */
    public static void logException(String message, Throwable exception) {
        logger.error("[BOT - ERROR] {}: {}", message, exception.getMessage(), exception);
    }
    
    /**
     * Logs an exception with a descriptive message and additional context at ERROR level.
     *
     * @param message Descriptive message about what was being attempted
     * @param context Additional context (e.g., user ID, channel ID)
     * @param exception The exception that occurred
     */
    public static void logException(String message, String context, Throwable exception) {
        logger.error("[BOT - ERROR] {} ({}): {}", message, context, exception.getMessage(), exception);
    }

    /**
     * Logs an exception with a descriptive message at ERROR level.
     *
     * @param jda JDA instance
     * @param message Descriptive message about what was being attempted
     * @param exception The exception that occurred
     */
    public static void logFatalException(JDA jda, String message, Throwable exception) {
        logger.error("[BOT - FATAL] {}: {}", message, exception.getMessage(), exception);
        try {
            MessageEmbed embed = EmbedUtils.createErrorEmbed("Fatal Bot Error", "An error occurred in an undefined Stage: " + exception.getMessage() + "\n\n" + exception.getStackTrace()[0].toString() + "\n\nThe Bot will now Exit with Code 1\n<@" + Config.get().getBot().getOwner_id() + ">");
            jda.getTextChannelById(Config.get().getLogging().getFatalLogChannelId()).sendMessageEmbeds(embed).queue();
        } catch (IOException e) {
            LogUtils.logException("Error while sending fatal error embed. Exiting...", e);
        }
        jda.shutdown();
        System.exit(1);
    }

    /**
     * Logs an exception with a descriptive message and additional context at ERROR level.
     *
     * @param jda JDA instance
     * @param message Descriptive message about what was being attempted
     * @param context Additional context (e.g., user ID, channel ID)
     * @param exception The exception that occurred
     */
    public static void logFatalException(JDA jda, String message, String context, Throwable exception) {
        logger.error("[BOT - FATAL] {} ({}): {}", message, context, exception.getMessage(), exception);
        try {
            MessageEmbed embed = EmbedUtils.createErrorEmbed("Fatal Bot Error", "An error occurred in Stage" + context + ": " + exception.getMessage() + "\n\n" + exception.getStackTrace()[0].toString() + "\n\nThe Bot will now Exit with Code 1\n<@" + Config.get().getBot().getOwner_id() + ">");
            jda.getTextChannelById(Config.get().getLogging().getFatalLogChannelId()).sendMessageEmbeds(embed).queue();
        } catch (IOException e) {
            LogUtils.logException("Error while sending fatal error embed. Exiting...", e);
        }
        jda.shutdown();
        System.exit(1);
    }

    /**
     * Logs an exception with a descriptive message at ERROR level.
     *
     * @param message Descriptive message about what was being attempted
     * @param exception The exception that occurred
     */
    public static void logFatalException(String message, Throwable exception) {
        logger.error("[BOT - FATAL] {}: {}", message, exception.getMessage(), exception);
        System.exit(1);
    }

    /**
     * Logs an exception with a descriptive message and additional context at ERROR level.
     *
     * @param message Descriptive message about what was being attempted
     * @param context Additional context (e.g., user ID, channel ID)
     * @param exception The exception that occurred
     */
    public static void logFatalException(String message, String context, Throwable exception) {
        logger.error("[BOT - FATAL] {} ({}): {}", message, context, exception.getMessage(), exception);
        System.exit(1);
    }
    
    /**
     * Logs a warning message with consistent formatting.
     *
     * @param message The warning message
     */
    public static void logWarning(String message) {
        logger.warn("[BOT - WARN] {}", message);
    }
    
    /**
     * Logs a warning message with context.
     *
     * @param message The warning message
     * @param context Additional context information
     */
    public static void logWarning(String message, String context) {
        logger.warn("[BOT - WARN] {} ({})", message, context);
    }
    
    /**
     * Logs an info message with consistent formatting.
     *
     * @param message The info message
     */
    public static void logInfo(String message) {
        logger.info("[BOT - INFO] {}", message);
    }
    
    /**
     * Logs an info message with context.
     *
     * @param message The info message
     * @param context Additional context information
     */
    public static void logInfo(String message, String context) {
        logger.info("[BOT - INFO] {} ({})", message, context);
    }
    
    /**
     * Logs a debug message with consistent formatting.
     *
     * @param message The debug message
     */
    public static void logDebug(String message) {
        logger.debug("[BOT - DEBUG] {}", message);
    }
    
    /**
     * Logs a debug message with context.
     *
     * @param message The debug message
     * @param context Additional context information
     */
    public static void logDebug(String message, String context) {
        logger.debug("[BOT - DEBUG] {} ({})", message, context);
    }
    
    /**
     * Logs a command execution attempt.
     *
     * @param command The command name
     * @param userId The user ID who executed the command
     */
    public static void logCommand(String command, String userId) {
        logger.info("[BOT - CMD] Command '{}' executed by user {}", command, userId);
    }
    
    /**
     * Logs a failed command execution.
     *
     * @param command The command name
     * @param userId The user ID who executed the command
     * @param reason The failure reason
     */
    public static void logCommandFailure(String command, String userId, String reason) {
        logger.warn("[BOT - CMD - WARN] Command '{}' failed for user {} - {}", command, userId, reason);
    }
    
    /**
     * Logs configuration-related operations.
     *
     * @param operation The operation being performed
     */
    public static void logConfig(String operation) {
        logger.info("[BOT - CONFIG] {}", operation);
    }
    
    /**
     * Logs storage operations.
     *
     * @param operation The storage operation
     * @param identifier Identifier for the operation (e.g., thread ID, user ID)
     */
    public static void logStorage(String operation, String identifier) {
        logger.debug("[BOT - STORAGE] {} for {}", operation, identifier);
    }
}