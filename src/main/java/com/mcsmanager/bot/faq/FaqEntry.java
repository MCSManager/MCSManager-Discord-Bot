package com.mcsmanager.bot.faq;

/**
 * Data model for FAQ entries loaded from configuration.
 * Represents a single question-answer pair with optional images.
 * 
 * @author SkyKing_PX
 */
public class FaqEntry {
    /** The FAQ question text */
    private String question;
    /** The FAQ answer text */
    private String answer;
    /** URL for a full-size image attachment */
    private String imageUrl;
    /** URL for a thumbnail image */
    private String thumbnailUrl;

    /**
     * Default constructor for SnakeYAML deserialization.
     */
    public FaqEntry() {
        // Constructor for SnakeYAML
    }

    /**
     * Creates a new FAQ entry with all fields.
     * 
     * @param question The FAQ question
     * @param answer The FAQ answer
     * @param imageUrl URL for full-size image (optional)
     * @param thumbnailUrl URL for thumbnail image (optional)
     */
    public FaqEntry(String question, String answer, String imageUrl, String thumbnailUrl) {
        this.question = question;
        this.answer = answer;
        this.imageUrl = imageUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    /** @return The FAQ question */
    public String getQuestion() {
        return question;
    }

    /** @param question The FAQ question to set */
    public void setQuestion(String question) {
        this.question = question;
    }

    /** @return The FAQ answer */
    public String getAnswer() {
        return answer;
    }

    /** @param answer The FAQ answer to set */
    public void setAnswer(String answer) {
        this.answer = answer;
    }

    /** @return The image URL */
    public String getImageUrl() {
        return imageUrl;
    }

    /** @param imageUrl The image URL to set */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /** @return The thumbnail URL */
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    /** @param thumbnailUrl The thumbnail URL to set */
    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
