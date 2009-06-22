package test;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.lang.reflect.Proxy;

import org.apache.commons.lang.Validate;
import org.junit.Test;
import org.springframework.aop.target.SingletonTargetSource;

// XXX: is this really necessary?
public class TestHelper {

	// return the path that corresponds to the package of a class
	public static String packagePath(Class<?> clazz) {
		String packageDeclaration = clazz.getPackage().toString();
		String path = packageDeclaration.replaceFirst("package ", "").replaceAll("\\.", "/") + "/";
		return path;
	}
	
	// return the path that corresponds to the package of a class instance
	public static String packagePath(Object instance) {
		return packagePath(instance.getClass());
	}
	
	// return the qualified Spring context XML file name
	public static String springFile(Class<?> clazz, String contextFile) {
		return packagePath(clazz) + contextFile;
	}
	
	// return the qualified Spring context XML file name
	public static String springFile(Object instance, String contextFile) {
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
		assertThat(packagePath(this), is("test/"));
	}

	@Test
	public void testSpringFile() {
		assertThat(springFile(this, "context.xml"), is("test/context.xml"));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void testSpringFilesEmpty() {
		springFiles(this);
	}
	
	@Test
	public void testSpringFiles() {
		String[] expected = {
				"test/context1.xml",
				"test/context2.xml"
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
