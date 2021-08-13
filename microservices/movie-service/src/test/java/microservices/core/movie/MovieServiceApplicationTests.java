package microservices.core.movie;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment=RANDOM_PORT)
class MovieServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void getMovieById() {

		int movieId = 1;

        client.get()
            .uri("/movie/" + movieId)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.movieId").isEqualTo(movieId);
	}

	@Test
	public void getMovieInvalidParameterString() {

        client.get()
            .uri("/movie/no-integer")
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(BAD_REQUEST)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/movie/no-integer")
            .jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getMovieNotFound() {

		int movieIdNotFound = 13;

        client.get()
            .uri("/movie/" + movieIdNotFound)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isNotFound()
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/movie/" + movieIdNotFound)
            .jsonPath("$.message").isEqualTo("No movie found for movieId: " + movieIdNotFound);
	}

	@Test
	public void getMovieInvalidParameterNegativeValue() {

        int movieIdInvalid = -1;

        client.get()
            .uri("/movie/" + movieIdInvalid)
            .accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
            .expectHeader().contentType(APPLICATION_JSON)
            .expectBody()
            .jsonPath("$.path").isEqualTo("/movie/" + movieIdInvalid)
            .jsonPath("$.message").isEqualTo("Invalid movieId: " + movieIdInvalid);
	}
}
