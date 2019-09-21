package ascelion.flyway.csv;

import java.util.Collection;

import static java.util.Arrays.asList;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.Scanner;

public final class CSVMigrationResolver extends CSVMigrationResolverBase<LoadableResource> {

	@Override
	protected Collection<LoadableResource> getResources(Configuration cf, String... suffixes) {
		final Scanner scn = new Scanner(asList(cf.getLocations()), cf.getClassLoader(), cf.getEncoding());

		return scn.getResources(cf.getSqlMigrationPrefix(), suffixes);
	}

	@Override
	protected CSVResolvedMigrationBase<LoadableResource> newMigration(LoadableResource res, MigrationVersion version, String table, String desc) {
		return new CSVResolvedMigration(res, version, table, desc);
	}

}
