package com.example.microservices.composite.movie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static java.util.Collections.singletonList;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import java.sql.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.microservices.composite.movie.services.MovieCompositeIntegration;

import api.core.crazycredit.CrazyCredit;
import api.core.movie.Movie;
import api.core.review.Review;
import api.core.trivia.Trivia;
import com.example.util.exceptions.InvalidInputException;
import com.example.util.exceptions.NotFoundException;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
class MovieCompositeServiceApplicationTests {

	private static final int MOVIE_ID_OK = 1;
	private static final int MOVIE_ID_NOT_FOUND = 2;
	private static final int MOVIE_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

	@MockBean
	private MovieCompositeIntegration compositeIntegration;

	@Before
	public void setUp() {

		when(compositeIntegration.getMovie(MOVIE_ID_OK)).
			thenReturn(new Movie(MOVIE_ID_OK, "Test Title", Date.valueOf("2021-08-13"), "Test country", 0, 0, 0, "mock-address"));
		when(compositeIntegration.getTrivia(MOVIE_ID_OK)).
			thenReturn(singletonList(new Trivia(MOVIE_ID_OK, 1, Date.valueOf("2021-08-13"), "Test content", false, "mock address")));
		when(compositeIntegration.getReviews(MOVIE_ID_OK)).
			thenReturn(singletonList(new Review(MOVIE_ID_OK, 1, Date.valueOf("2021-08-13"), "Test title", "Test content", 3, "mock address")));
		when(compositeIntegration.getCrazyCredits(MOVIE_ID_OK)).
			thenReturn(singletonList(new CrazyCredit(MOVIE_ID_OK, 1, "Test content", false, "mock address")));
		
		when(compositeIntegration.getMovie(MOVIE_ID_NOT_FOUND)).thenThrow(new NotFoundException("NOT FOUND: " + MOVIE_ID_NOT_FOUND));

		when(compositeIntegration.getMovie(MOVIE_ID_INVALID)).thenThrow(new InvalidInputException("INVALID: " + MOVIE_ID_INVALID));
	}

	@Test
	public void contextLoads() {
	}

	@Test
	public void getMovieById() {

        client.get()
            .uri("/movie-composite/" + MOVIE_ID_OK)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.movieId").isEqualTo(MOVIE_ID_OK)
            .jsonPath("$.trivia.length()").isEqualTo(1)
            .jsonPath("$.reviews.length()").isEqualTo(1)
        	.jsonPath("$.crazyCredits.length()").isEqualTo(1);
	}

	@Test
	public void getMovieNotFound() {

        client.get()
            .uri("/movie-composite/" + MOVIE_ID_NOT_FOUND)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/movie-composite/" + MOVIE_ID_NOT_FOUND)
            .jsonPath("$.message").isEqualTo("NOT FOUND: " + MOVIE_ID_NOT_FOUND);
	}

	@Test
	public void getMovieInvalidInput() {

        client.get()
            .uri("/movie-composite/" + MOVIE_ID_INVALID)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/movie-composite/" + MOVIE_ID_INVALID)
            .jsonPath("$.message").isEqualTo("INVALID: " + MOVIE_ID_INVALID);
	}

}
