
package ascelion.flyway.csv;

import static java.util.Arrays.asList;

import java.io.Reader;
import java.util.Collection;
import java.util.List;

import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.parser.PlaceholderReplacingReader;
import org.flywaydb.core.internal.resolver.ChecksumCalculator;
import org.flywaydb.core.internal.resource.LoadableResource;
import org.flywaydb.core.internal.scanner.LocationScannerCache;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.Scanner;

public final class CSVMigrationResolver extends CSVMigrationResolverBase<LoadableResource> {

	private final ResourceNameCache resourceNameCache = new ResourceNameCache();
	private final LocationScannerCache locationScannerCache = new LocationScannerCache();

	@Override
	protected Collection<LoadableResource> getResources(Configuration cf, String... suffixes) {
		final Scanner<Void> scanner = new Scanner<>(Void.class, asList(cf.getLocations()), cf.getClassLoader(),
				cf.getEncoding(), this.resourceNameCache,
				this.locationScannerCache);

		return scanner.getResources(cf.getSqlMigrationPrefix(), suffixes);
	}

	@Override
	protected CSVResolvedMigrationBase<LoadableResource> newMigration(Configuration cf,
			LoadableResource res, MigrationVersion version, String table, String desc) {
		final int checksum = getChecksumForLoadableResource(cf, res);

		return new CSVResolvedMigration(this.references, res, version, table, desc, checksum);
	}

	private Integer getChecksumForLoadableResource(Configuration cf, LoadableResource res) {
		if (cf.isPlaceholderReplacement()) {
			return ChecksumCalculator.calculate(createPlaceholderReplacingLoadableResource(cf, res));
		}

		return ChecksumCalculator.calculate(res);
	}

	private LoadableResource createPlaceholderReplacingLoadableResource(Configuration cf, LoadableResource res) {
		return new LoadableResource() {
			@Override
			public Reader read() {
				return PlaceholderReplacingReader.create(
						cf,
						parsingContext,
						res.read());
			}

			@Override
			public String getAbsolutePath() {
				return res.getAbsolutePath();
			}

			@Override
			public String getAbsolutePathOnDisk() {
				return res.getAbsolutePathOnDisk();
			}

			@Override
			public String getFilename() {
				return res.getFilename();
			}

			@Override
			public String getRelativePath() {
				return res.getRelativePath();
			}
		};
	}

}
