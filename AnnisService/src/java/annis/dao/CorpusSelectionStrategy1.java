package annis.dao;



public class CorpusSelectionStrategy1 extends BaseCorpusSelectionStrategy {

	public String whereClauseForNode(String docRefColumn) {
		if (selectAll())
			return null;
		return docRefColumn + " " + corpusConstraint();
	}

}
