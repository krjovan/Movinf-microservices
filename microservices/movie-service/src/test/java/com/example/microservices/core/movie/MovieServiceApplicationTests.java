package com.example.microservices.core.movie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.example.api.core.movie.Movie;
import com.example.api.event.Event;
import com.example.microservices.core.movie.persistence.MovieRepository;
import com.example.util.exceptions.InvalidInputException;

import static org.junit.Assert.*;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false"})
public class MovieServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private MovieRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getMovieById() {
		int movieId = 1;
		assertNull(repository.findByMovieId(movieId).block());
		assertEquals(0, (long)repository.count().block());
		sendCreateMovieEvent(movieId);

		assertNotNull(repository.findByMovieId(movieId).block());
		assertEquals(1, (long)repository.count().block());
		getAndVerifyMovie(movieId, OK)
        .jsonPath("$.movieId").isEqualTo(movieId);
	}
	
	@Test
	public void duplicateError() {
		int movieId = 1;
		assertNull(repository.findByMovieId(movieId).block());

		sendCreateMovieEvent(movieId);
		assertNotNull(repository.findByMovieId(movieId).block());
		try {
			sendCreateMovieEvent(movieId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Movie Id: " + movieId, iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}
	}

	@Test
	public void deleteMovie() {
		int movieId = 1;
		sendCreateMovieEvent(movieId);
		assertNotNull(repository.findByMovieId(movieId).block());
		sendDeleteMovieEvent(movieId);
		assertNull(repository.findByMovieId(movieId).block());
		sendDeleteMovieEvent(movieId);
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

	private void sendCreateMovieEvent(int movieId) {
		Movie movie = new Movie(movieId, "n", new Date(),"s", 0, 0, 0, "SA");
		Event<Integer, Movie> event = new Event(CREATE, movieId, movie);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteMovieEvent(int movieId) {
		Event<Integer, Movie> event = new Event(DELETE, movieId, null);
		input.send(new GenericMessage<>(event));
	}
}
