package com.mcsmanager.bot.shortcuts;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a shortcut that can be executed as a slash command.
 * Shortcuts are stored in JSON and can be managed at runtime.
 *
 * @author SkyKing_PX
 */
public class Shortcut {
    @JsonProperty("id")
    private String id;

    @JsonProperty("description")
    private String description;

    @JsonProperty("messageTitle")
    private String messageTitle;

    @JsonProperty("messageDescription")
    private String messageDescription;

    /**
     * No-arg constructor for Jackson deserialization.
     */
    public Shortcut() {
    }

    /**
     * Creates a new Shortcut.
     *
     * @param id The command ID (used as /shortcut execute <id>)
     * @param description The description of what this shortcut does
     * @param messageTitle The title of the embed message
     * @param messageDescription The description text of the embed message
     */
    public Shortcut(String id, String description, String messageTitle, String messageDescription) {
        this.id = id;
        this.description = description;
        this.messageTitle = messageTitle;
        this.messageDescription = messageDescription;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getMessageTitle() {
        return messageTitle;
    }

    public String getMessageDescription() {
        // Convert escape sequences like \n to actual newlines
        return messageDescription != null ? messageDescription.replace("\\n", "\n") : null;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setMessageTitle(String messageTitle) {
        this.messageTitle = messageTitle;
    }

    public void setMessageDescription(String messageDescription) {
        this.messageDescription = messageDescription;
    }

    @Override
    public String toString() {
        return "Shortcut{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", messageTitle='" + messageTitle + '\'' +
                ", messageDescription='" + messageDescription + '\'' +
                '}';
    }
}
