package com.example.microservices.core.crazycredit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.api.core.movie.Movie;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditRepository;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0", "eureka.client.enabled=false", "spring.cloud.config.enabled=false", "server.error.include-message=always"})
public class CrazyCreditServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private CrazyCreditRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;


	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll().block();
	}

	@Test
	public void getCrazyCreditsByMovieId() {

		int movieId = 1;

		sendCreateCrazyCreditEvent(movieId, 1);
		sendCreateCrazyCreditEvent(movieId, 2);
		sendCreateCrazyCreditEvent(movieId, 3);

		assertEquals(3, (long)repository.findByMovieId(movieId).count().block());

		getAndVerifyCrazyCreditsByMovieId(movieId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].movieId").isEqualTo(movieId)
			.jsonPath("$[2].crazyCreditId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {
		int movieId = 1;
		int crazyCreditId = 1;

		sendCreateCrazyCreditEvent(movieId, crazyCreditId);

		assertEquals(1, (long)repository.count().block());

		try {
			sendCreateCrazyCreditEvent(movieId, crazyCreditId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Movie Id: 1, Crazy credit Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, (long)repository.count().block());
	}

	@Test
	public void deleteCrazyCredits() {
		int movieId = 1;
		int crazyCreditId = 1;

		sendCreateCrazyCreditEvent(movieId, crazyCreditId);
		assertEquals(1, (long)repository.findByMovieId(movieId).count().block());

		sendDeleteCrazyCreditEvent(movieId);
		assertEquals(0, (long)repository.findByMovieId(movieId).count().block());

		sendDeleteCrazyCreditEvent(movieId);
	}

	@Test
	public void getCrazyCreditsMissingParameter() {
		getAndVerifyCrazyCreditsByMovieId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/crazy-credit")
			.jsonPath("$.message").isEqualTo("Required int parameter 'movieId' is not present");
	}

	@Test
	public void getCrazyCreditsInvalidParameter() {
		getAndVerifyCrazyCreditsByMovieId("?movieId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/crazy-credit")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getCrazyCreditsNotFound() {
		getAndVerifyCrazyCreditsByMovieId("?movieId=113", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getCrazyCreditsInvalidParameterNegativeValue() {
		int movieIdInvalid = -1;

		getAndVerifyCrazyCreditsByMovieId("?movieId=" + movieIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/crazy-credit")
			.jsonPath("$.message").isEqualTo("Invalid movieId: " + movieIdInvalid);
	}

	private WebTestClient.BodyContentSpec getAndVerifyCrazyCreditsByMovieId(int movieId, HttpStatus expectedStatus) {
		return getAndVerifyCrazyCreditsByMovieId("?movieId=" + movieId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyCrazyCreditsByMovieId(String movieIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/crazy-credit" + movieIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private void sendCreateCrazyCreditEvent(int movieId, int crazyCreditId) {
		CrazyCredit crazyCredit = new CrazyCredit(movieId, crazyCreditId, "Content " + crazyCreditId, false, "SA");
		Event<Integer, Movie> event = new Event(CREATE, movieId, crazyCredit);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteCrazyCreditEvent(int movieId) {
		Event<Integer, Movie> event = new Event(DELETE, movieId, null);
		input.send(new GenericMessage<>(event));
	}
}
