package ascelion.flyway.cdi;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Stream;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

import ascelion.cdi.literal.AnyLiteral;
import ascelion.flyway.api.FlywayMigration;
import ascelion.flyway.java.JavaResolvedMigration;

import static java.util.Arrays.stream;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

import org.flywaydb.core.api.migration.JavaMigration;
import org.flywaydb.core.api.resolver.Context;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.flywaydb.core.api.resolver.ResolvedMigration;

final class CdiMigrationResolver implements MigrationResolver {
	private final BeanManager bm;
	private final Set<String> packages;

	CdiMigrationResolver(BeanManager bm, FlywayMigration fm) {
		this.bm = bm;

		if (fm != null) {
			final Stream<String> s1 = stream(fm.packages());
			final Stream<String> s2 = stream(fm.packageClasses()).map(Class::getPackage).map(Package::getName);

			this.packages = Stream.concat(s1, s2).collect(toSet());
		} else {
			this.packages = emptySet();
		}
	}

	@Override
	public Collection<ResolvedMigration> resolveMigrations(Context context) {
		return this.bm.getBeans(JavaMigration.class, AnyLiteral.INSTANCE)
				.stream()
				.filter(this::filterByPackage)
				.map(this::createMigration)
				.map(JavaResolvedMigration::new)
				.collect(toList());
	}

	private JavaMigration createMigration(Bean<?> bean) {
		return ((Bean<JavaMigration>) bean).create(this.bm.createCreationalContext(null));
	}

	private boolean filterByPackage(Bean<?> bean) {
		return this.packages.isEmpty() || this.packages.contains(bean.getBeanClass().getPackage().getName());
	}

}
