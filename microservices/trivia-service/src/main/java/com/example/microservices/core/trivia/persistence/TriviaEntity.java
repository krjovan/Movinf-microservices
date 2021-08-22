package com.example.microservices.core.trivia.persistence;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="trivia")
@CompoundIndex(name = "mov-tri-id", unique = true, def = "{'movieId': 1, 'triviaId' : 1}")
public class TriviaEntity {

    @Id
    private String id;

    @Version
    private Integer version;
    
    private int movieId;
    private int triviaId;
    private Date publishDate;
    private String content;
	private boolean spoiler;

    public TriviaEntity() {
    }

    public TriviaEntity(
		int movieId,
    	int triviaId,
    	Date publishDate,
    	String content,
    	boolean spoiler) {
    	
    	this.movieId = movieId;
        this.triviaId = triviaId;
        this.publishDate = publishDate;
        this.content = content;
        this.spoiler = spoiler;
    }

    public String getId() {
        return id;
    }

    public Integer getVersion() {
        return version;
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

	public void setId(String id) {
        this.id = id;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
}
