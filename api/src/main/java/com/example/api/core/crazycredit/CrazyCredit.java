package com.example.api.core.crazycredit;

public class CrazyCredit {
	
	private int movieId;
    private int crazyCreditId;
    private String content;
    private boolean spoiler;
    private String serviceAddress;

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

	public void setMovieId(int movieId) {
		this.movieId = movieId;
	}

	public void setCrazyCreditId(int crazyCreditId) {
		this.crazyCreditId = crazyCreditId;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setSpoiler(boolean spoiler) {
		this.spoiler = spoiler;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}
