package com.example.microservices.core.crazycredit.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection="crazycredits")
@CompoundIndex(name = "mov-cra-id", unique = true, def = "{'movieId': 1, 'crazyCreditId' : 1}")
public class CrazyCreditEntity {

    @Id
    private String id;

    @Version
    private Integer version;
    
    private int movieId;
    private int crazyCreditId;
    private String content;
    private boolean spoiler;

    public CrazyCreditEntity() {
    }

    public CrazyCreditEntity(
		int movieId,
    	int crazyCreditId,
    	String content,
    	boolean spoiler) {
    	
    	this.movieId = movieId;
        this.crazyCreditId = crazyCreditId;
        this.content = content;
        this.spoiler = spoiler;
    }
    
    @Override
    public String toString() {
        return format("CrazyCreditEntity: %s/%d", movieId, crazyCreditId);
    }

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Integer getVersion() {
		return version;
	}

	public void setVersion(Integer version) {
		this.version = version;
	}

	public int getMovieId() {
		return movieId;
	}

	public void setMovieId(int movieId) {
		this.movieId = movieId;
	}

	public int getCrazyCreditId() {
		return crazyCreditId;
	}

	public void setCrazyCreditId(int crazyCreditId) {
		this.crazyCreditId = crazyCreditId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public boolean isSpoiler() {
		return spoiler;
	}

	public void setSpoiler(boolean spoiler) {
		this.spoiler = spoiler;
	}
}
