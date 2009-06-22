package annis.service.ifaces;

/**
 * Represents a Corpus.
 * 
 * @author k.huetter
 *
 */
public interface AnnisCorpus extends JSONAble {

	public abstract long getId();

	public abstract void setId(long id);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract int getTextCount();

	public abstract void setTextCount(int textCount);

	public abstract int getTokenCount();

	public abstract void setTokenCount(int tokenCount);

}