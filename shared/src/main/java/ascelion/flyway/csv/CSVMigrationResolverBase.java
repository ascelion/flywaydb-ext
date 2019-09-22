package ascelion.flyway.csv;

import java.util.Collection;

import static java.util.stream.Collectors.toList;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.resolver.MigrationInfoHelper;
import org.flywaydb.core.internal.resource.Resource;
import org.flywaydb.core.internal.util.Pair;

public abstract class CSVMigrationResolverBase<R extends Resource> implements MigrationResolver {
	static private String[] SUFFIXES = { ".csv", ".CSV" };

	@Override
	public final Collection<ResolvedMigration> resolveMigrations(Context context) {
		final Configuration cf = context.getConfiguration();

		return getResources(cf, SUFFIXES)
				.stream()
				.map(res -> createMigration(cf, res))
				.collect(toList());
	}

	protected abstract Collection<R> getResources(Configuration cf, String... suffixes);

	protected abstract CSVResolvedMigrationBase<R> newMigration(R res, MigrationVersion version, String table, String desc);

	private CSVResolvedMigrationBase<R> createMigration(Configuration cf, R res) {
		final Pair<MigrationVersion, String> mi = MigrationInfoHelper
				.extractVersionAndDescription(res.getFilename(), cf.getSqlMigrationPrefix(), cf.getSqlMigrationSeparator(), SUFFIXES, false);

		final String right = mi.getRight();
		final int sepIx = right.indexOf(cf.getSqlMigrationSeparator());
		final String table;
		final String desc;

		if (sepIx < 0) {
			table = right.replace(' ', '_');
			desc = "IMPORT TABLE " + table;
		} else {
			table = right.substring(0, sepIx).replace(' ', '_');
			desc = right.substring(sepIx + 1);
		}

		return newMigration(res, mi.getLeft(), table, desc);
	}

}
