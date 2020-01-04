package ascelion.flyway.sb2;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import ascelion.flyway.csv.CSVMigrationResolver;
import ascelion.flyway.java.JavaResolvedMigration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.flyway.FlywayConfigurationCustomizer;
import org.springframework.context.annotation.Configuration;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.FluentConfiguration;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;

@Configuration
@ConditionalOnClass(Flyway.class)
@ConditionalOnProperty(prefix = "spring.flyway", name = "enabled", matchIfMissing = true)
@Slf4j
public class FlywayMigrationsConfig implements FlywayConfigurationCustomizer {

	@Autowired(required = false)
	private JavaMigration[] migrations;

	@Override
	public void customize(FluentConfiguration cf) {
		final List<MigrationResolver> resolvers = new ArrayList<>(asList(cf.getResolvers()));

		resolvers.add(new CSVMigrationResolver());

		if (this.migrations != null) {
			resolvers.add(this::resolveJavaMigrations);
		}

		log.info("Flyway resolvers: {}", resolvers);
		log.info("Flyway locations: {}", asList(cf.getLocations()));

		cf.resolvers(resolvers.toArray(new MigrationResolver[0]));
	}

	private Collection<ResolvedMigration> resolveJavaMigrations(Context context) {
		return stream(this.migrations)
				.map(JavaResolvedMigration::new)
				.collect(toList());
	}
}
