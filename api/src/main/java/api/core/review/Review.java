package api.core.review;

import java.sql.Date;

public class Review {
    private final int movieId;
    private final int reviewId;
    private final Date publishDate;
    private final String title;
    private final String content;
    private final int rating;
    private final String serviceAddress;

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
}
