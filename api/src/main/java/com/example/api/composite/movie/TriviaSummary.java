package com.example.api.composite.movie;

import java.util.Date;

public class TriviaSummary {

    private final int triviaId;
    private final Date publishDate;
    private final String content;
    private final boolean spoiler;

    public TriviaSummary() {
    	this.triviaId = 0;
    	this.publishDate = null;
        this.content = null;
        this.spoiler = false;
    }
    
    public TriviaSummary(int triviaId, Date publishDate, String content, boolean spoiler) {
        this.triviaId = triviaId;
        this.publishDate = publishDate;
        this.content = content;
        this.spoiler = spoiler;
    }

    public int getTriviaId() {
        return triviaId;
    }

    public Date getPublishDate() {
		return publishDate;
	}

	public String getContent() {
        return content;
    }

    public boolean isSpoiler() {
        return spoiler;
    }
}
