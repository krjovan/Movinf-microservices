package com.example.microservices.core.movie.persistence;

import java.util.Date;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import static java.lang.String.format;

@Document(collection="movies")
public class MovieEntity {

    @Id
    private String id;

    @Version
    private Integer version;

    @Indexed(unique = true)
    private int movieId;

    private String title;
    private Date releaseDate;	
	private String country;
    private int budget;
    private int gross;
    private int runtime;

    public MovieEntity() {
    }

    public MovieEntity(
    	int movieId,
    	String title,
    	Date releaseDate,
    	String country,
    	int budget,
    	int gross,
    	int runtime) {
    	
    	this.movieId = movieId;
    	this.title = title;
    	this.releaseDate = releaseDate;
    	this.country = country;
    	this.budget = budget;
    	this.gross = gross;
    	this.runtime = runtime;
    }
    
    @Override
    public String toString() {
        return format("MovieEntity: %s", movieId);
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

	public String getTitle() {
		return title;
	}

	public Date getReleaseDate() {
		return releaseDate;
	}

	public String getCountry() {
		return country;
	}

	public int getBudget() {
		return budget;
	}

	public int getGross() {
		return gross;
	}

	public int getRuntime() {
		return runtime;
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

	public void setTitle(String title) {
		this.title = title;
	}

	public void setReleaseDate(Date releaseDate) {
		this.releaseDate = releaseDate;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public void setBudget(int budget) {
		this.budget = budget;
	}

	public void setGross(int gross) {
		this.gross = gross;
	}

	public void setRuntime(int runtime) {
		this.runtime = runtime;
	}
}
