package annis.service.internal;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import org.apache.shiro.web.jaxrs.ShiroFeature;


@ApplicationPath("/")
public class JaxrsApplication extends Application {
   
    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();
        classes.add(ShiroFeature.class);
        return classes;
    }
}
