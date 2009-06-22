package annis.utils;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class TestUtils {

	@Test
	public void min() {
		List<Long> values = Arrays.asList(23L, 42L, 99L);
		assertThat(Utils.min(values), is("23"));
	}
	
	@Test
	public void max() {
		List<Long> values = Arrays.asList(23L, 42L, 99L);
		assertThat(Utils.max(values), is("99"));
	}

	@Test
	public void avg() {
		List<Long> values = Arrays.asList(23L, 42L, 99L);
		assertThat(Utils.avg(values), is("54"));
	}
}
