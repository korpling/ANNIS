package annis.administration;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.client.WebTarget;

import org.corpus_tools.graphannis.errors.GraphANNISException;

import com.google.common.base.Joiner;

import annis.CommonHelper;
import annis.dao.QueryDao;
import annis.model.Query;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.QueryLanguage;
import annis.sqlgen.extensions.LimitOffsetQueryData;

public class URLShortenerQuery {

    public static enum Status {
        Ok, UnknownCorpus, CountDiffers, MatchesDiffer
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
        List<Match> matchesGraphANNIS = queryDao.find(query.getQuery(), QueryLanguage.AQL,
                new LinkedList<>(query.getCorpora()), new LimitOffsetQueryData(0, Integer.MAX_VALUE));
        MatchGroup matchesLegacy = annisSearchService.path("find").queryParam("q", query.getQuery())
                .queryParam("limit", -1).queryParam("corpora", Joiner.on(",").join(query.getCorpora())).request()
                .get(MatchGroup.class);
        if (matchesGraphANNIS.size() != matchesLegacy.getMatches().size()) {
            return Status.CountDiffers;
        } else {
            return matchesGraphANNIS.equals(matchesLegacy.getMatches()) ? Status.Ok : Status.MatchesDiffer;
        }
        // TODO: check in quirks mode and rewrite if necessary
    }
}
