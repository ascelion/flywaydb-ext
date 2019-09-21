package ascelion.flyway.java;

import lombok.Getter;
import org.flywaydb.core.api.MigrationType;
import org.flywaydb.core.api.MigrationVersion;
import org.flywaydb.core.api.executor.MigrationExecutor;
import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.ResolvedMigration;
import org.flywaydb.core.internal.util.ClassUtils;

public final class JavaResolvedMigration implements ResolvedMigration {
	private final JavaMigration migration;
	@Getter
	private final MigrationExecutor executor;

	public JavaResolvedMigration(JavaMigration migration) {
		this.migration = migration;
		this.executor = new JavaMigrationExecutor(migration);
	}

	@Override
	public MigrationType getType() {
		return MigrationType.JDBC;
	}

	@Override
	public MigrationVersion getVersion() {
		return this.migration.getVersion();
	}

	@Override
	public String getDescription() {
		return this.migration.getDescription();
	}

	@Override
	public String getScript() {
		return ClassUtils.getLocationOnDisk(this.migration.getClass());
	}

	@Override
	public Integer getChecksum() {
		return this.migration.getChecksum();
	}

	@Override
	public String getPhysicalLocation() {
		return ClassUtils.getLocationOnDisk(this.migration.getClass());
	}
}
