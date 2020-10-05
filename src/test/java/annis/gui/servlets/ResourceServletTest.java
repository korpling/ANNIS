package annis.gui.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("desktop")
class ResourceServletTest {

  @LocalServerPort
  private int port;

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void emptyPath() {
    assertEquals(HttpStatus.NOT_FOUND, restTemplate
        .getForEntity("http://localhost:" + port + "/Resource/", String.class).getStatusCode());
    assertEquals(HttpStatus.NOT_FOUND, restTemplate
        .getForEntity("http://localhost:" + port + "/Resource", String.class).getStatusCode());
  }

  @Test
  void dependencyResource()
  {
    ResponseEntity<String> result =
        restTemplate.getForEntity(
            "http://localhost:" + port + "/Resource/arch_dependency/vakyartha/jquery.js",
            String.class);
    assertEquals(HttpStatus.OK, result.getStatusCode());
    assertNotNull(result.getBody());
    assertTrue(result.getBody()
        .startsWith("/*! jQuery v2.2.1 | (c) jQuery Foundation | jquery.org/license */"));
  }


  @Test
  void unknownVis() {
    ResponseEntity<String> result = restTemplate.getForEntity(
        "http://localhost:" + port + "/Resource/doesnotexist/something.txt", String.class);
    assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
  }

  @Test
  void unknownResource() {
    ResponseEntity<String> result = restTemplate.getForEntity(
        "http://localhost:" + port + "/Resource/arch_dependency/noththere.txt", String.class);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void invalidPathStructure() {
    ResponseEntity<String> result = restTemplate
        .getForEntity("http://localhost:" + port + "/Resource/arch_dependency", String.class);
    assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
  }

  @Test
  void invalidClassAccess() {
    ResponseEntity<String> result = restTemplate.getForEntity(
        "http://localhost:" + port + "/Resource/arch_dependency/VisualizerPanel.class",
        String.class);
    assertEquals(HttpStatus.FORBIDDEN, result.getStatusCode());
  }

}
