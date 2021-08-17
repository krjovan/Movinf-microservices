package com.example.api.composite.movie;

import java.sql.Date;
import java.util.List;

public class MovieAggregate {
    private final int movieId;
    private final String title;
    private final Date releaseDate;
	private final String country;
	private final int budget;
    private final int gross;
    private final int runtime;
    private final List<TriviaSummary> trivia;
    private final List<ReviewSummary> reviews;
	private final List<CrazyCreditSummary> crazyCredits;
    private final ServiceAddresses serviceAddresses;
    
    public MovieAggregate() {
    	this.movieId = 0;
        this.title = null;
        this.releaseDate = null;
		this.country = null;
		this.budget = 0;
		this.gross = 0;
		this.runtime = 0;
        this.trivia = null;
        this.reviews = null;
		this.crazyCredits = null;
        this.serviceAddresses = null;
    }

    public MovieAggregate(
        int movieId,
        String title,
		Date releaseDate,
        String country,
        int budget,
        int gross,
        int runtime,
        List<TriviaSummary> trivia,
		List<ReviewSummary> reviews,
        List<CrazyCreditSummary> crazyCredits,
		ServiceAddresses serviceAddresses) {

        this.movieId = movieId;
        this.title = title;
        this.releaseDate = releaseDate;
		this.country = country;
		this.budget = budget;
		this.gross = gross;
		this.runtime = runtime;
        this.trivia = trivia;
        this.reviews = reviews;
		this.crazyCredits = crazyCredits;
        this.serviceAddresses = serviceAddresses;
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

	public List<TriviaSummary> getTrivia() {
        return trivia;
    }

    public List<ReviewSummary> getReviews() {
        return reviews;
    }
	
	public List<CrazyCreditSummary> getCrazyCredits() {
        return crazyCredits;
    }

    public ServiceAddresses getServiceAddresses() {
        return serviceAddresses;
    }
}
