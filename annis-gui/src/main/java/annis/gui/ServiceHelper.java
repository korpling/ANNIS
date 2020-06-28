package annis.gui;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.vaadin.ui.UI;

import org.corpus_tools.annis.api.model.AnnoKey;
import org.corpus_tools.annis.api.model.Annotation;
import org.corpus_tools.annis.api.model.Component;
import org.corpus_tools.annis.api.model.FindQuery;
import org.corpus_tools.annis.api.model.QueryLanguage;
import org.corpus_tools.annis.api.model.FindQuery.OrderEnum;
import org.corpus_tools.annis.auth.Authentication;
import org.corpus_tools.annis.auth.HttpBearerAuth;

import annis.libgui.AnnisUser;
import annis.libgui.Helper;

import org.corpus_tools.annis.ApiClient;
import org.corpus_tools.annis.ApiException;
import org.corpus_tools.annis.Configuration;
import org.corpus_tools.annis.api.CorporaApi;
import org.corpus_tools.annis.api.SearchApi;

public interface ServiceHelper {

    public static ApiClient getClient(UI ui) {
        ApiClient client = Configuration.getDefaultApiClient();
        AnnisUser user = Helper.getUser(ui);
        if (user != null && user.getToken() != null) {
            Authentication auth = client.getAuthentication("bearerAuth");
            if (auth instanceof HttpBearerAuth) {
                HttpBearerAuth bearerAuth = (HttpBearerAuth) auth;
                bearerAuth.setBearerToken(user.getToken());

                // TODO: get a new token if expired
            }
        }
        return client;
    }

    public static Set<AnnoKey> getMetaAnnotationNames(String corpus, UI ui) throws ApiException {
        CorporaApi api = new CorporaApi(getClient(ui));
        SearchApi search = new SearchApi(getClient(ui));

        final List<Annotation> nodeAnnos = api.corpusNodeAnnotations(corpus, false, true).stream().filter(
                a -> !Objects.equals(a.getKey().getNs(), "annis") && !Objects.equals(a.getKey().getName(), "tok"))
                .collect(Collectors.toList());

        final Set<AnnoKey> metaAnnos = new HashSet<>();
        // Check for each annotation if its actually a meta-annotation
        for (Annotation a : nodeAnnos) {
            FindQuery q = new FindQuery();
            q.setCorpora(Arrays.asList(corpus));
            q.setQuery("annis:node_type=\"corpus\" _ident_ " + getQName(a.getKey()));
            // Not sorting the results is much faster, especially if we only fetch the first
            // item
            // (we are only interested if a match exists, not how many items or which one)
            q.setOrder(OrderEnum.NOTSORTED);
            q.setLimit(1);
            q.setOffset(0);

            q.setQueryLanguage(QueryLanguage.AQL);
            String findResult = search.find(q);
            if (findResult != null && !findResult.isEmpty()) {
                metaAnnos.add(a.getKey());
            }

        }

        return metaAnnos;
    }

    public static String getQName(AnnoKey key) {
        if (key.getNs() == null || key.getNs().isEmpty()) {
            return key.getName();
        } else {
            return key.getNs() + ":" + key.getName();
        }
    }

    public static String getQName(Component c) {
        if (c.getLayer() == null || c.getLayer().isEmpty()) {
            return c.getName();
        } else {
            return c.getLayer() + ":" + c.getName();
        }
    }

}