package com.example.microservices.core.trivia;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.trivia.Trivia;
import com.example.microservices.core.trivia.persistence.TriviaRepository;
import static org.junit.Assert.assertEquals;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class TriviaServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private TriviaRepository repository;

	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getTriviaByMovieId() {

		int movieId = 1;

		postAndVerifyTrivia(movieId, 1, OK);
		postAndVerifyTrivia(movieId, 2, OK);
		postAndVerifyTrivia(movieId, 3, OK);

		assertEquals(3, repository.findByMovieId(movieId).size());

		getAndVerifyTriviaByMovieId(movieId, OK)
		.jsonPath("$.length()").isEqualTo(3)
		.jsonPath("$[2].movieId").isEqualTo(movieId)
		.jsonPath("$[2].triviaId").isEqualTo(3);
	}
	
	@Test
	public void duplicateError() {
		int movieId = 1;
		int triviaId = 1;

		postAndVerifyTrivia(movieId, triviaId, OK)
			.jsonPath("$.movieId").isEqualTo(movieId)
			.jsonPath("$.triviaId").isEqualTo(triviaId);

		assertEquals(1, repository.count());

		postAndVerifyTrivia(movieId, triviaId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/trivia")
			.jsonPath("$.message").isEqualTo("Duplicate key, Movie Id: 1, Trivia Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteTrivia() {
		int movieId = 1;
		int triviaId = 1;

		postAndVerifyTrivia(movieId, triviaId, OK);
		assertEquals(1, repository.findByMovieId(movieId).size());

		deleteAndVerifyTriviaByMovieId(movieId, OK);
		assertEquals(0, repository.findByMovieId(movieId).size());

		deleteAndVerifyTriviaByMovieId(movieId, OK);
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

	private WebTestClient.BodyContentSpec postAndVerifyTrivia(int movieId, int triviaId, HttpStatus expectedStatus) {
		Trivia trivia = new Trivia(movieId, triviaId, new Date(), "Some content " + triviaId, false, "SA");
		return client.post()
			.uri("/trivia")
			.body(just(trivia), Trivia.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyTriviaByMovieId(int movieId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/trivia?movieId=" + movieId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}

}
