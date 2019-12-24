package ascelion.flyway.csv;

import java.util.Collection;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.resource.LoadableResource;

public final class CSVMigrationResolver extends CSVMigrationResolverBase<LoadableResource> {
	private final ScannerProvider sp = ScannerProvider.resolveProvider();

	@Override
	protected Collection<LoadableResource> getResources(Configuration cf, String... suffixes) {
		return this.sp.create(cf, suffixes)
				.getResources(cf.getSqlMigrationPrefix(), suffixes);
	}

	@Override
	protected CSVResolvedMigrationBase<LoadableResource> newMigration(LoadableResource res, MigrationVersion version, String table, String desc) {
		return new CSVResolvedMigration(this.references, res, version, table, desc);
	}

}
