package annis.administration;

import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.MockitoAnnotations.initMocks;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;

public class TestCorpusAdministration {

	@Mock private SpringAnnisAdministrationDao administrationDao;
	private CorpusAdministration administration;
	
	@Before
	public void setup() {
		initMocks(this);

		administration = new CorpusAdministration();
		administration.setAdministrationDao(administrationDao);
	}
	
	@Test
	public void importCorporaOne() {
		
		String path = "somePath";
		administration.importCorpora(path);
		
		// insertion of a corpus needs to follow an exact order
		InOrder inOrder = inOrder(administrationDao);
		
		verifyPreImport(inOrder);

		// verify that the corpus was imported
		verifyImport(inOrder, path);
		
		verifyPostImport(inOrder);

		// that should be it
		verifyNoMoreInteractions(administrationDao);
	}
	
	@Test
	public void importCorporaMany() {
		String path1 = "somePath";
		String path2 = "anotherPath";
		String path3 = "yetAnotherPath";
		administration.importCorpora(path1, path2, path3);
		
		// insertion of a corpus needs to follow an exact order
		InOrder inOrder = inOrder(administrationDao);
		
		// drop indexes only once
		verifyPreImport(inOrder);

		// verify that each corpus was inserted in order
		verifyImport(inOrder, path1);
		verifyImport(inOrder, path2);
		verifyImport(inOrder, path3);

		// rebuild materialized tables and indexes only once
		verifyPostImport(inOrder);

		// that should be it
		verifyNoMoreInteractions(administrationDao);
	}

	private void verifyPreImport(InOrder inOrder) {
		inOrder.verify(administrationDao).dropIndexes();
	}

	private void verifyPostImport(InOrder inOrder) {
		inOrder.verify(administrationDao).dropMaterializedTables();
		inOrder.verify(administrationDao).createMaterializedTables();
		inOrder.verify(administrationDao).rebuildIndexes();
	}

	// a correct import requires this order
	private void verifyImport(InOrder inOrder, String path) {
		// create the staging area
		inOrder.verify(administrationDao).createStagingArea();
		
		// bulk import the data
		inOrder.verify(administrationDao).bulkImport(path);
		
		// compute and verify top-level corpus
		inOrder.verify(administrationDao).computeTopLevelCorpus();
		
		// import binaries
		inOrder.verify(administrationDao).importBinaryData(path);
		
		// post-process the data to speed up queries
		inOrder.verify(administrationDao).computeLeftTokenRightToken();
		inOrder.verify(administrationDao).computeComponents();
		inOrder.verify(administrationDao).computeLevel();
		
		// gather statistics about this corpus
		inOrder.verify(administrationDao).computeCorpusStatistics();
		
		// update IDs in staging area
		inOrder.verify(administrationDao).updateIds();
		
		// apply constraints to ensure data integrity
		inOrder.verify(administrationDao).applyConstraints();
		
		// insert the corpus from the staging area to the main db
		inOrder.verify(administrationDao).insertCorpus();
		
		// drop the staging area
		inOrder.verify(administrationDao).dropStagingArea();
	}
	
}
