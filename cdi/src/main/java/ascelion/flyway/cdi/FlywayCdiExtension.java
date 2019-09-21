package ascelion.flyway.cdi;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AfterTypeDiscovery;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;

import ascelion.cdi.literal.AnyLiteral;
import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.flyway.api.FlywayMigration;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class FlywayCdiExtension implements Extension {
	static private final Logger L = LoggerFactory.getLogger(FlywayCdiExtension.class);

	static Optional<FlywayMigration> flywayMigrationAnnotation(Bean<?> bean) {
		return bean.getQualifiers().stream()
				.filter(q -> q.annotationType() == FlywayMigration.class)
				.map(FlywayMigration.class::cast)
				.findAny();
	}

	static class CfBeanInfo extends Graph.Vertex<String> {
		static CfBeanInfo create(Bean<?> bean) {
			return new CfBeanInfo((Bean<Configuration>) bean, flywayMigrationAnnotation(bean).get());
		}

		private final Bean<Configuration> bean;
		private final FlywayMigration annotation;

		private CfBeanInfo(Bean<Configuration> bean, FlywayMigration annotation) {
			super(annotation.name(), annotation.dependsOn());

			this.bean = bean;
			this.annotation = annotation;
		}
	}

	void beforeBeanDiscovery(BeanManager bm, @Observes BeforeBeanDiscovery event) {
		final AnnotatedType<FlywayMigration> at = bm.createAnnotatedType(FlywayMigration.class);
		final AnnotatedTypeModifier<FlywayMigration> atm = AnnotatedTypeModifier.create(at);

		L.info("Adding qualifier {}", FlywayMigration.class);

		event.addQualifier(atm.makeQualifier("value"));
	}

	void afterTypeDiscovery(BeanManager bm, @Observes AfterTypeDiscovery event) {
		registerType(bm, event, Configuration.class);
		registerType(bm, event, CdiMigrationResolver.class);
	}

	void afterDeploymentValidation(BeanManager bm, @Observes AfterDeploymentValidation event) {
		final Set<Bean<?>> beans = bm.getBeans(Configuration.class, AnyLiteral.INSTANCE);

		switch (beans.size()) {
		case 0:
			break;
		case 1:
			final Bean<Configuration> bean = (Bean<Configuration>) beans.iterator().next();

			migrate(bm, bean, flywayMigrationAnnotation(bean).orElse(null));

			break;

		default:
			final Graph<String, CfBeanInfo> graph = new Graph<>();

			beans.stream()
					.map(CfBeanInfo::create)
					.forEach(graph::add);

			graph.sort().forEach(v -> migrate(bm, v.bean, v.annotation));
		}
	}

	private void migrate(BeanManager bm, Bean<Configuration> bean, FlywayMigration fm) {
		final CdiInstance<Configuration> cfi = new CdiInstance<>(bm, bean);
		final MigrationResolver res = new CdiMigrationResolver(bm, fm);

		try {
			final List<MigrationResolver> resolvers = stream(cfi.get().getResolvers()).collect(toList());

			resolvers.add(res);

			addCSV(resolvers);

			final Configuration cfg = Flyway.configure(currentThread().getContextClassLoader())
					.configuration(cfi.get())
					.resolvers(resolvers.toArray(new MigrationResolver[0]));
			final Flyway fw = new Flyway(cfg);

			fw.migrate();
		} finally {
			cfi.destroy();
		}
	}

	private void registerType(BeanManager bm, AfterTypeDiscovery event, Class<?> cls) {
		final String name = cls.getName();
		final AnnotatedType<?> type = bm.createAnnotatedType(cls);

		L.info("Adding type {}", name);

		event.addAnnotatedType(type, name);
	}

	private void addCSV(final List<MigrationResolver> resolvers) {
		try {
			final Class<?> cls = currentThread().getContextClassLoader().loadClass("ascelion.flyway.csv.CSVMigrationResolver");

			resolvers.add((MigrationResolver) cls.newInstance());
		} catch (final NoClassDefFoundError | ClassNotFoundException e) {
			/* ignored */
		} catch (final ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
