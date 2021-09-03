package com.example.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.example.api.core.crazycredit.*;
import com.example.api.core.movie.*;
import com.example.api.core.review.*;
import com.example.api.core.trivia.*;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.HttpErrorInfo;

import java.io.IOException;

import static reactor.core.publisher.Flux.empty;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

@EnableBinding(MovieCompositeIntegration.MessageSources.class)
@Component
public class MovieCompositeIntegration implements MovieService, TriviaService, ReviewService, CrazyCreditService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieCompositeIntegration.class);

    private final String movieServiceUrl = "http://movie";
    private final String triviaServiceUrl = "http://trivia";
    private final String reviewServiceUrl = "http://review";
    private final String crazyCreditServiceUrl = "http://crazycredit";
    
    private final ObjectMapper mapper;
    private final WebClient.Builder webClientBuilder;
    
    private WebClient webClient;
    
    private MessageSources messageSources;

    public interface MessageSources {

        String OUTPUT_MOVIES = "output-movies";
        String OUTPUT_TRIVIA = "output-trivia";
        String OUTPUT_REVIEWS = "output-reviews";
        String OUTPUT_CRAZYCREDITS = "output-crazycredits";

        @Output(OUTPUT_MOVIES)
        MessageChannel outputMovies();

        @Output(OUTPUT_TRIVIA)
        MessageChannel outputTrivia();

        @Output(OUTPUT_REVIEWS)
        MessageChannel outputReviews();
        
        @Output(OUTPUT_CRAZYCREDITS)
        MessageChannel outputCrazyCredits();
    }

    @Autowired
    public MovieCompositeIntegration(
    	WebClient.Builder webClientBuilder,
        ObjectMapper mapper,
        MessageSources messageSources
    ) {
    	this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
    }
    
    @Override
    public Movie createMovie(Movie body) {
    	messageSources.outputMovies().send(MessageBuilder.withPayload(new Event(CREATE, body.getMovieId(), body)).build());
        return body;
    }

    @Override
    public Mono<Movie> getMovie(int movieId) {
    	String url = movieServiceUrl + "/movie/" + movieId;
        LOG.debug("Will call the getMovie API on URL: {}", url);
        return getWebClient().get().uri(url).retrieve().bodyToMono(Movie.class).log().onErrorMap(WebClientResponseException.class, ex -> handleException(ex));
    }
    
    @Override
    public void deleteMovie(int movieId) {
    	messageSources.outputMovies().send(MessageBuilder.withPayload(new Event(DELETE, movieId, null)).build());
    }
    
    @Override
    public Trivia createTrivia(Trivia body) {
    	messageSources.outputTrivia().send(MessageBuilder.withPayload(new Event(CREATE, body.getMovieId(), body)).build());
        return body;
    }
    
    @Override
    public Flux<Trivia> getTrivia(int movieId) {
    	String url = triviaServiceUrl + "/trivia?movieId=" + movieId;
    	LOG.debug("Will call the getTrivia API on URL: {}", url);
    	// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Trivia.class).log().onErrorResume(error -> empty());
    }
    
    @Override
    public void deleteTrivia(int movieId) {
    	messageSources.outputTrivia().send(MessageBuilder.withPayload(new Event(DELETE, movieId, null)).build());
    }
    
    @Override
    public Review createReview(Review body) {
    	messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(CREATE, body.getMovieId(), body)).build());
        return body;
    }

    @Override
    public Flux<Review> getReviews(int movieId) {
    	String url = reviewServiceUrl + "/review?movieId=" + movieId;
    	LOG.debug("Will call the getReviews API on URL: {}", url);
        // Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(Review.class).log().onErrorResume(error -> empty());
    }
    
    @Override
    public void deleteReviews(int movieId) {
    	messageSources.outputReviews().send(MessageBuilder.withPayload(new Event(DELETE, movieId, null)).build());
    }
    
    @Override
    public CrazyCredit createCrazyCredit(CrazyCredit body) {
    	messageSources.outputCrazyCredits().send(MessageBuilder.withPayload(new Event(CREATE, body.getMovieId(), body)).build());
        return body;
    }

    @Override
	public Flux<CrazyCredit> getCrazyCredits(int movieId) {
    	String url = crazyCreditServiceUrl + "/crazy-credit?movieId=" + movieId;
    	LOG.debug("Will call the getCrazyCredits API on URL: {}", url);
    	// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(CrazyCredit.class).log().onErrorResume(error -> empty());
	}
    
    @Override
    public void deleteCrazyCredits(int movieId) {
    	messageSources.outputCrazyCredits().send(MessageBuilder.withPayload(new Event(DELETE, movieId, null)).build());
    }
    
    public Mono<Health> getMovieHealth() {
        return getHealth(movieServiceUrl);
    }
    
    public Mono<Health> getTriviaHealth() {
        return getHealth(triviaServiceUrl);
    }
    
    public Mono<Health> getReviewHealth() {
        return getHealth(reviewServiceUrl);
    }
    
    public Mono<Health> getCrazyCreditHealth() {
        return getHealth(crazyCreditServiceUrl);
    }
    
    private Mono<Health> getHealth(String url) {
        url += "/actuator/health";
        LOG.debug("Will call the Health API on URL: {}", url);
        return getWebClient().get().uri(url).retrieve().bodyToMono(String.class)
            .map(s -> new Health.Builder().up().build())
            .onErrorResume(ex -> Mono.just(new Health.Builder().down(ex).build()))
            .log();
    }
    
    private WebClient getWebClient() {
        if (webClient == null) {
            webClient = webClientBuilder.build();
        }
        return webClient;
    }
    
    private Throwable handleException(Throwable ex) {
        if (!(ex instanceof WebClientResponseException)) {
            LOG.warn("Got a unexpected error: {}, will rethrow it", ex.toString());
            return ex;
        }
        WebClientResponseException wcre = (WebClientResponseException)ex;
        switch (wcre.getStatusCode()) {
        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(wcre));
        case UNPROCESSABLE_ENTITY :
            return new InvalidInputException(getErrorMessage(wcre));
        default:
            LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", wcre.getStatusCode());
            LOG.warn("Error body: {}", wcre.getResponseBodyAsString());
            return ex;
        }
    }

    private String getErrorMessage(WebClientResponseException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

}
