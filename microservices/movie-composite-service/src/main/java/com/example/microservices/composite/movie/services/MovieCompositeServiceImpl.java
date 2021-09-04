package com.example.microservices.composite.movie.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import com.example.api.composite.movie.*;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.api.core.movie.Movie;
import com.example.api.core.review.Review;
import com.example.api.core.trivia.Trivia;
import com.example.util.http.ServiceUtil;

import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class MovieCompositeServiceImpl implements MovieCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieCompositeServiceImpl.class);
    
    private final SecurityContext nullSC = new SecurityContextImpl();
	
	private final ServiceUtil serviceUtil;
    private final MovieCompositeIntegration integration;

    @Autowired
    public MovieCompositeServiceImpl(ServiceUtil serviceUtil, MovieCompositeIntegration integration) {
        this.serviceUtil = serviceUtil;
        this.integration = integration;
    }
    
    @Override
    public Mono<Void> createCompositeMovie(MovieAggregate body) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalCreateCompositeMovie(sc, body)).then();
    }

    public void internalCreateCompositeMovie(SecurityContext sc, MovieAggregate body) {
        try {
        	
        	logAuthorizationInfo(sc);

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

            LOG.debug("createCompositeMovie: composite entities created for movieId: {}", body.getMovieId());

        } catch (RuntimeException re) {
            LOG.warn("createCompositeMovie failed: {}", re.toString());
            throw re;
        }
    }
    
    @Override
    public Mono<MovieAggregate> getCompositeMovie(int movieId) {
        return Mono.zip(
        		values -> createMovieAggregate((SecurityContext) values[0], (Movie) values[1], (List<Trivia>) values[2], (List<Review>) values[3], (List<CrazyCredit>) values[4], serviceUtil.getServiceAddress()),
                ReactiveSecurityContextHolder.getContext().defaultIfEmpty(nullSC),
                integration.getMovie(movieId),
                integration.getTrivia(movieId).collectList(),
                integration.getReviews(movieId).collectList(),
                integration.getCrazyCredits(movieId).collectList())
            .doOnError(ex -> LOG.warn("getCompositeMovie failed: {}", ex.toString()))
            .log();
    }
    
    @Override
    public Mono<Void> deleteCompositeMovie(int movieId) {
        return ReactiveSecurityContextHolder.getContext().doOnSuccess(sc -> internalDeleteCompositeMovie(sc, movieId)).then();
    }

    private void internalDeleteCompositeMovie(SecurityContext sc, int movieId) {
    	try {
    		logAuthorizationInfo(sc);
            LOG.debug("deleteCompositeMovie: Deletes a movie aggregate for movieId: {}", movieId);

            integration.deleteMovie(movieId);
            integration.deleteTrivia(movieId);
            integration.deleteReviews(movieId);
            integration.deleteCrazyCredits(movieId);

            LOG.debug("deleteCompositeMovie: aggregate entities deleted for movieId: {}", movieId);

        } catch (RuntimeException re) {
            LOG.warn("deleteCompositeMovie failed: {}", re.toString());
            throw re;
        }
    }
	
	private MovieAggregate createMovieAggregate(SecurityContext sc, Movie movie, List<Trivia> trivia, List<Review> reviews, List<CrazyCredit> crazyCredits, String serviceAddress) {
		logAuthorizationInfo(sc);
		
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
	
	private void logAuthorizationInfo(SecurityContext sc) {
        if (sc != null && sc.getAuthentication() != null && sc.getAuthentication() instanceof JwtAuthenticationToken) {
            Jwt jwtToken = ((JwtAuthenticationToken)sc.getAuthentication()).getToken();
            logAuthorizationInfo(jwtToken);
        } else {
            LOG.warn("No JWT based Authentication supplied, running tests are we?");
        }
    }

    private void logAuthorizationInfo(Jwt jwt) {
        if (jwt == null) {
            LOG.warn("No JWT supplied, running tests are we?");
        } else {
            if (LOG.isDebugEnabled()) {
                URL issuer = jwt.getIssuer();
                List<String> audience = jwt.getAudience();
                Object subject = jwt.getClaims().get("sub");
                Object scopes = jwt.getClaims().get("scope");
                Object expires = jwt.getClaims().get("exp");

                LOG.debug("Authorization info: Subject: {}, scopes: {}, expires {}: issuer: {}, audience: {}", subject, scopes, expires, issuer, audience);
            }
        }
    }

}
