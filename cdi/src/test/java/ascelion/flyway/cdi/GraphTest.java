package ascelion.flyway.cdi;

import java.util.List;

import ascelion.flyway.cdi.Graph.Vertex;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class GraphTest {

	@Test(expected = Graph.CycleException.class)
	public void has_cycle() {
		final Graph<String, Graph.Vertex<String>> graph = new Graph<>();

		final Vertex<String> v1 = new Graph.Vertex<>("a", "b");
		final Vertex<String> v2 = new Graph.Vertex<>("b", "a");

		graph.add(v1);
		graph.add(v2);

		try {
			graph.sort();
		} catch (final Graph.CycleException e) {
			assertThat(e.cycle(), hasSize(3));

			throw e;
		}
	}

	@Test(expected = Graph.LookupException.class)
	public void not_found() {
		final Graph<String, Graph.Vertex<String>> graph = new Graph<>();

		final Vertex<String> v1 = new Graph.Vertex<>("a", "b");
		final Vertex<String> v2 = new Graph.Vertex<>("b", "c");

		graph.add(v2);
		graph.add(v1);

		graph.sort();
	}

	@Test
	public void is_sorted() {
		final Graph<String, Graph.Vertex<String>> graph = new Graph<>();

		final Vertex<String> v1 = new Graph.Vertex<>("a", "b");
		final Vertex<String> v2 = new Graph.Vertex<>("b", "c");
		final Vertex<String> v3 = new Graph.Vertex<>("c");

		graph.add(v3);
		graph.add(v2);
		graph.add(v1);

		final List<Vertex<String>> sorted = graph.sort().stream().collect(toList());

		final int x1 = sorted.indexOf(v1);
		final int x2 = sorted.indexOf(v2);
		final int x3 = sorted.indexOf(v3);

		assertThat(x1, lessThan(x2));
		assertThat(x2, lessThan(x3));
	}
}
