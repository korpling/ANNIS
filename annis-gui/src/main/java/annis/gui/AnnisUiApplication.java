package annis.gui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@PropertySource(value = "file:${user.home}/.annis/annis-gui.properties",
    ignoreResourceNotFound = true)
@PropertySource(value = "file:${ANNIS_CFG}/annis-gui.properties", ignoreResourceNotFound = true)
@PropertySource(value = "file:/etc/annis/annis-gui.properties", ignoreResourceNotFound = true)
@ComponentScan(basePackages = {"annis.gui", "annis.visualizers", "annis.gui.exporter"})
@ServletComponentScan
@EnableJpaRepositories("annis.gui.query_references")
@EnableTransactionManagement
@EntityScan("annis.gui.query_references")
@EnableConfigurationProperties(UIConfig.class)
public class AnnisUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnnisUiApplication.class, args);
    }

}
