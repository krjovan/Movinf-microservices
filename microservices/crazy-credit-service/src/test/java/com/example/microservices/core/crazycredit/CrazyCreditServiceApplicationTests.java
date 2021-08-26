package com.example.microservices.core.crazycredit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.microservices.core.crazycredit.persistence.CrazyCreditRepository;

import static org.junit.Assert.assertEquals;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {"spring.data.mongodb.port: 0"})
public class CrazyCreditServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private CrazyCreditRepository repository;


	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getCrazyCreditsByMovieId() {

		int movieId = 1;

		postAndVerifyCrazyCredit(movieId, 1, OK);
		postAndVerifyCrazyCredit(movieId, 2, OK);
		postAndVerifyCrazyCredit(movieId, 3, OK);

		assertEquals(3, repository.findByMovieId(movieId).size());

		getAndVerifyCrazyCreditsByMovieId(movieId, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].movieId").isEqualTo(movieId)
			.jsonPath("$[2].crazyCreditId").isEqualTo(3);
	}

	@Test
	public void duplicateError() {
		int movieId = 1;
		int crazyCreditId = 1;

		postAndVerifyCrazyCredit(movieId, crazyCreditId, OK)
			.jsonPath("$.movieId").isEqualTo(movieId)
			.jsonPath("$.crazyCreditId").isEqualTo(crazyCreditId);

		assertEquals(1, repository.count());

		postAndVerifyCrazyCredit(movieId, crazyCreditId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/crazy-credit")
			.jsonPath("$.message").isEqualTo("Duplicate key, Movie Id: 1, CrazyCredit Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteCrazyCredits() {
		int movieId = 1;
		int crazyCreditId = 1;

		postAndVerifyCrazyCredit(movieId, crazyCreditId, OK);
		assertEquals(1, repository.findByMovieId(movieId).size());

		deleteAndVerifyCrazyCreditsByMovieId(movieId, OK);
		assertEquals(0, repository.findByMovieId(movieId).size());

		deleteAndVerifyCrazyCreditsByMovieId(movieId, OK);
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

	private WebTestClient.BodyContentSpec postAndVerifyCrazyCredit(int movieId, int crazyCreditId, HttpStatus expectedStatus) {
		CrazyCredit crazyCredit = new CrazyCredit(movieId, crazyCreditId, "Content " + crazyCreditId, false, "SA");
		return client.post()
			.uri("/crazy-credit")
			.body(just(crazyCredit), CrazyCredit.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyCrazyCreditsByMovieId(int movieId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/crazy-credit?movieId=" + movieId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
