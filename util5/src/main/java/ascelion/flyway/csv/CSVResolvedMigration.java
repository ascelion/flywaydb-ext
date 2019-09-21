package ascelion.flyway.csv;

import java.io.IOException;

import static java.util.Optional.ofNullable;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.line.Line;
import org.flywaydb.core.internal.line.LineReader;
import org.flywaydb.core.internal.resource.LoadableResource;

final class CSVResolvedMigration extends CSVResolvedMigrationBase<LoadableResource> {

	CSVResolvedMigration(LoadableResource resource, MigrationVersion version, String table, String description) {
		super(resource, version, table, description, resource.checksum());
	}

	@Override
	protected LineProvider openResource() throws IOException {
		final LineReader rd = getResource().loadAsString();

		return () -> ofNullable(rd.readLine())
				.map(Line::getLine)
				.orElse(null);
	}
}
