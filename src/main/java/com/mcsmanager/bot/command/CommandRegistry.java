package com.mcsmanager.bot.command;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.util.List;

/**
 * Registry for all Discord slash commands supported by MCSM Discord Bot.
 * Centralizes command definition and configuration.
 * 
 * @author SkyKing_PX
 */
public class CommandRegistry {
    /**
     * Registers and configures all slash commands for the bot.
     * 
     * @return List of CommandData objects to register with Discord
     */
    public static List<CommandData> registerCommands() {
        CommandData faq = Commands.slash("faq", "Suggests a user to read the FAQ Channel")
                .addOptions(
                        new OptionData(OptionType.USER, "user", "Optionally choose if you want to ping a member", false)
                );

        CommandData info = Commands.slash("info", "Shows some useful information about the bot");

        CommandData close = Commands.slash("close", "Closes a Forum Post");

        CommandData sendFaq = Commands.slash("sendfaq", "Prints out all configured FAQ entries in the FAQ channel");

        CommandData reload = Commands.slash("reloadconfig", "Reloads the bot's configuration");

        CommandData createTicketPanel = Commands.slash("createticketpanel", "Creates a ticket panel in a specified channel")
                .addOptions(new OptionData(OptionType.CHANNEL, "channel", "The channel where the ticket panel should be created", true));

        return List.of(faq, info, close, sendFaq, reload, createTicketPanel);
    }
}
