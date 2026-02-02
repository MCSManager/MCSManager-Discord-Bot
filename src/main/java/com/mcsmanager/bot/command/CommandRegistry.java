package com.mcsmanager.bot.command;

import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

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

        CommandData shortcut = Commands.slash("shortcut", "Manage and execute shortcuts")
            .addSubcommands(
                new SubcommandData("add", "Add a new shortcut")
                    .addOptions(
                        new OptionData(OptionType.STRING, "id", "Unique ID for the shortcut (used to execute it)", true),
                        new OptionData(OptionType.STRING, "description", "Description of what this shortcut does", true),
                        new OptionData(OptionType.STRING, "title", "Title of the embed message", true),
                        new OptionData(OptionType.STRING, "message", "Description text of the embed message", true)
                    ),
                new SubcommandData("remove", "Remove a shortcut")
                    .addOptions(
                        new OptionData(OptionType.STRING, "id", "ID of the shortcut to remove", true)
                    ),
                new SubcommandData("execute", "Execute a shortcut")
                    .addOptions(
                        new OptionData(OptionType.STRING, "id", "ID of the shortcut to execute", true)
                    ),
                new SubcommandData("list", "List all available shortcuts")
            );

        return List.of(faq, info, close, sendFaq, reload, shortcut);
    }
}
