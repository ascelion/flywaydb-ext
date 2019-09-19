package ascelion.flyway.demo.cdi1;

import java.io.IOException;

import static java.util.Arrays.stream;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class Demo {
	public static void main(String[] args) throws IOException {
		stream(System.getProperty("java.class.path").split(":"))
				.forEach(System.out::println);

		final Weld weld = new Weld();
		final WeldContainer container = weld.initialize();
		final Demo application = container.select(Demo.class).get();

		application.run();

		weld.shutdown();
	}

	private void run() {
	}
}
