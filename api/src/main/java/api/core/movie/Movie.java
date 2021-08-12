package api.core.movie;

import java.sql.Date;

public class Movie {
    private final int movieId;
    private final String title;
    private final Date releaseDate;	
	private final String country;
    private final int budget;
    private final int gross;
    private final int runtime;
    private final String serviceAddress;

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
}
