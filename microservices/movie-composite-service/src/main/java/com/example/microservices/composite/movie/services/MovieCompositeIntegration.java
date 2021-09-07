package com.example.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
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
import java.net.URI;
import java.time.Duration;

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
    
    private final MessageSources messageSources;

    private final int movieServiceTimeoutSec;

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
        MessageSources messageSources,
        @Value("${app.movie-service.timeoutSec}") int movieServiceTimeoutSec
    ) {
    	this.webClientBuilder = webClientBuilder;
        this.mapper = mapper;
        this.messageSources = messageSources;
        this.movieServiceTimeoutSec = movieServiceTimeoutSec;
    }
    
    @Override
    public Movie createMovie(Movie body) {
    	messageSources.outputMovies().send(MessageBuilder.withPayload(new Event(CREATE, body.getMovieId(), body)).build());
        return body;
    }

    @Retry(name = "movie")
    @CircuitBreaker(name = "movie")
    @Override
    public Mono<Movie> getMovie(int movieId, int delay, int faultPercent) {

        URI url = UriComponentsBuilder.fromUriString(movieServiceUrl + "/movie/{movieId}?delay={delay}&faultPercent={faultPercent}").build(movieId, delay, faultPercent);
        LOG.debug("Will call the getMovie API on URL: {}", url);
        return getWebClient().get().uri(url)
                .retrieve().bodyToMono(Movie.class).log()
                .onErrorMap(WebClientResponseException.class, ex -> handleException(ex))
                .timeout(Duration.ofSeconds(movieServiceTimeoutSec));
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
    	URI url = UriComponentsBuilder.fromUriString(triviaServiceUrl + "/trivia?movieId={movieId}").build(movieId);
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
    	URI url = UriComponentsBuilder.fromUriString(reviewServiceUrl + "/review?movieId={movieId}").build(movieId);
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
    	URI url = UriComponentsBuilder.fromUriString(crazyCreditServiceUrl + "/crazy-credit?movieId={movieId}").build(movieId);
    	LOG.debug("Will call the getCrazyCredits API on URL: {}", url);
    	// Return an empty result if something goes wrong to make it possible for the composite service to return partial responses
        return getWebClient().get().uri(url).retrieve().bodyToFlux(CrazyCredit.class).log().onErrorResume(error -> empty());
	}
    
    @Override
    public void deleteCrazyCredits(int movieId) {
    	messageSources.outputCrazyCredits().send(MessageBuilder.withPayload(new Event(DELETE, movieId, null)).build());
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
