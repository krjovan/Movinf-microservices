package com.example.api.core.movie;

import java.sql.Date;

public class Movie {
    private int movieId;
    private String title;
    private Date releaseDate;	
	private String country;
    private int budget;
    private int gross;
    private int runtime;
    private String serviceAddress;

    public Movie() {
        movieId = 0;
        title = null;
        releaseDate = null;
        country = null;
        budget = 0;
        gross = 0;
        runtime = 0;
        serviceAddress = null;
    }

    public Movie(
		int movieId,
		String title,
		Date releaseDate,
		String country,
		int budget,
		int gross,
		int runtime,
		String serviceAddress) {
    	
    	this.movieId = movieId;
    	this.title = title;
    	this.releaseDate = releaseDate;
    	this.country = country;
    	this.budget = budget;
    	this.gross = gross;
    	this.runtime = runtime;
    	this.serviceAddress = serviceAddress;
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

    public String getServiceAddress() {
        return serviceAddress;
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

	public void setServiceAddress(String serviceAddress) {
		this.serviceAddress = serviceAddress;
	}
}
