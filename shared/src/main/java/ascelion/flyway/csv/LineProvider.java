package ascelion.flyway.csv;

import java.io.Closeable;
import java.io.IOException;

public interface LineProvider extends Closeable {
	@Override
	default void close() throws IOException {
	}

	String nextLine() throws IOException;
}
