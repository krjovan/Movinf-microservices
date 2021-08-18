package com.example.microservices.core.review.persistence;

import java.sql.Date;

import javax.persistence.*;

@Entity
@Table(name = "reviews", indexes = { @Index(name = "reviews_unique_idx", unique = true, columnList = "movieId,reviewId") })
public class ReviewEntity {

    @Id @GeneratedValue
    private int id;

    @Version
    private int version;

    private int movieId;
    private int reviewId;
    private Date publishDate;
    private String title;
    private String content;
    private int rating;

    public ReviewEntity() {
    }

    public ReviewEntity(
		int movieId,
    	int reviewId,
    	Date publishDate,
    	String title,
    	String content,
    	int rating) {
    	
    	this.movieId = movieId;
        this.reviewId = reviewId;
        this.publishDate = publishDate;
        this.title = title;
        this.content = content;
        this.rating = rating;
    }

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

	public int getMovieId() {
		return movieId;
	}

	public void setMovieId(int movieId) {
		this.movieId = movieId;
	}

	public int getReviewId() {
		return reviewId;
	}

	public void setReviewId(int reviewId) {
		this.reviewId = reviewId;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}
}
