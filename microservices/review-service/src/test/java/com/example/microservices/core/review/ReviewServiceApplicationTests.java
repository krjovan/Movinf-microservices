package com.example.microservices.core.review;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.review.Review;
import org.springframework.integration.channel.AbstractMessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.support.GenericMessage;
import com.example.api.core.movie.Movie;
import com.example.api.event.Event;
import com.example.util.exceptions.InvalidInputException;
import com.example.microservices.core.review.persistence.ReviewRepository;

import static org.junit.Assert.assertEquals;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;
import static org.junit.Assert.fail;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
		"logging.level.com.example=DEBUG",
		"eureka.client.enabled=false",
		"spring.cloud.config.enabled=false",
	    "spring.datasource.url=jdbc:h2:mem:review-db",
		"server.error.include-message=always"})
public class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository repository;
	
	@Autowired
	private Sink channels;

	private AbstractMessageChannel input = null;

	@Before
	public void setupDb() {
		input = (AbstractMessageChannel) channels.input();
		repository.deleteAll();
	}

	@Test
	public void getReviewsByMovieId() {
		int movieId = 1;
		assertEquals(0, repository.findByMovieId(movieId).size());
		sendCreateReviewEvent(movieId, 1);
		sendCreateReviewEvent(movieId, 2);
		sendCreateReviewEvent(movieId, 3);
		assertEquals(3, repository.findByMovieId(movieId).size());
		getAndVerifyReviewsByMovieId(movieId, OK)
		.jsonPath("$.length()").isEqualTo(3)
		.jsonPath("$[2].movieId").isEqualTo(movieId)
		.jsonPath("$[2].reviewId").isEqualTo(3);
	}
	
	@Test
	public void duplicateError() {
		int movieId = 1;
		int reviewId = 1;

		assertEquals(0, repository.count());
		sendCreateReviewEvent(movieId, reviewId);

		assertEquals(1, repository.count());
		try {
			sendCreateReviewEvent(movieId, reviewId);
			fail("Expected a MessagingException here!");
		} catch (MessagingException me) {
			if (me.getCause() instanceof InvalidInputException)	{
				InvalidInputException iie = (InvalidInputException)me.getCause();
				assertEquals("Duplicate key, Movie Id: 1, Review Id:1", iie.getMessage());
			} else {
				fail("Expected a InvalidInputException as the root cause!");
			}
		}

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteReviews() {
		int movieId = 1;
		int reviewId = 1;

		sendCreateReviewEvent(movieId, reviewId);
		assertEquals(1, repository.findByMovieId(movieId).size());

		sendDeleteReviewEvent(movieId);
		assertEquals(0, repository.findByMovieId(movieId).size());

		sendDeleteReviewEvent(movieId);
	}

	@Test
	public void getReviewsMissingParameter() {
		getAndVerifyReviewsByMovieId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Required int parameter 'movieId' is not present");
	}

	@Test
	public void getReviewsInvalidParameter() {
		getAndVerifyReviewsByMovieId("?movieId=no-integer", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getReviewsNotFound() {
		getAndVerifyReviewsByMovieId("?movieId=213", OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getReviewsInvalidParameterNegativeValue() {
		int movieIdInvalid = -1;
		getAndVerifyReviewsByMovieId("?movieId=" + movieIdInvalid, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Invalid movieId: " + movieIdInvalid);
	}
	
	private WebTestClient.BodyContentSpec getAndVerifyReviewsByMovieId(int movieId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByMovieId("?movieId=" + movieId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByMovieId(String movieIdQuery, HttpStatus expectedStatus) {
		return client.get()
			.uri("/review" + movieIdQuery)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private void sendCreateReviewEvent(int movieId, int reviewId) {
		Review review = new Review(movieId, reviewId, new Date(), "Title " + reviewId, "Content " + reviewId, 0, "SA");
		Event<Integer, Movie> event = new Event(CREATE, movieId, review);
		input.send(new GenericMessage<>(event));
	}

	private void sendDeleteReviewEvent(int movieId) {
		Event<Integer, Movie> event = new Event(DELETE, movieId, null);
		input.send(new GenericMessage<>(event));
	}

}
