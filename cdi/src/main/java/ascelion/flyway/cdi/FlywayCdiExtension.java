package ascelion.flyway.cdi;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;

import javax.annotation.Priority;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Alternative;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Annotated;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessBean;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.AnnotationLiteral;

import ascelion.cdi.metadata.AnnotatedTypeModifier;
import ascelion.flyway.api.FlywayMigration;

import static java.lang.Thread.currentThread;
import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.resolver.MigrationResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unchecked")
public final class FlywayCdiExtension implements Extension {
	static private final Logger LOG = LoggerFactory.getLogger(FlywayCdiExtension.class);

	private static <A extends Annotation> Optional<A> findMetaAnnotation(Class<A> type, Set<Annotation> visited, Collection<Annotation> annotations) {
		for (final Annotation a : annotations) {
			if (visited.add(a)) {
				final Class<? extends Annotation> t = a.annotationType();

				final Optional<A> result = Optional.ofNullable(t.getAnnotation(type))
						.map(Optional::of)
						.orElseGet(() -> findMetaAnnotation(type, visited, asList(t.getAnnotations())));

				if (result.isPresent()) {
					return result;
				}
			}
		}

		return Optional.empty();
	}

	static <A extends Annotation> Optional<A> findMetaAnnotation(Class<A> type, Annotated annotated) {
		return Optional.ofNullable(annotated.getAnnotation(type))
				.map(Optional::of)
				.orElseGet(() -> findMetaAnnotation(type, new HashSet<>(), annotated.getAnnotations()));
	}

	static class NoFlywayMigration extends AnnotationLiteral<FlywayMigration> implements FlywayMigration {

		@Override
		public String name() {
			return "";
		}

		@Override
		public String[] packages() {
			return new String[0];
		}

		@Override
		public Class<?>[] packageClasses() {
			return new Class[0];
		}

		@Override
		public String[] dependsOn() {
			return new String[0];
		}
	};

	@RequiredArgsConstructor
	static abstract class ConfigurationInstance implements Comparable<ConfigurationInstance> {
		final FlywayMigration annotation;
		final Integer priority;

		abstract Configuration create(BeanManager bm);

		abstract void destroy(Configuration instance);

		@Override
		public int compareTo(ConfigurationInstance that) {
			if (this.priority != null && that.priority != null) {
				return -Integer.compare(this.priority, that.priority);
			}

			if (this.priority == null) {
				return +1;
			}
			if (that.priority == null) {
				return -1;
			}

			return 0;
		}
	}

	static class BeanInstance extends ConfigurationInstance {
		final Bean<Configuration> bean;
		CreationalContext<Configuration> context;

		BeanInstance(Bean<Configuration> bean, FlywayMigration annotation, Integer priority) {
			super(annotation, priority);

			this.bean = bean;
		}

		@Override
		Configuration create(BeanManager bm) {
			this.context = bm.createCreationalContext(this.bean);

			return this.bean.create(this.context);
		}

		@Override
		void destroy(Configuration instance) {
			this.bean.destroy(instance, this.context);
		}
	}

	static class ProducerInstance extends ConfigurationInstance {
		final Producer<Configuration> prod;

		ProducerInstance(Producer<Configuration> prod, FlywayMigration annotation, Integer priority) {
			super(annotation, priority);

			this.prod = prod;
		}

		@Override
		Configuration create(BeanManager bm) {
			return this.prod.produce(bm.createCreationalContext(null));
		}

		@Override
		void destroy(Configuration instance) {
			this.prod.dispose(instance);
		}
	}

	static final class ConfigurationInfo extends Graph.Vertex<String> {
		final ConfigurationInstance instance;

		ConfigurationInfo(ConfigurationInstance instance) {
			super(instance.annotation.name(), instance.annotation.dependsOn());

			this.instance = instance;
		}
	}

	private final Map<String, SortedSet<ConfigurationInstance>> migrations = new HashMap<>();

	void beforeBeanDiscovery(BeanManager bm, @Observes BeforeBeanDiscovery event) {
		final AnnotatedType<FlywayMigration> at = bm.createAnnotatedType(FlywayMigration.class);

		LOG.info("Adding qualifier {}", FlywayMigration.class);

		event.addQualifier(AnnotatedTypeModifier.makeQualifier(at, "value"));

		registerType(bm, event, Configuration.class);
		registerType(bm, event, CdiMigrationResolver.class);
	}

	private void registerType(BeanManager bm, BeforeBeanDiscovery event, Class<?> cls) {
		final String name = cls.getName();
		final AnnotatedType<?> type = bm.createAnnotatedType(cls);

		LOG.info("Adding type {}", name);

		event.addAnnotatedType(type, name);
	}

	void processProducer(BeanManager bm, @Observes ProcessProducer<?, Configuration> event) {
		processConfiguration(event.getAnnotatedMember(), (a, p) -> new ProducerInstance(event.getProducer(), a, p));
	}

	void processBean(BeanManager bm, @Observes ProcessBean<Configuration> event) {
		processConfiguration(event.getAnnotated(), (a, p) -> new BeanInstance(event.getBean(), a, p));
	}

	private void processConfiguration(Annotated annotated, BiFunction<FlywayMigration, Integer, ConfigurationInstance> sup) {
		final FlywayMigration annotation = findMetaAnnotation(FlywayMigration.class, annotated)
				.orElseGet(NoFlywayMigration::new);
		final boolean alternative = findMetaAnnotation(Alternative.class, annotated)
				.map(a -> true)
				.orElse(false);
		final Integer priority = findMetaAnnotation(Priority.class, annotated)
				.map(Priority::value)
				.orElseGet(() -> alternative ? 0 : null);

		LOG.info("Found migration {}, alternative: {}, priority: {}", annotation.name(), alternative, priority);

		this.migrations
				.computeIfAbsent(annotation.name(), k -> new TreeSet<>())
				.add(sup.apply(annotation, priority));
	}

	void afterDeploymentValidation(BeanManager bm, @Observes AfterDeploymentValidation event) {
		final Graph<String, ConfigurationInfo> graph = new Graph<>();

		this.migrations.values().stream()
				.map(s -> s.first())
				.forEach(i -> {
					graph.add(new ConfigurationInfo(i));
				});

		graph
				.sort()
				.forEach(mi -> migrate(bm, mi));
	}

	private void migrate(BeanManager bm, ConfigurationInfo ci) {
		final MigrationResolver res = new CdiMigrationResolver(bm, ci.instance.annotation);
		final Configuration cfg = ci.instance.create(bm);

		try {
			final List<MigrationResolver> resolvers = stream(cfg.getResolvers()).collect(toList());

			resolvers.add(res);

			addCSV(resolvers);

			final Configuration newCfg = Flyway.configure(currentThread().getContextClassLoader())
					.configuration(cfg)
					.resolvers(resolvers.toArray(new MigrationResolver[0]));
			final Flyway fw = new Flyway(newCfg);

			LOG.info("Running migration {}", ci.name);

			fw.migrate();
		} finally {
			ci.instance.destroy(cfg);
		}
	}

	private void addCSV(final List<MigrationResolver> resolvers) {
		try {
			final Class<?> cls = currentThread().getContextClassLoader()
					.loadClass("ascelion.flyway.csv.CSVMigrationResolver");

			resolvers.add((MigrationResolver) cls.newInstance());
		} catch (final NoClassDefFoundError | ClassNotFoundException e) {
			/* ignored */
		} catch (final ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}
}
