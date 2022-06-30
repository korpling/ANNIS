package org.corpus_tools.annis.gui.servlets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ActiveProfiles({"desktop", "test", "headless"})
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

  @Test
  void handleSingleIOException() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    CitationRedirectionServlet servlet = new CitationRedirectionServlet();
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);

    // Throw an exception when the servlet tries to perform the redirect
    when(request.getRequestURI()).thenReturn("http://localhost:" + port + "/Cite/" + ORIGINAL_URL);

    doThrow(new IOException("Connection aborted")).when(response).sendRedirect(anyString());
    servlet.doGet(request, response);

    verify(response).sendRedirect(anyString());
    verify(response).sendError(eq(400), anyString());
    verifyNoMoreInteractions(response);
  }


  @Test
  void handleDoubleIOException() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    CitationRedirectionServlet servlet = new CitationRedirectionServlet();
    HttpSession session = mock(HttpSession.class);

    when(request.getSession()).thenReturn(session);

    // Throw an exception when the servlet tries to perform the redirect
    when(request.getRequestURI()).thenReturn("http://localhost:" + port + "/Cite/" + ORIGINAL_URL);

    doThrow(new IOException("Connection aborted (1)")).when(response).sendRedirect(anyString());
    // Also throw an exception when the 400 error code is send
    doThrow(new IOException("Connection aborted (2)")).when(response).sendError(anyInt(),
        anyString());
    servlet.doGet(request, response);

    verify(response).sendRedirect(anyString());
    verify(response).sendError(eq(400), anyString());
    verifyNoMoreInteractions(response);
  }

}
