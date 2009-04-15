package annisservice.objects;

import java.io.Serializable;
import java.util.HashSet;

import annisservice.ifaces.AnnisCorpus;
import annisservice.ifaces.AnnisCorpusSet;

public class AnnisCorpusSetImpl extends HashSet<AnnisCorpus> implements Serializable, AnnisCorpusSet {
	private static final long serialVersionUID = 7014311983171387068L;

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
