package com.example.api.core.review;

import java.util.Date;

public class Review {
    private int movieId;
    private int reviewId;
    private Date publishDate;
    private String title;
    private String content;
    private int rating;
    private String serviceAddress;

    public Review() {
        movieId = 0;
        reviewId = 0;
        publishDate = null;
        title = null;
        content = null;
        rating = 0;
        serviceAddress = null;
    }

    public Review(
    	int movieId,
    	int reviewId,
    	Date publishDate,
    	String title,
    	String content,
    	int rating,
    	String serviceAddress) {
    	
        this.movieId = movieId;
        this.reviewId = reviewId;
        this.publishDate = publishDate;
        this.title = title;
        this.content = content;
        this.rating = rating;
        this.serviceAddress = serviceAddress;
    }

    public int getMovieId() {
		return movieId;
	}

	public int getReviewId() {
		return reviewId;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public String getTitle() {
		return title;
	}

	public String getContent() {
		return content;
	}

	public int getRating() {
		return rating;
	}

	public String getServiceAddress() {
        return serviceAddress;
    }

	public void setMovieId(int movieId) {
		this.movieId = movieId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}
