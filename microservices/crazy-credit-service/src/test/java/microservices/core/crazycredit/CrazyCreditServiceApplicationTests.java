package microservices.core.crazycredit;

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
class CrazyCreditServiceApplicationTests {

	@Autowired
	private WebTestClient client;

	@Test
	public void getCrazyCreditsByMovieId() {

		int movieId = 1;

		client.get()
			.uri("/crazy-credit?movieId=" + movieId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[0].movieId").isEqualTo(movieId);
	}

	@Test
	public void getCrazyCreditsMissingParameter() {

		client.get()
			.uri("/crazy-credit")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/crazy-credit")
			.jsonPath("$.message").isEqualTo("Required int parameter 'movieId' is not present");
	}

	@Test
	public void getCrazyCreditsInvalidParameter() {

		client.get()
			.uri("/crazy-credit?movieId=no-integer")
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(BAD_REQUEST)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/crazy-credit")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	public void getCrazyCreditsNotFound() {

		int movieIdNotFound = 113;

		client.get()
			.uri("/crazy-credit?movieId=" + movieIdNotFound)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isOk()
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	public void getCrazyCreditsInvalidParameterNegativeValue() {

		int movieIdInvalid = -1;

		client.get()
			.uri("/crazy-credit?movieId=" + movieIdInvalid)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(UNPROCESSABLE_ENTITY)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody()
			.jsonPath("$.path").isEqualTo("/crazy-credit")
			.jsonPath("$.message").isEqualTo("Invalid movieId: " + movieIdInvalid);
	}
}
