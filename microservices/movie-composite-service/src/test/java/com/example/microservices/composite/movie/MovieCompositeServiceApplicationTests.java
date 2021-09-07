package com.example.microservices.composite.movie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import org.springframework.http.HttpStatus;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.util.Date;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.api.core.crazycredit.CrazyCredit;
import com.example.api.core.movie.Movie;
import com.example.api.core.review.Review;
import com.example.api.core.trivia.Trivia;
import com.example.microservices.composite.movie.services.MovieCompositeIntegration;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest(
		webEnvironment=RANDOM_PORT,
		classes = {MovieCompositeServiceApplication.class, TestSecurityConfig.class },
		properties = {"spring.main.allow-bean-definition-overriding=true","eureka.client.enabled=false","spring.cloud.config.enabled=false"})
public class MovieCompositeServiceApplicationTests {

	private static final int MOVIE_ID_OK = 1;
	private static final int MOVIE_ID_NOT_FOUND = 2;
	private static final int MOVIE_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

	@MockBean
	private MovieCompositeIntegration compositeIntegration;

	@Before
	public void setUp() {

		when(compositeIntegration.getMovie(eq(MOVIE_ID_OK), anyInt(), anyInt())).
			thenReturn(Mono.just(new Movie(MOVIE_ID_OK, "Test Title", new Date(), "Test country", 0, 0, 0, "mock-address")));
		when(compositeIntegration.getTrivia(MOVIE_ID_OK)).
			thenReturn(Flux.fromIterable(singletonList(new Trivia(MOVIE_ID_OK, 1, new Date(), "Test content", false, "mock address"))));
		when(compositeIntegration.getReviews(MOVIE_ID_OK)).
			thenReturn(Flux.fromIterable(singletonList(new Review(MOVIE_ID_OK, 1, new Date(), "Test title", "Test content", 3, "mock address"))));
		when(compositeIntegration.getCrazyCredits(MOVIE_ID_OK)).
			thenReturn(Flux.fromIterable(singletonList(new CrazyCredit(MOVIE_ID_OK, 1, "Test content", false, "mock address"))));
		
		when(compositeIntegration.getMovie(eq(MOVIE_ID_NOT_FOUND), anyInt(), anyInt())).thenThrow(new NotFoundException("NOT FOUND: " + MOVIE_ID_NOT_FOUND));

		when(compositeIntegration.getMovie(eq(MOVIE_ID_INVALID), anyInt(), anyInt())).thenThrow(new InvalidInputException("INVALID: " + MOVIE_ID_INVALID));
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void getMovieById() {
		getAndVerifyMovie(MOVIE_ID_OK, OK)
        .jsonPath("$.movieId").isEqualTo(MOVIE_ID_OK)
        .jsonPath("$.trivia.length()").isEqualTo(1)
        .jsonPath("$.reviews.length()").isEqualTo(1)
    	.jsonPath("$.crazyCredits.length()").isEqualTo(1);
	}

	@Test
	public void getMovieNotFound() {
		getAndVerifyMovie(MOVIE_ID_NOT_FOUND, NOT_FOUND)
        .jsonPath("$.path").isEqualTo("/movie-composite/" + MOVIE_ID_NOT_FOUND)
        .jsonPath("$.message").isEqualTo("NOT FOUND: " + MOVIE_ID_NOT_FOUND);
	}

	@Test
	public void getMovieInvalidInput() {
		getAndVerifyMovie(MOVIE_ID_INVALID, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/movie-composite/" + MOVIE_ID_INVALID)
        .jsonPath("$.message").isEqualTo("INVALID: " + MOVIE_ID_INVALID);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyMovie(int movieId, HttpStatus expectedStatus) {
		return client.get()
			.uri("/movie-composite/" + movieId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}
}
