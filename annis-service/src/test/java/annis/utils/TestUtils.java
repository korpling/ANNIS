/*
 * Copyright 2009-2011 Collaborative Research Centre SFB 632 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package annis.utils;

import java.util.Arrays;
import java.util.List;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
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
