package com.mcsmanager.bot.command;

import com.mcsmanager.bot.Config;
import com.mcsmanager.bot.shortcuts.Shortcut;
import com.mcsmanager.bot.shortcuts.ShortcutStorage;
import com.mcsmanager.bot.util.EmbedUtils;
import com.mcsmanager.bot.util.LogUtils;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Handles shortcut slash commands.
 * Supports add, remove, and execute subcommands for managing shortcuts.
 *
 * @author SkyKing_PX
 */
public class ShortcutCommand extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("shortcut")) {
            return;
        }

        String subcommand = event.getSubcommandName();

        if (subcommand == null) {
            event.reply("Invalid subcommand").setEphemeral(true).queue();
            return;
        }

        switch (subcommand) {
            case "add" -> handleAdd(event);
            case "remove" -> handleRemove(event);
            case "execute" -> handleExecute(event);
            case "list" -> handleList(event);
            default -> event.reply("Unknown subcommand: " + subcommand).setEphemeral(true).queue();
        }
    }

    /**
     * Handles the 'add' subcommand to create a new shortcut.
     */
    private void handleAdd(SlashCommandInteractionEvent event) {
        // Check if user has moderator role (you can customize this check)
        if (!isModerator(event)) {
            event.reply("You don't have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String id = event.getOption("id", "", opt -> opt.getAsString().toLowerCase());
        String description = event.getOption("description", "", opt -> opt.getAsString());
        String messageTitle = event.getOption("title", "", opt -> opt.getAsString());
        String messageDescription = event.getOption("message", "", opt -> opt.getAsString());

        if (id.isEmpty() || description.isEmpty() || messageTitle.isEmpty() || messageDescription.isEmpty()) {
            event.reply("All fields are required!").setEphemeral(true).queue();
            return;
        }

        if (ShortcutStorage.hasShortcut(id)) {
            event.reply("A shortcut with the ID `" + id + "` already exists!").setEphemeral(true).queue();
            return;
        }

        Shortcut shortcut = new Shortcut(id, description, messageTitle, messageDescription);
        ShortcutStorage.addShortcut(shortcut);

        MessageEmbed embed = EmbedUtils.createSuccess()
                .setTitle("✅ Shortcut Added")
                .addField("ID", "`" + id + "`", false)
                .addField("Description", description, false)
                .addField("Message Title", messageTitle, false)
                .addField("Message Description", messageDescription, false)
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
        LogUtils.logInfo("Shortcut added: " + id);
    }

    /**
     * Handles the 'remove' subcommand to delete a shortcut.
     */
    private void handleRemove(SlashCommandInteractionEvent event) {
        // Check if user has moderator role
        if (!isModerator(event)) {
            event.reply("You don't have permission to use this command.").setEphemeral(true).queue();
            return;
        }

        String id = event.getOption("id", "", opt -> opt.getAsString().toLowerCase());

        if (id.isEmpty()) {
            event.reply("ID is required!").setEphemeral(true).queue();
            return;
        }

        if (!ShortcutStorage.hasShortcut(id)) {
            event.reply("No shortcut found with the ID `" + id + "`!").setEphemeral(true).queue();
            return;
        }

        ShortcutStorage.removeShortcut(id);

        MessageEmbed embed = EmbedUtils.createSuccess()
                .setTitle("✅ Shortcut Removed")
                .setDescription("The shortcut `" + id + "` has been removed.")
                .build();

        event.replyEmbeds(embed).setEphemeral(true).queue();
        LogUtils.logInfo("Shortcut removed: " + id);
    }

    /**
     * Handles the 'execute' subcommand to send a shortcut message.
     */
    private void handleExecute(SlashCommandInteractionEvent event) {
        String id = event.getOption("id", "", opt -> opt.getAsString().toLowerCase());

        if (id.isEmpty()) {
            event.reply("ID is required!").setEphemeral(true).queue();
            return;
        }

        Shortcut shortcut = ShortcutStorage.getShortcut(id);

        if (shortcut == null) {
            event.reply("No shortcut found with the ID `" + id + "`!").setEphemeral(true).queue();
            return;
        }

        MessageEmbed embed = EmbedUtils.createDefault()
                .setTitle(shortcut.getMessageTitle())
                .setDescription(shortcut.getMessageDescription())
                .build();

        event.replyEmbeds(embed).queue();
        LogUtils.logInfo("Shortcut executed: " + id);
    }

    /**
     * Handles the 'list' subcommand to display all available shortcuts.
     */
    private void handleList(SlashCommandInteractionEvent event) {
        java.util.List<Shortcut> allShortcuts = ShortcutStorage.getAllShortcuts();

        if (allShortcuts.isEmpty()) {
            event.reply("No shortcuts available.").setEphemeral(true).queue();
            return;
        }

        // Build the list embed
        net.dv8tion.jda.api.EmbedBuilder embed = EmbedUtils.createDefault()
                .setTitle("📋 Available Shortcuts")
                .setDescription("Here are all the available shortcuts you can execute:");

        for (Shortcut shortcut : allShortcuts) {
            embed.addField(
                    "`/" + shortcut.getId() + "`",
                    shortcut.getDescription(),
                    false
            );
        }

        event.replyEmbeds(embed.build()).queue();
        LogUtils.logInfo("Listed all shortcuts: " + allShortcuts.size());
    }

    /**
     * Checks if the user has moderator permissions.
     * Checks against configured moderator roles from Config.
     */
    private boolean isModerator(SlashCommandInteractionEvent event) {
        if (event.getMember() == null) {
            return false;
        }

        String[] modRoleIds;
        try {
            modRoleIds = Config.get().getRoles().getModerators();
        } catch (Exception e) {
            LogUtils.logException("Error loading mod roles", e);
            return false;
        }

        List<String> modRolesList = List.of(modRoleIds);
        return event.getMember().getRoles().stream()
                .anyMatch(role -> modRolesList.contains(role.getId()));
    }
}
