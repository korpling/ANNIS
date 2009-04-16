package annis.dao;

import java.util.List;

public abstract class CorpusSelectionStrategyFactory {

	public CorpusSelectionStrategy createCorpusSelectionStrategy(List<Long> corpusList) {
		BaseCorpusSelectionStrategy corpusSelectionStrategy = createCorpusSelectionStrategy();
		corpusSelectionStrategy.setCorpusList(corpusList);
		return corpusSelectionStrategy;
	}

	public abstract BaseCorpusSelectionStrategy createCorpusSelectionStrategy();
	
}