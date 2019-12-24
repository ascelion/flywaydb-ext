package ascelion.flyway.csv;

import static java.util.Arrays.asList;

import java.lang.reflect.Constructor;
import java.nio.charset.Charset;
import java.util.Collection;

import lombok.SneakyThrows;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.internal.scanner.ResourceNameCache;
import org.flywaydb.core.internal.scanner.Scanner;

interface ScannerProvider {
	static ScannerProvider resolveProvider() {
		try {
			return new ScannerProvider60();
		} catch (final NoSuchMethodException e) {
			return new ScannerProvider61();
		}
	}

	class ScannerProvider60 implements ScannerProvider {
		@SuppressWarnings("rawtypes")
		private final Constructor<Scanner> constructor;

		ScannerProvider60() throws NoSuchMethodException {
			this.constructor = Scanner.class.getConstructor(Class.class, Collection.class, ClassLoader.class, Charset.class);
		}

		@SuppressWarnings("unchecked")
		@Override
		@SneakyThrows
		public Scanner<Void> create(Configuration cf, String... suffixes) {
			return this.constructor.newInstance(Void.class, asList(cf.getLocations()), cf.getClassLoader(), cf.getEncoding());
		}
	}

	class ScannerProvider61 implements ScannerProvider {
		private final ResourceNameCache resourceNameCache = new ResourceNameCache();

		@Override
		public Scanner<Void> create(Configuration cf, String... suffixes) {
			return new Scanner<>(Void.class, asList(cf.getLocations()), cf.getClassLoader(), cf.getEncoding(), this.resourceNameCache);
		}
	}

	Scanner<Void> create(Configuration cf, String... suffixes);
}
