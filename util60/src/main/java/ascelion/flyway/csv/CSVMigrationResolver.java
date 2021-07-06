
package ascelion.flyway.csv;

import static java.util.Arrays.asList;

import java.util.Collection;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.Scanner;

public final class CSVMigrationResolver extends CSVMigrationResolverBase<LoadableResource> {

	@Override
	protected Collection<LoadableResource> getResources(Configuration cf, String... suffixes) {
		final Scanner<Void> scanner = new Scanner<>(Void.class, asList(cf.getLocations()), cf.getClassLoader(),
				cf.getEncoding());

		return scanner.getResources(cf.getSqlMigrationPrefix(), suffixes);
	}

	@Override
	protected CSVResolvedMigrationBase<LoadableResource> newMigration(Configuration cf, LoadableResource res,
			MigrationVersion version, String table, String desc) {
		return new CSVResolvedMigration(this.references, res, version, table, desc);
	}

}
