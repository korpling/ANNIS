package annis.administration;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.WebTarget;

import org.corpus_tools.graphannis.errors.GraphANNISException;

import com.google.common.base.Joiner;

import annis.CommonHelper;
import annis.dao.QueryDao;
import annis.model.Query;
import annis.service.objects.MatchAndDocumentCount;
import annis.service.objects.QueryLanguage;

public class URLShortenerQuery {
    
    public static enum Status {
        Ok,
        UnknownCorpus,
        CountDiffers
    }
    
    
    private Query query;
    public URLShortenerQuery(String url) {
       
        if (url.startsWith("/embeddedvis")) {
            // TODO: parse embedded vis linked query
        } else if (url.startsWith("/#")) {
            Map<String, String> args = CommonHelper.parseFragment(url.substring("/#".length()));
            String corporaRaw = args.get("c");
            String aql = args.get("q");
            if (corporaRaw != null && aql != null) {
                Set<String> corpora = new LinkedHashSet<>(Arrays.asList(corporaRaw.split("\\s*,\\s*")));
                this.query = new Query();
                this.query.setCorpora(corpora);
                this.query.setQuery(aql);
                this.query.setQueryLanguage(QueryLanguage.AQL);
            }
        }
    }
    
    public Query getQuery() {
        return query;
    }
    
    public Status test(QueryDao queryDao, WebTarget annisSearchService) throws GraphANNISException {
        int countGraphANNIS = queryDao.count(query.getQuery(),
                QueryLanguage.AQL, new LinkedList<>(query.getCorpora()));
        MatchAndDocumentCount countLegacy = annisSearchService.path("count").queryParam("q", query.getQuery())
                .queryParam("corpora", Joiner.on(",").join(query.getCorpora())).request()
                .get(MatchAndDocumentCount.class);
        if (countGraphANNIS != countLegacy.getMatchCount()) {
            return Status.CountDiffers;
            // TODO: check the actual match IDs
            // TODO: check in quirks mode and rewrite if necessary
        } else {
            return Status.Ok;
        }
    }
}
