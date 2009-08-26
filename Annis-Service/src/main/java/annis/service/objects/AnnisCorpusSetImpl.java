package annis.service.objects;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

import annis.service.ifaces.AnnisCorpus;
import annis.service.ifaces.AnnisCorpusSet;

public class AnnisCorpusSetImpl extends HashSet<AnnisCorpus> implements Serializable, AnnisCorpusSet {
	private static final long serialVersionUID = 7014311983171387068L;

	public AnnisCorpusSetImpl() {
		super();
	}

	public AnnisCorpusSetImpl(Collection<? extends AnnisCorpus> collection) {
		super(collection);
	}

	/* (non-Javadoc)
	 * @see annisservice.objects.AnnisCorpusList#getJSON()
	 */
	public String getJSON() {
		StringBuffer json = new StringBuffer("{ \"size\":" + this.size() + ",\"list\":[");
		int count = 0;
		for(AnnisCorpus corpus : this) {
			if(count++ > 0)
				json.append(", ");
			json.append(corpus.getJSON());
		}
		return json.toString() + "]}";	
	}

}
