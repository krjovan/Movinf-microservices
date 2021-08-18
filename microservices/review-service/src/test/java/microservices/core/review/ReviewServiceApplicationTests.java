package microservices.core.review;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.core.review.Review;
import com.example.microservices.core.review.persistence.ReviewRepository;

import static org.junit.Assert.assertEquals;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

import java.sql.Date;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT, properties = {
"spring.datasource.url=jdbc:h2:mem:review-db"})
class ReviewServiceApplicationTests {

	@Autowired
	private WebTestClient client;
	
	@Autowired
	private ReviewRepository repository;

	@Before
	public void setupDb() {
		repository.deleteAll();
	}

	@Test
	public void getReviewsByMovieId() {
		int movieId = 1;
		assertEquals(0, repository.findByMovieId(movieId).size());
		postAndVerifyReview(movieId, 1, OK);
		postAndVerifyReview(movieId, 2, OK);
		postAndVerifyReview(movieId, 3, OK);
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
		postAndVerifyReview(movieId, reviewId, OK)
			.jsonPath("$.movieId").isEqualTo(movieId)
			.jsonPath("$.reviewId").isEqualTo(reviewId);

		assertEquals(1, repository.count());
		postAndVerifyReview(movieId, reviewId, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Duplicate key, Movie Id: 1, Review Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	public void deleteReviews() {
		int movieId = 1;
		int reviewId = 1;

		postAndVerifyReview(movieId, reviewId, OK);
		assertEquals(1, repository.findByMovieId(movieId).size());

		deleteAndVerifyReviewsByMovieId(movieId, OK);
		assertEquals(0, repository.findByMovieId(movieId).size());

		deleteAndVerifyReviewsByMovieId(movieId, OK);
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

	private WebTestClient.BodyContentSpec postAndVerifyReview(int movieId, int reviewId, HttpStatus expectedStatus) {
		Review review = new Review(movieId, reviewId, Date.valueOf("2021-08-12"), "Title " + reviewId, "Content " + reviewId, 0, "SA");
		return client.post()
			.uri("/review")
			.body(just(review), Review.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByMovieId(int movieId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/review?movieId=" + movieId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}

}
