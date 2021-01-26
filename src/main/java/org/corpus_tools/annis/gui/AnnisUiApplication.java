package org.corpus_tools.annis.gui;

import java.awt.Desktop;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@PropertySources({@PropertySource(value = "classpath:app.properties"),
    @PropertySource(value = "file:${user.home}/.annis/annis-gui.properties",
                ignoreResourceNotFound = true),
    @PropertySource(value = "file:${ANNIS_CFG}/annis-gui.properties",
                ignoreResourceNotFound = true),
    @PropertySource(value = "file:/etc/annis/annis-gui.properties",
                ignoreResourceNotFound = true)})
@ComponentScan(basePackages = {"org.corpus_tools.annis.gui",
    "org.corpus_tools.annis.visualizers", "org.corpus_tools.annis.gui.exporter"})
@ServletComponentScan
@EnableJpaRepositories("org.corpus_tools.annis.gui.query_references")
@EnableTransactionManagement
@EntityScan("org.corpus_tools.annis.gui.query_references")
@EnableConfigurationProperties(UIConfig.class)
public class AnnisUiApplication {

    public static void main(String[] args) {
      SpringApplicationBuilder builder = new SpringApplicationBuilder(AnnisUiApplication.class);
      if (Desktop.isDesktopSupported()) {
        builder.headless(false);
      }
      builder.run(args);
    }

}
