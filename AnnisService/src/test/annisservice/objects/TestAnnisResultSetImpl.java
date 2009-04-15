package annisservice.objects;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import java.util.Comparator;

import org.junit.Before;
import org.junit.Test;

import annisservice.ifaces.AnnisResult;

public class TestAnnisResultSetImpl {

	private Comparator<AnnisResult> comparator;
	private AnnisResult annisResult1;
	private AnnisResult annisResult2;
	private AnnisTokenImpl start1;
	private AnnisTokenImpl start2;
	private AnnisTokenImpl end1;
	private AnnisTokenImpl end2;
	
	@Before
	public void setup() {
		comparator = new AnnisResultSetImpl.AnnisResultSetComparator();
		start1 = new AnnisTokenImpl();
		start2 = new AnnisTokenImpl();
		end1 = new AnnisTokenImpl();
		end2 = new AnnisTokenImpl();
		annisResult1 = new AnnisResultImpl();
		annisResult1.getTokenList().add(start1);
		annisResult1.getTokenList().add(end1);
		annisResult2 = new AnnisResultImpl();
		annisResult2.getTokenList().add(start2);
		annisResult2.getTokenList().add(end2);
	}

	@Test
	public void testComparatorDifferentStart() {
		start1.setId(1);
		start2.setId(2);
		assertThat(comparator.compare(annisResult1, annisResult2), is(lessThan(0)));
	}
	
	@Test
	public void testComparatorSameStartDifferentEnd() {
		start1.setId(1);
		start2.setId(1);
		end1.setId(2);
		end2.setId(3);
		assertThat(comparator.compare(annisResult1, annisResult2), is(lessThan(0)));
	}
	
	@Test
	public void testComparatorSameStartSameEnd() {
		start1.setId(1);
		start2.setId(1);
		end1.setId(2);
		end2.setId(2);
		assertThat(comparator.compare(annisResult1, annisResult2), is(lessThan(0)));
	}
	
}
