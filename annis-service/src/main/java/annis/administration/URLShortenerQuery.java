package annis.administration;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.ServerErrorException;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.corpus_tools.graphannis.errors.GraphANNISException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;

import annis.CommonHelper;
import annis.dao.QueryDao;
import annis.model.DisplayedResultQuery;
import annis.model.Query;
import annis.service.objects.Match;
import annis.service.objects.MatchGroup;
import annis.service.objects.QueryLanguage;
import annis.sqlgen.extensions.LimitOffsetQueryData;

public class URLShortenerQuery {

    private final static Logger log = LoggerFactory.getLogger(URLShortenerQuery.class);

    private Query query;

    private String errorMsg;

    protected URLShortenerQuery() {
        this.query = new DisplayedResultQuery();
        this.errorMsg = null;
    }

    public static URLShortenerQuery parse(String url) throws URISyntaxException, UnsupportedEncodingException {

        URI parsedURI = new URI(url);

        if (parsedURI.getPath().startsWith("/embeddedvis")) {
            // parse embedded vis linked query
            Map<String, String> args = new LinkedHashMap<>();
            for (String argRaw : Splitter.on('&').trimResults().split(parsedURI.getRawQuery())) {
                List<String> keyValue = Splitter.on('=').limit(2).splitToList(argRaw);
                if (keyValue.size() == 1) {
                    args.putIfAbsent(keyValue.get(0), "");
                } else {
                    args.putIfAbsent(keyValue.get(0), keyValue.get(1));
                }
            }
            String interfaceURL = URLDecoder.decode(args.get("embedded_interface"), "UTF-8");

            if (interfaceURL != null) {
                return parse(interfaceURL);
            }

        } else {
            Map<String, String> args = CommonHelper.parseFragment(parsedURI.getFragment());
            String corporaRaw = args.get("c");
            String aql = args.get("q");
            if (corporaRaw != null && aql != null) {
                URLShortenerQuery result = new URLShortenerQuery();
                Set<String> corpora = new LinkedHashSet<>(Arrays.asList(corporaRaw.split("\\s*,\\s*")));
                result.getQuery().setCorpora(corpora);
                result.getQuery().setQuery(aql);
                result.getQuery().setQueryLanguage(QueryLanguage.AQL);
                return result;
            }
        }

        return null;
    }

    public Query getQuery() {
        return query;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public static int MAX_RETRY = 3;

    public QueryStatus test(QueryDao queryDao, WebTarget annisSearchService) throws GraphANNISException {

        if (this.query.getCorpora().isEmpty()) {
            this.errorMsg = "Empty corpus list";
            return QueryStatus.Failed;
        }

        List<Match> matchesGraphANNIS = queryDao.find(query.getQuery(), query.getQueryLanguage(),
                new LinkedList<>(query.getCorpora()), new LimitOffsetQueryData(0, Integer.MAX_VALUE));

        WebTarget findTarget = annisSearchService.path("find").queryParam("q", query.getQuery()).queryParam("corpora",
                Joiner.on(",").join(query.getCorpora()));

        try {

            int tries = 0;
            MatchGroup matchesLegacy = null;
            while (matchesLegacy == null) {
                try {
                    tries++;
                    matchesLegacy = findTarget.request(MediaType.APPLICATION_XML_TYPE).get(MatchGroup.class);
                } catch (ServerErrorException ex) {
                    if (tries >= MAX_RETRY) {
                        throw ex;
                    } else {
                        log.warn("Server error when executing query {}", query.getQuery(), ex);
                    }
                }
            }

            QueryStatus status = QueryStatus.Ok;
            if (matchesGraphANNIS.size() != matchesLegacy.getMatches().size()) {
                this.errorMsg = "should have been " + matchesLegacy.getMatches().size() + " but was "
                        + matchesGraphANNIS.size();
                status = QueryStatus.CountDiffers;
            } else {
                Iterator<Match> itGraphANNIS = matchesGraphANNIS.iterator();
                Iterator<Match> itLegacy = matchesLegacy.getMatches().iterator();
                while (itGraphANNIS.hasNext() && itLegacy.hasNext()) {
                    String m1 = itGraphANNIS.next().toString();
                    String m2 = itLegacy.next().toString();

                    if (!m1.equals(m2)) {
                        this.errorMsg = m1 + " != " + m2;
                        status = QueryStatus.MatchesDiffer;
                        break;
                    }
                }
            }

            if (status != QueryStatus.Ok && this.query.getQueryLanguage() == QueryLanguage.AQL) {
                // check in quirks mode and rewrite if necessary
                URLShortenerQuery quirksQuery = new URLShortenerQuery();
                quirksQuery.query = new Query(this.query.getQuery(), QueryLanguage.AQL_QUIRKS_V3,
                        this.query.getCorpora());
                log.info("Trying quirks mode for query {} on corpus {}", this.query.getQuery(), this.query.getCorpora());
                        
                QueryStatus quirksStatus = quirksQuery.test(queryDao, annisSearchService);
                if(quirksStatus == QueryStatus.Ok) {
                    this.query = quirksQuery.query;
                    this.errorMsg = "Rewrite in quirks mode necessary";
                    status = QueryStatus.Ok;
                } else {
                    this.errorMsg = quirksQuery.getErrorMsg();
                    log.warn("Quirks not sucessful");
                }
            }

            return status;

        } catch (ForbiddenException ex) {
            this.errorMsg = ex.toString();
            return QueryStatus.Failed;
        }
    }
}
