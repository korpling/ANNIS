package annis.gui.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("desktop")
class CitationRedirectionServletTest {

  @LocalServerPort
  private int port;

  private TestRestTemplate restTemplate;

  private final static String ORIGINAL_URL = "random-url";

  @BeforeEach
  void setup() {
    this.restTemplate = new TestRestTemplate();
  }

  @Test
  void testRedirect() {
    ResponseEntity<String> response = restTemplate
        .getForEntity("http://localhost:" + port + "/Cite/" + ORIGINAL_URL, String.class);
    assertEquals(HttpStatus.FOUND, response.getStatusCode());
    assertEquals("http://localhost:" + port + "/", response.getHeaders().get("Location").get(0));
  }

}
