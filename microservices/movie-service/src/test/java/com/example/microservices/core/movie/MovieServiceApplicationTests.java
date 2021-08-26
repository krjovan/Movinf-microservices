package com.example.microservices.core.movie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.api.core.movie.Movie;
import com.example.microservices.core.movie.persistence.MovieRepository;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class MovieServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private MovieRepository repository;

	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getMovieById() {
		int movieId = 1;
		postAndVerifyMovie(movieId, OK);
		assertTrue(repository.findByMovieId(movieId).isPresent());
		getAndVerifyMovie(movieId, OK)
        .jsonPath("$.movieId").isEqualTo(movieId);
	}
	
	@Test
	public void duplicateError() {
		int movieId = 1;
		postAndVerifyMovie(movieId, OK);
		assertTrue(repository.findByMovieId(movieId).isPresent());
		postAndVerifyMovie(movieId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/movie")
			.jsonPath("$.message").isEqualTo("Duplicate key, Movie Id: " + movieId);
	}

	@Test
	public void deleteMovie() {
		int movieId = 1;
		postAndVerifyMovie(movieId, OK);
		assertTrue(repository.findByMovieId(movieId).isPresent());
		deleteAndVerifyMovie(movieId, OK);
		assertFalse(repository.findByMovieId(movieId).isPresent());
		deleteAndVerifyMovie(movieId, OK);
	}

	@Test
	public void getMovieInvalidParameterString() {
		getAndVerifyMovie("/no-integer", BAD_REQUEST)
        .jsonPath("$.path").isEqualTo("/movie/no-integer")
        .jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getMovieNotFound() {
		int movieIdNotFound = 13;
		getAndVerifyMovie(movieIdNotFound, NOT_FOUND)
        .jsonPath("$.path").isEqualTo("/movie/" + movieIdNotFound)
        .jsonPath("$.message").isEqualTo("No movie found for movieId: " + movieIdNotFound);
	}

	@Test
	public void getMovieInvalidParameterNegativeValue() {
        int movieIdInvalid = -1;
        getAndVerifyMovie(movieIdInvalid, UNPROCESSABLE_ENTITY)
        .jsonPath("$.path").isEqualTo("/movie/" + movieIdInvalid)
        .jsonPath("$.message").isEqualTo("Invalid movieId: " + movieIdInvalid);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyMovie(int movieId, HttpStatus expectedStatus) {
		return getAndVerifyMovie("/" + movieId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyMovie(String movieIdPath, HttpStatus expectedStatus) {
		return client.get()
			.uri("/movie" + movieIdPath)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec postAndVerifyMovie(int movieId, HttpStatus expectedStatus) {
		Movie movie = new Movie(movieId, "n", new Date(),"s", 0, 0, 0, "SA");
		return client.post()
			.uri("/movie")
			.body(just(movie), Movie.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyMovie(int movieId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/movie/" + movieId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
