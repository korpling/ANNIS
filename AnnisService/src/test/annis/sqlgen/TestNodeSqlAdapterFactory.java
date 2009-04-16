package annis.sqlgen;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import annis.dao.CorpusSelectionStrategy;
import annis.model.AnnisNode;

public class TestNodeSqlAdapterFactory {

	// object under test
	private NodeSqlAdapterFactory nodeSqlAdapterFactory;

	// dummy dependencies
	@Mock private AnnisNode node;
	@Mock private CorpusSelectionStrategy corpusSelectionStrategy;
	
	// a dummy adapter
	class DummySqlTableNodeAdapter extends AbstractNodeSqlAdapter {

		public String selectClause() {
			return null;
		}

		public String selectClauseNullValues() {
			return null;
		}
		
		public String fromClause() {
			return null;
		}

		public List<String> whereClause() {
			return null;
		}

		public String getStructTable() {
			return null;
		}

		public void setStructTable(String structTable) { }

	};
	
	@Before
	public void setup() {
		initMocks(this);
		
		// create a factory that returns the dummy node adapter
		nodeSqlAdapterFactory = new NodeSqlAdapterFactory() {
			@Override
			public AbstractNodeSqlAdapter createNodeSqlAdapter() {
				return new DummySqlTableNodeAdapter();
			}
		};
		
	}
	
	// node, corpusSelectionStrategy is passed to created adapter
	@Test
	public void dependenciesPassedToAdapter() {
		// create an adapter for the mocked node
		AbstractNodeSqlAdapter adapter = (AbstractNodeSqlAdapter) 
			nodeSqlAdapterFactory.createNodeSqlAdapter(node, corpusSelectionStrategy);
		
		// verify that node and corpusSelectionStrategy are passed to the adapter
		assertThat(adapter.getNode(), is(sameInstance(node)));
		assertThat(adapter.getCorpusSelectionStrategy(), is(sameInstance(corpusSelectionStrategy)));
	}
	
	// adapter is registered with corpusSelectionStrategy
	@Test
	public void registerAdapterWithCorpusSelectionStragey() {
		NodeSqlAdapter adapter = nodeSqlAdapterFactory.createNodeSqlAdapter(node, corpusSelectionStrategy);
		verify(corpusSelectionStrategy).registerNodeAdapter(adapter);
	}
	
}
