package annis.gui;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"annis.gui", "annis.visualizers"})
@ServletComponentScan
public class AnnisUiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AnnisUiApplication.class, args);
    }

}
