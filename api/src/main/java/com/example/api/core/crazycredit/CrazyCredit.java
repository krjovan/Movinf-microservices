package com.example.api.core.crazycredit;

public class CrazyCredit {
	
	private final int movieId;
    private final int crazyCreditId;
    private final String content;
    private final boolean spoiler;
    private final String serviceAddress;

    public CrazyCredit() {
        movieId = 0;
        crazyCreditId = 0;
        content = null;
        spoiler = false;
        serviceAddress = null;
    }

    public CrazyCredit(
    	int movieId,
    	int crazyCreditId,
    	String content,
    	boolean spoiler,
    	String serviceAddress) {
    	
        this.movieId = movieId;
        this.crazyCreditId = crazyCreditId;
        this.content = content;
        this.spoiler = spoiler;
        this.serviceAddress = serviceAddress;
    }

	public int getMovieId() {
		return movieId;
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

	public String getServiceAddress() {
        return serviceAddress;
    }
}
