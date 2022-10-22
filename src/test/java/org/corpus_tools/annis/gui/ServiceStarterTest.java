package org.corpus_tools.annis.gui;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.util.ResourceUtils;


@SpringBootTest
@ActiveProfiles({"test", "headless"})
@WebAppConfiguration
class ServiceStarterTest {

  @Autowired
  private BeanFactory beanFactory;

  private ServiceStarter service;

  @BeforeEach
  public void setup() {
    service = beanFactory.getBean(ServiceStarter.class);
  }


  @Test
  void testNonExistingServiceConfig() throws IOException {

    File serviceConfig = new File("./this-file-does-not-exist.toml");
    service.config.setWebserviceConfig(serviceConfig.getAbsolutePath());

    File resultFile = service.getServiceConfig();

    // The file should be a newly created one
    assertNotEquals(serviceConfig, resultFile);

    List<String> resultContent = Files.readAllLines(resultFile.toPath());
    Collections.sort(resultContent);

    // Check that the file content are the default values
    assertEquals(3, resultContent.size());
    assertTrue(resultContent.get(0).startsWith("database.graphannis = '"));
    assertTrue(resultContent.get(0).endsWith("/.annis/v4'"));

    assertTrue(resultContent.get(1).startsWith("database.sqlite = '"));
    assertTrue(resultContent.get(1).endsWith("/.annis/v4/service_data.sqlite3'"));

    assertEquals("logging.debug = true", resultContent.get(2));
  }

  @Test
  void testOverwriteGraphannisLocation() throws IOException {
    File serviceConfig =
        ResourceUtils.getFile(this.getClass().getResource("overwritten-graphannis-location.toml"));
    service.config.setWebserviceConfig(serviceConfig.getAbsolutePath());

    File resultFile = service.getServiceConfig();

    // The file should be a newly created one
    assertNotEquals(serviceConfig, resultFile);

    List<String> resultContent = Files.readAllLines(resultFile.toPath());
    Collections.sort(resultContent);

    // Check that the file content are the default values, except for the graphANNIS location
    assertEquals(3, resultContent.size());
    assertEquals("database.graphannis = '/tmp/this-is-a-test'", resultContent.get(0));

    assertTrue(resultContent.get(1).startsWith("database.sqlite = '"));
    assertTrue(resultContent.get(1).endsWith("/.annis/v4/service_data.sqlite3'"));

    assertEquals("logging.debug = true", resultContent.get(2));
  }

  @Test
  void testOverwriteSqliteLocation() throws IOException {
    File serviceConfig =
        ResourceUtils.getFile(this.getClass().getResource("overwritten-sqlite-location.toml"));
    service.config.setWebserviceConfig(serviceConfig.getAbsolutePath());

    File resultFile = service.getServiceConfig();

    // The file should be a newly created one
    assertNotEquals(serviceConfig, resultFile);

    List<String> resultContent = Files.readAllLines(resultFile.toPath());
    Collections.sort(resultContent);

    // Check that the file content are the default values, except for the sqlite location
    assertEquals(3, resultContent.size());

    assertTrue(resultContent.get(0).startsWith("database.graphannis = '"));
    assertTrue(resultContent.get(0).endsWith("/.annis/v4'"));

    assertEquals("database.sqlite = '/tmp/this-is-a-test'", resultContent.get(1));

    assertEquals("logging.debug = true", resultContent.get(2));
  }

  @Test
  void testEnableDebugLogging() throws IOException {
    File serviceConfig =
        ResourceUtils.getFile(this.getClass().getResource("debug-disabled.toml"));

    service.config.setWebserviceConfig(serviceConfig.getAbsolutePath());

    File resultFile = service.getServiceConfig();

    // The file should be a newly created one
    assertNotEquals(serviceConfig, resultFile);

    List<String> resultContent = Files.readAllLines(resultFile.toPath());
    Collections.sort(resultContent);

    // Check that the debug value has been overwritten
    assertEquals(3, resultContent.size());
    assertEquals("logging.debug = true", resultContent.get(2));

  }
}
