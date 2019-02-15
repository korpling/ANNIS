package annis.administration;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;

import annis.CommonHelper;
import annis.dao.QueryDao;
import annis.model.Query;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.QueryLanguage;
import annis.sqlgen.extensions.LimitOffsetQueryData;

public class URLShortenerQuery {

    private final static Logger log = LoggerFactory.getLogger(URLShortenerQuery.class);

    private Query query;

    private String errorMsg;

    public URLShortenerQuery(String url) {
        this.query = new Query();
        this.query.setCorpora(new LinkedHashSet<>());
        this.query.setQuery("");
        this.query.setQueryLanguage(QueryLanguage.AQL);
        this.errorMsg = null;

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

    public String getErrorMsg() {
        return errorMsg;
    }

    public QueryStatus test(QueryDao queryDao, WebTarget annisSearchService) throws GraphANNISException {

        if (this.query.getCorpora().isEmpty()) {
            this.errorMsg = "Empty corpus list";
            return QueryStatus.Failed;
        }

        List<Match> matchesGraphANNIS = queryDao.find(query.getQuery(), QueryLanguage.AQL,
                new LinkedList<>(query.getCorpora()), new LimitOffsetQueryData(0, Integer.MAX_VALUE));

        WebTarget findTarget = annisSearchService.path("find").queryParam("q", query.getQuery()).queryParam("corpora",
                Joiner.on(",").join(query.getCorpora()));

        try {
            MatchGroup matchesLegacy = findTarget.request(MediaType.APPLICATION_XML_TYPE).get(MatchGroup.class);

            if (matchesGraphANNIS.size() != matchesLegacy.getMatches().size()) {
                this.errorMsg = "should have been " + matchesLegacy.getMatches().size() + " but was "
                        + matchesGraphANNIS.size();
                return QueryStatus.CountDiffers;
            } else {
                Iterator<Match> itGraphANNIS = matchesGraphANNIS.iterator();
                Iterator<Match> itLegacy = matchesLegacy.getMatches().iterator();
                while (itGraphANNIS.hasNext() && itLegacy.hasNext()) {
                    String m1 = itGraphANNIS.next().toString();
                    String m2 = itLegacy.next().toString();

                    if (!m1.equals(m2)) {
                        this.errorMsg = m1 + " != " + m2;
                        return QueryStatus.MatchesDiffer;
                    }
                }
                return QueryStatus.Ok;
            }
            // TODO: check in quirks mode and rewrite if necessary

        } catch (ForbiddenException ex) {
            this.errorMsg = ex.toString();
            return QueryStatus.Failed;
        }
    }
}
