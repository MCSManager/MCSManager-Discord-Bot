package com.mcsmanager.bot.storage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JSON-based storage system for managing voting data on suggestion forums.
 * Handles vote tracking, user vote history, and persistent storage operations.
 * Thread-safe implementation using synchronized methods.
 * 
 * @author SkyKing_PX
 */
public class VoteStorage {

    /** JSON file for storing vote data */
    private final File file = new File("votes.json");
    /** Jackson ObjectMapper for JSON operations */
    private final ObjectMapper mapper = new ObjectMapper();
    /** Root JSON node containing all vote data */
    private ObjectNode root;

    /**
     * Initializes the vote storage system.
     * Creates a new storage file if one doesn't exist, otherwise loads existing data.
     * 
     * @throws IOException If there is an error reading or creating the storage file
     */
    public VoteStorage() throws IOException {
        if (!file.exists()) {
            root = mapper.createObjectNode();
            save();
        } else {
            root = (ObjectNode) mapper.readTree(file);
        }
    }

    /**
     * Retrieves a user's vote for a specific thread.
     * 
     * @param threadID Discord thread ID to check
     * @param userId Discord user ID to check
     * @return "up", "down", or null if no vote exists
     */
    public synchronized String getUserVote(String threadID, String userId) {
        JsonNode msg = root.path(threadID).path("voters");
        return msg.path(userId).asText(null);
    }

    /**
     * Saves a user's vote for a specific thread.
     * Creates thread data structure if it doesn't exist.
     * 
     * @param threadID Discord thread ID to vote on
     * @param userId Discord user ID who is voting
     * @param voteType Type of vote: "up" or "down"
     * @throws IOException If there is an error saving to the storage file
     */
    public synchronized void saveUserVote(String threadID, String userId, String voteType) throws IOException {
        ObjectNode msgNode = (ObjectNode) root.get(threadID);
        if (msgNode == null) {
            msgNode = mapper.createObjectNode();
            msgNode.put("up", 0);
            msgNode.put("down", 0);
            msgNode.set("voters", mapper.createObjectNode());
            root.set(threadID, msgNode);
        }

        ObjectNode voters = (ObjectNode) msgNode.with("voters");
        voters.put(userId, voteType);
        save();
    }

    /**
     * Sets the vote count for a specific thread.
     * Creates thread data structure if it doesn't exist.
     * 
     * @param threadID Discord thread ID to update
     * @param up Number of upvotes
     * @param down Number of downvotes
     * @throws IOException If there is an error saving to the storage file
     */
    public synchronized void setVoteCount(String threadID, int up, int down) throws IOException {
        ObjectNode msgNode = (ObjectNode) root.get(threadID);
        if (msgNode == null) {
            msgNode = mapper.createObjectNode();
            root.set(threadID, msgNode);
        }
        msgNode.put("up", up);
        msgNode.put("down", down);
        save();
    }

    /**
     * Removes all vote data for a specific thread.
     * 
     * @param threadID Discord thread ID to remove data for
     * @throws IOException If there is an error saving to the storage file
     */
    public synchronized void removeAllVotes(String threadID) throws IOException {
        if (root.has(threadID)) {
            root.remove(threadID);
            save();
        }
    }

    /**
     * Loads all vote data from storage.
     * 
     * @return Map where key is thread ID and value is int array [upvotes, downvotes]
     */
    public synchronized Map<String, int[]> loadAllVotes() {
        Map<String, int[]> result = new HashMap<>();
        root.fields().forEachRemaining(entry -> {
            JsonNode node = entry.getValue();
            int up = node.path("up").asInt(0);
            int down = node.path("down").asInt(0);
            result.put(entry.getKey(), new int[]{up, down});
        });
        return result;
    }

    /**
     * Saves the current vote data to the JSON file.
     * 
     * @throws IOException If there is an error writing to the storage file
     */
    private void save() throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, root);
    }
}
