package com.example.microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.example.api.core.crazycredit.*;
import com.example.api.core.movie.*;
import com.example.api.core.review.*;
import com.example.api.core.trivia.*;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;
import com.example.util.http.HttpErrorInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.http.HttpMethod.GET;

@Component
public class MovieCompositeIntegration implements MovieService, TriviaService, ReviewService, CrazyCreditService {

    private static final Logger LOG = LoggerFactory.getLogger(MovieCompositeIntegration.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper mapper;

    private final String movieServiceUrl;
    private final String triviaServiceUrl;
    private final String reviewServiceUrl;
    private final String crazyCreditServiceUrl;

    @Autowired
    public MovieCompositeIntegration(
        RestTemplate restTemplate,
        ObjectMapper mapper,

        @Value("${app.movie-service.host}") String movieServiceHost,
        @Value("${app.movie-service.port}") int    movieServicePort,

        @Value("${app.trivia-service.host}") String triviaServiceHost,
        @Value("${app.trivia-service.port}") int    triviaServicePort,

        @Value("${app.review-service.host}") String reviewServiceHost,
        @Value("${app.review-service.port}") int    reviewServicePort,
        
        @Value("${app.crazy-credit-service.host}") String crazyCreditServiceHost,
        @Value("${app.crazy-credit-service.port}") int    crazyCreditServicePort
    ) {

        this.restTemplate = restTemplate;
        this.mapper = mapper;

        movieServiceUrl        = "http://" + movieServiceHost + ":" + movieServicePort + "/movie";
        triviaServiceUrl = "http://" + triviaServiceHost + ":" + triviaServicePort + "/trivia";
        reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review";
        crazyCreditServiceUrl    = "http://" + crazyCreditServiceHost + ":" + crazyCreditServicePort + "/crazy-credit";
    }
    
    @Override
    public Movie createMovie(Movie body) {

        try {
            String url = movieServiceUrl;
            LOG.debug("Will post a new movie to URL: {}", url);

            Movie movie = restTemplate.postForObject(url, body, Movie.class);
            LOG.debug("Created a movie with id: {}", movie.getMovieId());

            return movie;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public Movie getMovie(int movieId) {

        try {
            String url = movieServiceUrl + "/" + movieId;
            LOG.debug("Will call the getMovie API on URL: {}", url);

            Movie movie = restTemplate.getForObject(url, Movie.class);
            LOG.debug("Found a movie with id: {}", movie.getMovieId());

            return movie;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    
    @Override
    public void deleteMovie(int movieId) {
        try {
            String url = movieServiceUrl + "/" + movieId;
            LOG.debug("Will call the deleteMovie API on URL: {}", url);
            restTemplate.delete(url);
        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    
    @Override
    public Trivia createTrivia(Trivia body) {
        try {
        	String url = triviaServiceUrl;
            LOG.debug("Will post a new trivia to URL: {}", url);

            Trivia trivia = restTemplate.postForObject(url, body, Trivia.class);
            LOG.debug("Created a trivia with id: {}", trivia.getTriviaId());

            return trivia;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    
    @Override
    public List<Trivia> getTrivia(int movieId) {

        try {
            String url = triviaServiceUrl + "?movieId=" + movieId;

            LOG.debug("Will call the getTrivia API on URL: {}", url);
            List<Trivia> trivia = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Trivia>>() {}).getBody();

            LOG.debug("Found {} trivia for movie with id: {}", trivia.size(), movieId);
            return trivia;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting trivia, return zero trivia: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public void deleteTrivia(int movieId) {
        try {
            String url = triviaServiceUrl + "?movieId=" + movieId;
            LOG.debug("Will call the deleteTrivia API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    
    @Override
    public Review createReview(Review body) {

        try {
            String url = reviewServiceUrl;
            LOG.debug("Will post a new review to URL: {}", url);

            Review review = restTemplate.postForObject(url, body, Review.class);
            LOG.debug("Created a review with id: {}", review.getMovieId());

            return review;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
    public List<Review> getReviews(int movieId) {

        try {
            String url = reviewServiceUrl + "?movieId=" + movieId;

            LOG.debug("Will call the getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();

            LOG.debug("Found {} reviews for movie with id: {}", reviews.size(), movieId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }
    
    @Override
    public void deleteReviews(int movieId) {
        try {
            String url = reviewServiceUrl + "?movieId=" + movieId;
            LOG.debug("Will call the deleteReviews API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    
    @Override
    public CrazyCredit createCrazyCredit(CrazyCredit body) {

        try {
            String url = crazyCreditServiceUrl;
            LOG.debug("Will post a new crazy credit to URL: {}", url);

            CrazyCredit crazyCredit = restTemplate.postForObject(url, body, CrazyCredit.class);
            LOG.debug("Created a crazy credit with id: {}", crazyCredit.getMovieId());

            return crazyCredit;

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }

    @Override
	public List<CrazyCredit> getCrazyCredits(int movieId) {
		
		try {
            String url = crazyCreditServiceUrl + "?movieId=" + movieId;

            LOG.debug("Will call the getCrazyCredits API on URL: {}", url);
            List<CrazyCredit> crazyCredits = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<CrazyCredit>>() {}).getBody();

            LOG.debug("Found {} crazy credits for movie with id: {}", crazyCredits.size(), movieId);
            return crazyCredits;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting crazy credits, return zero crazy credits: {}", ex.getMessage());
            return new ArrayList<>();
        }
	}
    
    @Override
    public void deleteCrazyCredits(int movieId) {
        try {
            String url = crazyCreditServiceUrl + "?movieId=" + movieId;
            LOG.debug("Will call the deleteCrazyCredits API on URL: {}", url);

            restTemplate.delete(url);

        } catch (HttpClientErrorException ex) {
            throw handleHttpClientException(ex);
        }
    }
    
    private RuntimeException handleHttpClientException(HttpClientErrorException ex) {
        switch (ex.getStatusCode()) {

        case NOT_FOUND:
            return new NotFoundException(getErrorMessage(ex));

        case UNPROCESSABLE_ENTITY :
            return new InvalidInputException(getErrorMessage(ex));

        default:
            LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
            LOG.warn("Error body: {}", ex.getResponseBodyAsString());
            return ex;
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

}
