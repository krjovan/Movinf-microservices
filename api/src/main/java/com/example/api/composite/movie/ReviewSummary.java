package com.example.api.composite.movie;

import java.util.Date;

public class ReviewSummary {

    private final int reviewId;
    private final Date publishDate;
    private final String title;
    private final String content;
    private final int rating;
    
    public ReviewSummary() {
        this.reviewId = 0;
        this.publishDate = null;
        this.title = null;
        this.content = null;
        this.rating = 0;
    }

    public ReviewSummary(int reviewId, Date publishDate, String title, String content, int rating) {
        this.reviewId = reviewId;
        this.publishDate = publishDate;
        this.title = title;
        this.content = content;
        this.rating = rating;
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
}
