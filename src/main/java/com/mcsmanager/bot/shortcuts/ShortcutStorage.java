package com.mcsmanager.bot.shortcuts;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mcsmanager.bot.util.LogUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles storage and management of shortcuts using JSON.
 * Shortcuts are persisted to disk and can be loaded on startup.
 *
 * @author SkyKing_PX
 */
public class ShortcutStorage {
    private static final String SHORTCUTS_FILE = "shortcuts.json";
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static Map<String, Shortcut> shortcuts = new HashMap<>();

    /**
     * Loads shortcuts from the JSON file.
     */
    public static void load() {
        try {
            File file = new File(SHORTCUTS_FILE);

            if (!file.exists()) {
                LogUtils.logInfo("Shortcuts file not found, creating new one.");
                save();
                return;
            }

            ShortcutData data = objectMapper.readValue(file, ShortcutData.class);
            if (data != null && data.shortcuts != null) {
                shortcuts.clear();
                for (Shortcut shortcut : data.shortcuts) {
                    String lowerCaseId = shortcut.getId().toLowerCase();
                    shortcuts.put(lowerCaseId, shortcut);
                    LogUtils.logInfo("Loaded shortcut: " + lowerCaseId);
                }
                LogUtils.logInfo("Loaded " + shortcuts.size() + " shortcuts from file.");
            } else {
                LogUtils.logInfo("Shortcuts file is empty or invalid.");
            }
        } catch (IOException e) {
            LogUtils.logException("Error loading shortcuts", e);
        }
    }

    /**
     * Saves shortcuts to the JSON file.
     */
    public static void save() {
        try {
            ShortcutData data = new ShortcutData();
            data.shortcuts = new ArrayList<>(shortcuts.values());

            objectMapper.writerWithDefaultPrettyPrinter()
                    .writeValue(new File(SHORTCUTS_FILE), data);
            LogUtils.logInfo("Shortcuts saved to file.");
        } catch (IOException e) {
            LogUtils.logException("Error saving shortcuts", e);
        }
    }

    /**
     * Adds a new shortcut.
     *
     * @param shortcut The shortcut to add
     * @return true if added successfully, false if a shortcut with that ID already exists
     */
    public static boolean addShortcut(Shortcut shortcut) {
        String id = shortcut.getId().toLowerCase();
        if (shortcuts.containsKey(id)) {
            return false;
        }
        shortcuts.put(id, shortcut);
        save();
        return true;
    }

    /**
     * Removes a shortcut by ID.
     *
     * @param id The shortcut ID to remove
     * @return true if removed successfully, false if the shortcut doesn't exist
     */
    public static boolean removeShortcut(String id) {
        String lowerCaseId = id.toLowerCase();
        if (!shortcuts.containsKey(lowerCaseId)) {
            return false;
        }
        shortcuts.remove(lowerCaseId);
        save();
        return true;
    }

    /**
     * Gets a shortcut by ID.
     *
     * @param id The shortcut ID
     * @return The shortcut, or null if not found
     */
    public static Shortcut getShortcut(String id) {
        String lowerCaseId = id.toLowerCase();
        LogUtils.logInfo("Searching for shortcut: " + lowerCaseId + ", Available: " + shortcuts.keySet());
        return shortcuts.get(lowerCaseId);
    }

    /**
     * Checks if a shortcut exists.
     *
     * @param id The shortcut ID
     * @return true if the shortcut exists
     */
    public static boolean hasShortcut(String id) {
        return shortcuts.containsKey(id.toLowerCase());
    }

    /**
     * Gets all shortcuts.
     *
     * @return A list of all shortcuts
     */
    public static List<Shortcut> getAllShortcuts() {
        return new ArrayList<>(shortcuts.values());
    }

    /**
     * Internal class for JSON serialization.
     */
    public static class ShortcutData {
        public List<Shortcut> shortcuts;

        /**
         * No-arg constructor for Jackson deserialization.
         */
        public ShortcutData() {
        }
    }
}
