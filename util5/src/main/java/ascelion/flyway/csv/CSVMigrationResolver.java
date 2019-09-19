package ascelion.flyway.csv;

import java.util.Collection;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.Scanner;
import org.flywaydb.core.internal.util.Pair;

public final class CSVMigrationResolver implements MigrationResolver {
	static private String[] SUFFIXES = { ".csv", ".CSV" };

	@Override
	public Collection<ResolvedMigration> resolveMigrations(Context context) {
		final Configuration cf = context.getConfiguration();
		final Scanner scn = new Scanner(asList(cf.getLocations()), cf.getClassLoader(), cf.getEncoding());

		return scn.getResources(cf.getSqlMigrationPrefix(), SUFFIXES)
				.stream()
				.map(res -> createMigration(cf, res))
				.collect(toList());
	}

	private CSVResolvedMigration createMigration(Configuration cf, LoadableResource res) {
		final Pair<MigrationVersion, String> mi = MigrationInfoHelper
				.extractVersionAndDescription(res.getFilename(), cf.getSqlMigrationPrefix(), cf.getSqlMigrationSeparator(), SUFFIXES, false);

		final String right = mi.getRight();
		final int sepIx = right.indexOf(cf.getSqlMigrationSeparator());
		final String table;
		final String desc;

		if (sepIx < 0) {
			table = right;
			desc = "IMPORT TABLE " + table;
		} else {
			table = right.substring(0, sepIx);
			desc = right.substring(sepIx + 1);
		}

		return new CSVResolvedMigration(res, mi.getLeft(), table, desc);
	}

}
