package com.example.api.composite.movie;

public class CrazyCreditSummary {
	
	private final int crazyCreditId;
    private final String content;
    private final boolean spoiler;

    public CrazyCreditSummary() {
        this.crazyCreditId = 0;
        this.content = null;
        this.spoiler = false;
    }
    
    public CrazyCreditSummary(int crazyCreditId, String content, boolean spoiler) {
        this.crazyCreditId = crazyCreditId;
        this.content = content;
        this.spoiler = spoiler;
    }

    public int getCrazyCreditId() {
        return crazyCreditId;
    }

    public String getContent() {
        return content;
    }
    
    public boolean isSpoiler() {
        return spoiler;
    }
}
