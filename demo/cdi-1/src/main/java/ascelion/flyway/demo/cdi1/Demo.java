package ascelion.flyway.demo.cdi1;

import javax.annotation.Priority;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Destroyed;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.interceptor.Interceptor;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class Demo {
	static private final Logger L = LoggerFactory.getLogger(Demo.class);

	public static void main(String[] args) {
//		stream(System.getProperty("java.class.path").split(":"))
//				.forEach(System.out::println);

		final Weld weld = new Weld();

		try (final WeldContainer container = weld.initialize()) {
			container.select(Demo.class)
					.get()
					.run();
		}
	}

	void run() {
		L.info("Running demo");
	}

	void init(
	//@formatter:off
			@Observes
			@Initialized(ApplicationScoped.class)
			@Priority(Interceptor.Priority.APPLICATION - 100)
			Object unused) {
	//@formatter:on

		L.info("Starting demo");
	}

	void done(
	//@formatter:off
			@Observes
			@Priority(Interceptor.Priority.APPLICATION - 100)
			@Destroyed(ApplicationScoped.class)
			Object unused) {
		//@formatter:on
		L.info("Finishing demo");
	}
}
