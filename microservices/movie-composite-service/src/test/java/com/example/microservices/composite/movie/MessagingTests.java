package com.example.microservices.composite.movie;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import com.example.api.composite.movie.MovieAggregate;
import com.example.api.composite.movie.TriviaSummary;
import com.example.api.composite.movie.ReviewSummary;
import com.example.api.composite.movie.CrazyCreditSummary;
import com.example.api.core.movie.Movie;
import com.example.api.core.trivia.Trivia;
import com.example.api.core.review.Review;
import com.example.api.core.crazycredit.CrazyCredit;
import com.example.api.event.Event;
import com.example.microservices.composite.movie.services.MovieCompositeIntegration;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.cloud.stream.test.matcher.MessageQueueMatcher.receivesPayloadThat;
import static org.springframework.http.HttpStatus.OK;
import static reactor.core.publisher.Mono.just;
import static com.example.api.event.Event.Type.CREATE;
import static com.example.api.event.Event.Type.DELETE;
import static com.example.microservices.composite.movie.IsSameEvent.sameEventExceptCreatedAt;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
public class MessagingTests {

	private static final int MOVIE_ID_OK = 1;
	private static final int MOVIE_ID_NOT_FOUND = 2;
	private static final int MOVIE_ID_INVALID = 3;

    @Autowired
    private WebTestClient client;

	@Autowired
	private MovieCompositeIntegration.MessageSources channels;

	@Autowired
	private MessageCollector collector;

	BlockingQueue<Message<?>> queueMovies = null;
	BlockingQueue<Message<?>> queueTrivia = null;
	BlockingQueue<Message<?>> queueReviews = null;
	BlockingQueue<Message<?>> queueCrazyCredits = null;

	@Before
	public void setUp() {
		queueMovies = getQueue(channels.outputMovies());
		queueTrivia = getQueue(channels.outputTrivia());
		queueReviews = getQueue(channels.outputReviews());
		queueCrazyCredits = getQueue(channels.outputCrazyCredits());
	}

	@Test
	public void createCompositeMovie1() {

		MovieAggregate composite = new MovieAggregate(1, "Some title", new Date(), "Some country", 0, 0, 0, null, null, null, null);
		postAndVerifyMovie(composite, OK);

		// Assert one expected new movie events queued up
		assertEquals(1, queueMovies.size());

		Event<Integer, Movie> expectedEvent = new Event(CREATE, composite.getMovieId(), new Movie(composite.getMovieId(), composite.getTitle(), composite.getReleaseDate(), composite.getCountry(), composite.getBudget(), composite.getGross(), composite.getRuntime(), null));
		assertThat(queueMovies, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

		// Assert none trivia, review and crazycredits events
		assertEquals(0, queueTrivia.size());
		assertEquals(0, queueReviews.size());
		assertEquals(0, queueCrazyCredits.size());
	}

	@Test
	public void createCompositeMovie2() {

		MovieAggregate composite = new MovieAggregate(1, "Some title", new Date(), "Some country", 0, 0, 0,
			singletonList(new TriviaSummary(1, new Date(), "Some content", false)),
			singletonList(new ReviewSummary(1, new Date(), "Some title", "Some content", 0)), 
			singletonList(new CrazyCreditSummary(1, "Some content", false)), 
					null);

		postAndVerifyMovie(composite, OK);

		// Assert one create movie event queued up
		assertEquals(1, queueMovies.size());

		Event<Integer, Movie> expectedMovieEvent = new Event(CREATE, composite.getMovieId(), new Movie(composite.getMovieId(), composite.getTitle(), composite.getReleaseDate(), composite.getCountry(), composite.getBudget(), composite.getGross(), composite.getRuntime(), null));
		assertThat(queueMovies, receivesPayloadThat(sameEventExceptCreatedAt(expectedMovieEvent)));

		// Assert one create trivia event queued up
		assertEquals(1, queueTrivia.size());

		TriviaSummary tri = composite.getTrivia().get(0);
		Event<Integer, Movie> expectedTriviaEvent = new Event(CREATE, composite.getMovieId(), new Trivia(composite.getMovieId(), tri.getTriviaId(), tri.getPublishDate(), tri.getContent(), tri.isSpoiler(), null));
		assertThat(queueTrivia, receivesPayloadThat(sameEventExceptCreatedAt(expectedTriviaEvent)));

		// Assert one create review event queued up
		assertEquals(1, queueReviews.size());

		ReviewSummary rev = composite.getReviews().get(0);
		Event<Integer, Movie> expectedReviewEvent = new Event(CREATE, composite.getMovieId(), new Review(composite.getMovieId(), rev.getReviewId(), rev.getPublishDate(), rev.getTitle(), rev.getContent(), rev.getRating(), null));
		assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
		
		// Assert one create crazy credit event queued up
		assertEquals(1, queueCrazyCredits.size());

		CrazyCreditSummary cra = composite.getCrazyCredits().get(0);
		Event<Integer, Movie> expectedCrazyCreditEvent = new Event(CREATE, composite.getMovieId(), new CrazyCredit(composite.getMovieId(), cra.getCrazyCreditId(), cra.getContent(), cra.isSpoiler(), null));
		assertThat(queueCrazyCredits, receivesPayloadThat(sameEventExceptCreatedAt(expectedCrazyCreditEvent)));
	}

	@Test
	public void deleteCompositeMovie() {

		deleteAndVerifyMovie(1, OK);

		// Assert one delete movie event queued up
		assertEquals(1, queueMovies.size());

		Event<Integer, Movie> expectedEvent = new Event(DELETE, 1, null);
		assertThat(queueMovies, is(receivesPayloadThat(sameEventExceptCreatedAt(expectedEvent))));

		// Assert one delete trivia event queued up
		assertEquals(1, queueTrivia.size());

		Event<Integer, Movie> expectedTriviaEvent = new Event(DELETE, 1, null);
		assertThat(queueTrivia, receivesPayloadThat(sameEventExceptCreatedAt(expectedTriviaEvent)));

		// Assert one delete review event queued up
		assertEquals(1, queueReviews.size());

		Event<Integer, Movie> expectedReviewEvent = new Event(DELETE, 1, null);
		assertThat(queueReviews, receivesPayloadThat(sameEventExceptCreatedAt(expectedReviewEvent)));
		
		// Assert one delete crazy credit event queued up
		assertEquals(1, queueCrazyCredits.size());

		Event<Integer, Movie> expectedCrazyCreditsEvent = new Event(DELETE, 1, null);
		assertThat(queueCrazyCredits, receivesPayloadThat(sameEventExceptCreatedAt(expectedCrazyCreditsEvent)));
	}

	private BlockingQueue<Message<?>> getQueue(MessageChannel messageChannel) {
		return collector.forChannel(messageChannel);
	}

	private void postAndVerifyMovie(MovieAggregate compositeMovie, HttpStatus expectedStatus) {
		client.post()
			.uri("/movie-composite")
			.body(just(compositeMovie),MovieAggregate.class)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus);
	}

	private void deleteAndVerifyMovie(int movieId, HttpStatus expectedStatus) {
		client.delete()
			.uri("/movie-composite/" + movieId)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus);
	}
}
