package annis.gui;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.corpus_tools.ApiException;
import org.corpus_tools.annis.AnnoKey;
import org.corpus_tools.annis.Annotation;
import org.corpus_tools.annis.Component;
import org.corpus_tools.annis.CorporaApi;
import org.corpus_tools.annis.CorpusList;
import org.corpus_tools.annis.FindQuery;
import org.corpus_tools.annis.SearchApi;
import org.corpus_tools.annis.FindQuery.OrderEnum;

public interface ServiceHelper {

    public static Set<AnnoKey> getMetaAnnotationNames(String corpus) throws ApiException {
        CorporaApi api = new CorporaApi();
        SearchApi search = new SearchApi();

        final List<Annotation> nodeAnnos = api.corpusNodeAnnotations(corpus, false, true).stream().filter(
                a -> !Objects.equals(a.getKey().getNs(), "annis") && !Objects.equals(a.getKey().getName(), "tok"))
                .collect(Collectors.toList());

        final Set<AnnoKey> metaAnnos = new HashSet<>();
        // Check for each annotation if its actually a meta-annotation
        for (Annotation a : nodeAnnos) {
            CorpusList c = new CorpusList();
            c.add(corpus);
            FindQuery q = new FindQuery();
            q.setCorpora(c);
            q.setQuery("annis:node_type=\"corpus\" _ident_ " + getQName(a.getKey()));
            // Not sorting the results is much faster, especially if we only fetch the first
            // item
            // (we are only interested if a match exists, not how many items or which one)
            q.setOrder(OrderEnum.NOTSORTED);
            q.setLimit(1);
            q.setOffset(0);

            q.setQueryLanguage(org.corpus_tools.annis.QueryLanguage.AQL);
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