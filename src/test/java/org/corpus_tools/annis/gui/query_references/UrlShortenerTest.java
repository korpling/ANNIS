package org.corpus_tools.annis.gui.query_references;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import com.github.mvysny.kaributesting.v8.MockVaadin;
import com.vaadin.spring.internal.UIScopeImpl;
import java.io.IOException;
import java.net.URI;
import net.jcip.annotations.NotThreadSafe;
import org.corpus_tools.annis.gui.AnnisUI;
import org.corpus_tools.annis.gui.SingletonBeanStoreRetrievalStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;

@SpringBootTest
@ActiveProfiles({"desktop", "test", "headless"})
@WebAppConfiguration
@NotThreadSafe
class UrlShortenerTest {

  @Autowired
  private BeanFactory beanFactory;

  private AnnisUI ui;

  @BeforeEach
  void setup() throws IOException {
    UIScopeImpl.setBeanStoreRetrievalStrategy(new SingletonBeanStoreRetrievalStrategy());
    this.ui = beanFactory.getBean(AnnisUI.class);

    MockVaadin.setup(() -> ui);


  }

  @AfterEach
  void cleanup() {
    ui.getUrlShortener().getRepo().deleteAll();
  }

  @Test
  void testLongURI() {
    String uuid = ui.getUrlShortener().shortenURL(URI.create(
        "/eeroihohlahno1quie7oophiuleip5aeQu6lichoon1aiwoobahmuKiesee5ahch3nah"
            + "ph2xahthiequaiquei5IhooHahpa7aiCha5iek4kob0ou8uihai8naNgeey4ohxekae"
            + "kah9oomied7uodaiwie5maiquieBahb4quei0aeliath7vinahbie8pheezai0yiero"
            + "ht2saeYie2thiekeub7yahl6roo9iexoiNogahta8iefiesam6ohghoo5Ajeeziequa"
            + "Sel6aiyaenangie8ohrao5Aeph9eib7oim8aidoo9tiepoo1CheiriuQuieTaizoo3r"
            + "aiciebahMahYie7le9aighaiLaegi6chie4eex9oo9aisha7EimohJoh3poo4phu"),
        ui);

    assertNotNull(uuid);
    assertNotEquals("", uuid);

  }

}
