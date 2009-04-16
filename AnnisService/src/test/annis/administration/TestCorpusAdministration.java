package annis.administration;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

public class TestCorpusAdministration {

	@Mock private SpringAnnisAdministrationDao databaseUtils;
	private CorpusAdministration administration;
	
	@Before
	public void setup() {
		initMocks(this);

		administration = new CorpusAdministration();
		administration.setAdministrationDao(databaseUtils);
	}
	
	@Test
	public void importCorporaOne() {
		
		String path = "somePath";
		administration.importCorpora(path);
		
		// insertion of a corpus needs to follow an exact order
		InOrder inOrder = inOrder(databaseUtils);
		
		verifyPreImport(inOrder);

		// verify that the corpus was imported
		verifyImport(inOrder, path);
		
		verifyPostImport(inOrder);

		// that should be it
		verifyNoMoreInteractions(databaseUtils);
	}
	
	@Test
	public void importCorporaMany() {
		String path1 = "somePath";
		String path2 = "anotherPath";
		String path3 = "yetAnotherPath";
		administration.importCorpora(path1, path2, path3);
		
		// insertion of a corpus needs to follow an exact order
		InOrder inOrder = inOrder(databaseUtils);
		
		// drop indexes only once
		verifyPreImport(inOrder);

		// verify that each corpus was inserted in order
		verifyImport(inOrder, path1);
		verifyImport(inOrder, path2);
		verifyImport(inOrder, path3);

		// rebuild materialized tables and indexes only once
		verifyPostImport(inOrder);

		// that should be it
		verifyNoMoreInteractions(databaseUtils);
	}

	private void verifyPreImport(InOrder inOrder) {
		inOrder.verify(databaseUtils).dropIndexes();
	}

	private void verifyPostImport(InOrder inOrder) {
		inOrder.verify(databaseUtils).dropMaterializedTables();
		inOrder.verify(databaseUtils).createMaterializedTables();
		inOrder.verify(databaseUtils).rebuildIndexes();
	}

	// a correct import requires this order
	private void verifyImport(InOrder inOrder, String path) {
		// create the staging area
		inOrder.verify(databaseUtils).createStagingArea();
		
		// bulk import the data
		inOrder.verify(databaseUtils).bulkImport(path);
		
		// import binaries
		inOrder.verify(databaseUtils).importBinaryData(path);
		
		// post-process the data to speed up queries
		inOrder.verify(databaseUtils).computeLeftTokenRightToken();
		inOrder.verify(databaseUtils).computeComponents();
		inOrder.verify(databaseUtils).computeLevel();
		
		// gather statistics about this corpus
		inOrder.verify(databaseUtils).computeCorpusStatistics();
		
		// update IDs in staging area
		inOrder.verify(databaseUtils).updateIds();
		
		// apply constraints to ensure data integrity
		inOrder.verify(databaseUtils).applyConstraints();
		
		// insert the corpus from the staging area to the main db
		inOrder.verify(databaseUtils).insertCorpus();
		
		// drop the staging area
		inOrder.verify(databaseUtils).dropStagingArea();
	}
	
}
