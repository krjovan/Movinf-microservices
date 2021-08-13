package microservices.composite.movie.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import api.core.crazycredit.*;
import api.core.movie.*;
import api.core.review.*;
import api.core.trivia.*;
import util.exceptions.InvalidInputException;
import util.exceptions.NotFoundException;
import util.http.HttpErrorInfo;

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

        movieServiceUrl        = "http://" + movieServiceHost + ":" + movieServicePort + "/movie/";
        triviaServiceUrl = "http://" + triviaServiceHost + ":" + triviaServicePort + "/trivia?movieId=";
        reviewServiceUrl         = "http://" + reviewServiceHost + ":" + reviewServicePort + "/review?movieId=";
        crazyCreditServiceUrl    = "http://" + crazyCreditServiceHost + ":" + crazyCreditServicePort + "/crazy-credit?movieId=";
    }

    public Movie getMovie(int movieId) {

        try {
            String url = movieServiceUrl + movieId;
            LOG.debug("Will call getMovie API on URL: {}", url);

            Movie movie = restTemplate.getForObject(url, Movie.class);
            LOG.debug("Found a movie with id: {}", movie.getMovieId());

            return movie;

        } catch (HttpClientErrorException ex) {

            switch (ex.getStatusCode()) {

            case NOT_FOUND:
                throw new NotFoundException(getErrorMessage(ex));

            case UNPROCESSABLE_ENTITY :
                throw new InvalidInputException(getErrorMessage(ex));

            default:
                LOG.warn("Got a unexpected HTTP error: {}, will rethrow it", ex.getStatusCode());
                LOG.warn("Error body: {}", ex.getResponseBodyAsString());
                throw ex;
            }
        }
    }

    private String getErrorMessage(HttpClientErrorException ex) {
        try {
            return mapper.readValue(ex.getResponseBodyAsString(), HttpErrorInfo.class).getMessage();
        } catch (IOException ioex) {
            return ex.getMessage();
        }
    }

    public List<Trivia> getTrivia(int movieId) {

        try {
            String url = triviaServiceUrl + movieId;

            LOG.debug("Will call getTrivia API on URL: {}", url);
            List<Trivia> trivia = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Trivia>>() {}).getBody();

            LOG.debug("Found {} trivia for movie with id: {}", trivia.size(), movieId);
            return trivia;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting trivia, return zero trivia: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

    public List<Review> getReviews(int movieId) {

        try {
            String url = reviewServiceUrl + movieId;

            LOG.debug("Will call getReviews API on URL: {}", url);
            List<Review> reviews = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<Review>>() {}).getBody();

            LOG.debug("Found {} reviews for movie with id: {}", reviews.size(), movieId);
            return reviews;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting reviews, return zero reviews: {}", ex.getMessage());
            return new ArrayList<>();
        }
    }

	public List<CrazyCredit> getCrazyCredits(int movieId) {
		
		try {
            String url = crazyCreditServiceUrl + movieId;

            LOG.debug("Will call getCrazyCredits API on URL: {}", url);
            List<CrazyCredit> crazyCredits = restTemplate.exchange(url, GET, null, new ParameterizedTypeReference<List<CrazyCredit>>() {}).getBody();

            LOG.debug("Found {} crazy credits for movie with id: {}", crazyCredits.size(), movieId);
            return crazyCredits;

        } catch (Exception ex) {
            LOG.warn("Got an exception while requesting crazy credits, return zero crazy credits: {}", ex.getMessage());
            return new ArrayList<>();
        }
	}

}
