package ascelion.flyway.csv;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static java.util.Optional.ofNullable;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.internal.line.Line;
import org.flywaydb.core.internal.line.LineReader;
import org.flywaydb.core.internal.resource.LoadableResource;

final class CSVResolvedMigration extends CSVResolvedMigrationBase<LoadableResource> {

	CSVResolvedMigration(Map<String, List<String>> references,
			LoadableResource resource, MigrationVersion version,
			String table, String description) {

		super(references, resource, version, table, description, resource.checksum());
	}

	@Override
	protected LineProvider openResource() throws IOException {
		final LineReader rd = getResource().loadAsString();

		return () -> ofNullable(rd.readLine())
				.map(Line::getLine)
				.orElse(null);
	}
}
