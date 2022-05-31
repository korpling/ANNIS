package org.corpus_tools.annis.gui;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.tomlj.Toml;
import org.tomlj.TomlParseResult;

@SpringBootTest
@ActiveProfiles({"desktop", "test", "headless"})
@WebAppConfiguration
class ServiceStarterDesktopTest {


  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;
  private ServiceStarter starter;

  private ch.qos.logback.classic.Logger logger;
  private List<ILoggingEvent> logEvents = new ArrayList<>();


  @BeforeEach
  public void setup() {
    logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(ServiceStarter.class);
    AppenderBase<ILoggingEvent> logAppender = new AppenderBase<ILoggingEvent>() {
      protected void append(ILoggingEvent eventObject) {
        logEvents.add(eventObject);
      }
    };
    logger.addAppender(logAppender);
    logAppender.start();

    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    ui = beanFactory.getBean(AnnisUI.class);
    starter = beanFactory.getBean(ServiceStarter.class);
    assertNotNull(starter);
    MockVaadin.setup(() -> ui);
  }

  @AfterEach
  public void tearDown() {
    MockVaadin.tearDown();
  }

  @Test
  void shutdownGracefully() throws Exception {
    starter.destroy();
    // Service should be stopped gracefully
    assertFalse(logEvents.isEmpty());
    String lastMessage = logEvents.get(logEvents.size() - 1).getMessage();
    assertEquals("Stopped graphANNIS process", lastMessage);
  }

  @Test
  void testDesktopAuthConfigured() {
    assertNotNull(ui.getLastAccessToken());
    SecurityContext ctx = ui.getSecurityContext();
    assertNotNull(ctx);
    Authentication auth = ctx.getAuthentication();
    assertNotNull(auth);

    assertTrue(auth.isAuthenticated());
    assertEquals("desktop", auth.getName());
    assertNull(auth.getDetails());
    assertEquals(ui.getLastAccessToken(), auth.getCredentials());
    assertTrue(auth.getPrincipal() instanceof OAuth2User);
  }

  @Test
  void testUnpackToml() {
    TomlParseResult toml =
        Toml.parse("[main]\n" + "test =\"value\"\n" + "b = [42, 23]\n" + "nested_array = [[1,2]]");
    Map<String, Object> unpacked = ServiceStarterDesktop.unpackToml(toml);

    assertEquals(1, unpacked.size());
    assertTrue(unpacked.get("main") instanceof Map);
    @SuppressWarnings("unchecked")
    Map<String, Object> main = (Map<String, Object>) unpacked.get("main");
    assertEquals(3, main.size());

    assertEquals("value", main.get("test"));
    assertTrue(main.get("b") instanceof List);
    @SuppressWarnings("unchecked")
    List<Object> b = (List<Object>) main.get("b");
    assertEquals(2, b.size());
    assertEquals(42l, b.get(0));
    assertEquals(23l, b.get(1));

    assertTrue(main.get("nested_array") instanceof List);
    @SuppressWarnings("unchecked")
    List<Object> nestedArray = (List<Object>) main.get("nested_array");
    assertEquals(1, nestedArray.size());
    assertTrue(nestedArray.get(0) instanceof List);
    assertEquals(Arrays.asList(1l, 2l), nestedArray.get(0));
  }


}
