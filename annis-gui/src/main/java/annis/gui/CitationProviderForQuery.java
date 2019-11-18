package annis.gui;

import java.util.HashSet;
import java.util.Set;

import annis.gui.beans.CitationProvider;
import annis.model.ContextualizedQuery;
import annis.model.Query;

public class CitationProviderForQuery implements CitationProvider {
    private final Query query;

    public CitationProviderForQuery(Query query) {
      this.query = query;
    }

    @Override
    public String getQuery() {
      if (query == null) {
        return null;
      }
      return query.getQuery();
    }

    @Override
    public Set<String> getCorpora() {
      if (query == null) {
        return new HashSet<>();
      }
      return query.getCorpora();
    }

    @Override
    public int getLeftContext() {
      if (query instanceof ContextualizedQuery) {
        return ((ContextualizedQuery) query).getLeftContext();
      }
      return 5;
    }

    @Override
    public int getRightContext() {
      if (query instanceof ContextualizedQuery) {
        return ((ContextualizedQuery) query).getRightContext();
      }
      return 5;
    }

  }