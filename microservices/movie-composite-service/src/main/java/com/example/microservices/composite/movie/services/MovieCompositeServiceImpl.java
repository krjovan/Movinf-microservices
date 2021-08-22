package com.example.microservices.composite.movie.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import com.example.api.composite.movie.*;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.api.core.movie.Movie;
import com.example.api.core.review.Review;
import com.example.api.core.trivia.Trivia;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.ServiceUtil;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MovieCompositeServiceImpl implements MovieCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieCompositeServiceImpl.class);
	
	private final ServiceUtil serviceUtil;
    private  MovieCompositeIntegration integration;

    @Autowired
    public MovieCompositeServiceImpl(ServiceUtil serviceUtil, MovieCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }
    
    @Override
    public void createCompositeMovie(MovieAggregate body) {
        try {

            LOG.debug("createCompositeMovie: creates a new composite entity for movieId: {}", body.getMovieId());

            Movie movie = new Movie(body.getMovieId(), body.getTitle(), body.getReleaseDate(), body.getCountry(),
            						body.getBudget(), body.getGross(), body.getRuntime(), null);
            integration.createMovie(movie);

            if (body.getTrivia() != null) {
                body.getTrivia().forEach(r -> {
                    Trivia trivia = new Trivia(body.getMovieId(), r.getTriviaId(), r.getPublishDate(), r.getContent(), r.isSpoiler(), null);
                    integration.createTrivia(trivia);
                });
            }

            if (body.getReviews() != null) {
                body.getReviews().forEach(r -> {
                    Review review = new Review(body.getMovieId(), r.getReviewId(), r.getPublishDate(), r.getTitle(),
                    						   r.getContent(), r.getRating(), null);
                    integration.createReview(review);
                });
            }
            
            if (body.getCrazyCredits() != null) {
                body.getCrazyCredits().forEach(r -> {
                    CrazyCredit crazyCredit = new CrazyCredit(body.getMovieId(), r.getCrazyCreditId(), r.getContent(), r.isSpoiler(), null);
                    integration.createCrazyCredit(crazyCredit);
                });
            }

            LOG.debug("createCompositeMovie: composite entites created for movieId: {}", body.getMovieId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeMovie failed", re);
            throw re;
        }
    }
    
    @Override
    public MovieAggregate getCompositeMovie(int movieId) {
        LOG.debug("getCompositeMovie: lookup a movie aggregate for movieId: {}", movieId);
		Movie movie = integration.getMovie(movieId);
        if (movie == null) throw new NotFoundException("No movie found for movieId: " + movieId);

        List<Trivia> trivia = integration.getTrivia(movieId);

        List<Review> reviews = integration.getReviews(movieId);
        
        List<CrazyCredit> crazyCredits = integration.getCrazyCredits(movieId);
        
        LOG.debug("getCompositeMovie: aggregate entity found for movieId: {}", movieId);

        return createMovieAggregate(movie, trivia, reviews, crazyCredits, serviceUtil.getServiceAddress());
	}
    
    @Override
    public void deleteCompositeMovie(int movieId) {

        LOG.debug("deleteCompositeMovie: Deletes a movie aggregate for movieId: {}", movieId);

        integration.deleteMovie(movieId);

        integration.deleteTrivia(movieId);

        integration.deleteReviews(movieId);
        
        integration.deleteCrazyCredits(movieId);

        LOG.debug("getCompositeMovie: aggregate entities deleted for movieId: {}", movieId);
    }
	
	private MovieAggregate createMovieAggregate(Movie movie, List<Trivia> trivia, List<Review> reviews, List<CrazyCredit> crazyCredits, String serviceAddress) {

        // 1. Setup movie info
        int movieId = movie.getMovieId();
        String title = movie.getTitle();
        Date releaseDate = movie.getReleaseDate();
        String country = movie.getCountry();
        int budget = movie.getBudget();
        int gross = movie.getGross();
        int runtime = movie.getRuntime();

        // 2. Copy summary trivia info, if available
        List<TriviaSummary> triviaSummaries = (trivia == null) ? null :
        	trivia.stream()
                .map(r -> new TriviaSummary(r.getTriviaId(), r.getPublishDate(), r.getContent(), r.isSpoiler()))
                .collect(Collectors.toList());

        // 3. Copy summary review info, if available
        List<ReviewSummary> reviewSummaries = (reviews == null)  ? null :
            reviews.stream()
                .map(r -> new ReviewSummary(r.getReviewId(), r.getPublishDate(), r.getTitle(), r.getContent(), r.getRating()))
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

        return new MovieAggregate(movieId, title, releaseDate, country, budget, gross, runtime,
        						  triviaSummaries, reviewSummaries, crazyCreditSummaries, serviceAddresses);
    }

}
