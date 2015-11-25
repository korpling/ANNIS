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
package annis.test;

import java.lang.reflect.Proxy;
import org.apache.commons.lang3.Validate;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.springframework.aop.target.SingletonTargetSource;

// XXX: is this really necessary?
public class TestHelper {

	// return the path that corresponds to the package of a class
	private static String packagePath(Class<?> clazz) {
		String packageDeclaration = clazz.getPackage().toString();
		String path = packageDeclaration.replaceFirst("package ", "").replaceAll("\\.", "/") + "/";
		return path;
	}
	
	// return the path that corresponds to the package of a class instance
	private static String packagePath(Object instance) {
		return packagePath(instance.getClass());
	}
	
	// return the qualified Spring context XML file name
	private static String springFile(Class<?> clazz, String contextFile) {
		return packagePath(clazz) + contextFile;
	}
	
	// return the qualified Spring context XML file name
	private static String springFile(Object instance, String contextFile) {
		return springFile(instance.getClass(), contextFile);
	}
	
	// return an array with qualifed Spring context XML file names
	public static String[] springFiles(Class<?> clazz, String... contextFiles) {
		Validate.notEmpty(contextFiles, "need at least one file name");
		String[] springFiles = new String[contextFiles.length];
		String packagePath = packagePath(clazz);
		for (int i = 0; i < contextFiles.length; ++i)
			springFiles[i] = packagePath + contextFiles[i];
		return springFiles;
	}
	
	// return an array with qualifed Spring context XML file names
	public static String[] springFiles(Object instance, String... contextFiles) {
		return springFiles(instance.getClass(), contextFiles);
	}
	
	// sanity checks
	
	@Test
	public void testPackagePath() {
		assertThat(packagePath(this), is("annis/test/"));
	}

	@Test
	public void testSpringFile() {
		assertThat(springFile(this, "context.xml"), is("annis/test/context.xml"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSpringFilesEmpty() {
		springFiles(this);
	}
	
	@Test
	public void testSpringFiles() {
		String[] expected = {
				"annis/test/context1.xml",
				"annis/test/context2.xml"
		};
		assertThat(springFiles(this, "context1.xml", "context2.xml"), is(expected));
	}

	// retrieve the underlying SpringAnnisDao class of the proxy that is returned by Spring
	public static Object proxyTarget(Object proxy) {
		if ( ! (proxy instanceof Proxy) )
			fail("Not a proxy: " + proxy);
		
		SingletonTargetSource targetSource = null;
		try {
			targetSource = (SingletonTargetSource) proxy.getClass().getMethod("getTargetSource").invoke(proxy);
		} catch (Exception e) {
			fail(e.getMessage());
		}
	
		if (targetSource == null)
			fail("Couldn't get target of annisDao proxy");
		
		return targetSource.getTarget();
	}
}
