package com.example.api.core.trivia;

import java.sql.Date;

public class Trivia {
    private int movieId;
    private int triviaId;
    private Date publishDate;
    private String content;
	private boolean spoiler;
    private String serviceAddress;

    public Trivia() {
    	movieId = 0;
    	triviaId = 0;
    	publishDate = null;
        content = null;
        spoiler = false;
        serviceAddress = null;
    }

    public Trivia(
    	int movieId,
    	int triviaId,
    	Date publishDate,
    	String content,
    	boolean spoiler,
    	String serviceAddress) {
    	
        this.movieId = movieId;
        this.triviaId = triviaId;
        this.publishDate = publishDate;
        this.content = content;
        this.spoiler = spoiler;
        this.serviceAddress = serviceAddress;
    }

    public int getMovieId() {
		return movieId;
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

	public String getServiceAddress() {
        return serviceAddress;
    }

	public void setMovieId(int movieId) {
		this.movieId = movieId;
	}

	public void setTriviaId(int triviaId) {
		this.triviaId = triviaId;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
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
