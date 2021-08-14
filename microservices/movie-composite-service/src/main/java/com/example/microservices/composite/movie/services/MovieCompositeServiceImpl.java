package com.example.microservices.composite.movie.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import api.composite.movie.*;
import api.core.movie.Movie;
import api.core.trivia.Trivia;
import api.core.review.Review;
import api.core.crazycredit.CrazyCredit;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

import java.sql.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MovieCompositeServiceImpl implements MovieCompositeService {

	private final ServiceUtil serviceUtil;
    private  MovieCompositeIntegration integration;

    @Autowired
    public MovieCompositeServiceImpl(ServiceUtil serviceUtil, MovieCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }
	
	@Override
	public MovieAggregate getMovie(int movieId) {
		Movie movie = integration.getMovie(movieId);
        if (movie == null) throw new NotFoundException("No movie found for movieId: " + movieId);

        List<Trivia> trivia = integration.getTrivia(movieId);

        List<Review> reviews = integration.getReviews(movieId);
        
        List<CrazyCredit> crazyCredits = integration.getCrazyCredits(movieId);

        return createMovieAggregate(movie, trivia, reviews, crazyCredits, serviceUtil.getServiceAddress());
	}
	
	private MovieAggregate createMovieAggregate(Movie movie, List<Trivia> trivia, List<Review> reviews, List<CrazyCredit> crazyCredits, String serviceAddress) {

        // 1. Setup movie info
        int movieId = movie.getMovieId();
        String title = movie.getTitle();
        Date releaseDate = movie.getReleaseDate();
        String country = movie.getCountry();

        // 2. Copy summary trivia info, if available
        List<TriviaSummary> triviaSummaries = (trivia == null) ? null :
        	trivia.stream()
                .map(r -> new TriviaSummary(r.getTriviaId(), r.getContent(), r.isSpoiler()))
                .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getTitle(), r.getContent(), r.getRating()))
                .collect(Collectors.toList());
        
        // 4. Copy summary crazy credit info, if available
        List<CrazyCreditSummary> crazyCreditSummaries = (crazyCredits == null)  ? null :
        	crazyCredits.stream()
                .map(r -> new CrazyCreditSummary(r.getCrazyCreditId(), r.getContent(), r.isSpoiler()))
                .collect(Collectors.toList());

        // 5. Create info regarding the involved microservices addresses
        String movieAddress = movie.getServiceAddress();
        String reviewAddress = (reviews != null && reviews.size() > 0) ? reviews.get(0).getServiceAddress() : "";
        String triviaAddress = (trivia != null && trivia.size() > 0) ? trivia.get(0).getServiceAddress() : "";
        String crazyCreditAddress = (crazyCredits != null && crazyCredits.size() > 0) ? crazyCredits.get(0).getServiceAddress() : "";
        ServiceAddresses serviceAddresses = new ServiceAddresses(serviceAddress, movieAddress, reviewAddress, triviaAddress, crazyCreditAddress);

        return new MovieAggregate(movieId, title, releaseDate, country, triviaSummaries, reviewSummaries, crazyCreditSummaries, serviceAddresses);
    }

}
