import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class TestSpringBeanScope {

	@Test
	public void testSpringBeanScope() {
		ApplicationContext ctx = new ClassPathXmlApplicationContext("bean-scope.xml");
		A a = (A) ctx.getBean("a");
		System.out.println(a);
		a = (A) ctx.getBean("a");
		System.out.println(a);
		B b = (B) ctx.getBean("b");
		System.out.println(b);
		b = (B) ctx.getBean("b");
		System.out.println(b);
	}
	
}
