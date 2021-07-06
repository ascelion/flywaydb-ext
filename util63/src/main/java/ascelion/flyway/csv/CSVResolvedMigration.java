package ascelion.flyway.csv;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resource.LoadableResource;

final class CSVResolvedMigration extends CSVResolvedMigrationBase<LoadableResource> implements ResolvedMigration {

	CSVResolvedMigration(Map<String, List<String>> references, LoadableResource resource, MigrationVersion version,
			String table, String description, int checksum) {
		super(references, resource, version, table, description, checksum);
	}

	@Override
	protected LineProvider openResource() throws IOException {
		final Reader in = getResource().read();
		final BufferedReader rd = new BufferedReader(in);

		return new LineProvider() {

			@Override
			public String nextLine() throws IOException {
				return rd.readLine();
			}

			@Override
			public void close() throws IOException {
				rd.close();
			}
		};
	}

	@Override
	public boolean checksumMatches(Integer checksum) {
		return checksum == null || Objects.equals(checksum, getChecksum());
	}

	@Override
	public boolean checksumMatchesWithoutBeingIdentical(Integer checksum) {
		return checksum == null;
	}
}
