package com.example.microservices.core.trivia;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.movie.Movie;
import com.example.api.core.trivia.Trivia;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.microservices.core.trivia.persistence.TriviaRepository;
import static org.junit.Assert.assertEquals;

import static org.junit.Assert.fail;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class TriviaServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private TriviaRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getTriviaByMovieId() {

		int movieId = 1;

		sendCreateTriviaEvent(movieId, 1);
		sendCreateTriviaEvent(movieId, 2);
		sendCreateTriviaEvent(movieId, 3);

		assertEquals(3, (long)repository.findByMovieId(movieId).count().block());

		getAndVerifyTriviaByMovieId(movieId, OK)
		.jsonPath("$.length()").isEqualTo(3)
		.jsonPath("$[2].movieId").isEqualTo(movieId)
		.jsonPath("$[2].triviaId").isEqualTo(3);
	}
	
	@Test
	public void duplicateError() {
		int movieId = 1;
		int triviaId = 1;

		sendCreateTriviaEvent(movieId, triviaId);

		assertEquals(1, (long)repository.count().block());

		try {
			sendCreateTriviaEvent(movieId, triviaId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Movie Id: 1, Trivia Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long)repository.count().block());
	}

	@Test
	public void deleteTrivia() {
		int movieId = 1;
		int triviaId = 1;

		sendCreateTriviaEvent(movieId, triviaId);
		assertEquals(1, (long)repository.findByMovieId(movieId).count().block());

		sendDeleteTriviaEvent(movieId);
		assertEquals(0, (long)repository.findByMovieId(movieId).count().block());

		sendDeleteTriviaEvent(movieId);
	}

	@Test
	public void getTriviaMissingParameter() {
		getAndVerifyTriviaByMovieId("", BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/trivia")
		.jsonPath("$.message").isEqualTo("Required int parameter 'movieId' is not present");
	}

	@Test
	public void getTriviaInvalidParameter() {
		getAndVerifyTriviaByMovieId("?movieId=no-integer", BAD_REQUEST)
		.jsonPath("$.path").isEqualTo("/trivia")
		.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getTriviaNotFound() {
		getAndVerifyTriviaByMovieId("?movieId=113", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getTriviaInvalidParameterNegativeValue() {
		int movieIdInvalid = -1;
		getAndVerifyTriviaByMovieId("?movieId=" + movieIdInvalid, UNPROCESSABLE_ENTITY)
		.jsonPath("$.path").isEqualTo("/trivia")
		.jsonPath("$.message").isEqualTo("Invalid movieId: " + movieIdInvalid);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyTriviaByMovieId(int movieId, HttpStatus expectedStatus) {
		return getAndVerifyTriviaByMovieId("?movieId=" + movieId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyTriviaByMovieId(String movieIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/trivia" + movieIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private void sendCreateTriviaEvent(int movieId, int triviaId) {
		Trivia trivia = new Trivia(movieId, triviaId, new Date(), "Some content " + triviaId, false, "SA");
		Event<Integer, Movie> event = new Event(CREATE, movieId, trivia);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteTriviaEvent(int movieId) {
		Event<Integer, Movie> event = new Event(DELETE, movieId, null);
		input.send(new GenericMessage<>(event));
	}

}
