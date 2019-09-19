package ascelion.flyway.demo.cdi1;

import javax.enterprise.inject.Produces;
import javax.inject.Named;
import javax.inject.Singleton;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;

public class FlywayInit {

	@Produces
	@Named("database.importFile")
	String importFile = "data.csv";

	@Produces
	@Singleton
	Configuration configuration() {
		return Flyway.configure()
				.dataSource("jdbc:h2:mem:db1", "sa", "sa")
				.locations("db");
	}
}
