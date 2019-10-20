package ascelion.flyway.cdi;

import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.Collections.unmodifiableCollection;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("unchecked")
final class Graph<T, V extends Graph.Vertex<T>> {

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	static class CycleException extends RuntimeException {
		private final Collection<?> cycle;

		<V> Collection<V> cycle() {
			return (Collection<V>) this.cycle;
		}
	}

	@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
	static class LookupException extends RuntimeException {
		private final Object name;

		<T> T name() {
			return (T) this.name;
		}
	}

	@EqualsAndHashCode(of = "name")
	static class Vertex<D> {
		final D name;
		final D[] dependencies;

		Vertex(D name, D... dependencies) {
			this.name = name;
			this.dependencies = dependencies;
		}

		@Override
		public String toString() {
			return this.name.toString();
		}
	}

	private final Map<T, V> vertices = new HashMap<>();

	void add(V vertex) {
		this.vertices.compute(vertex.name, (k, v) -> {
			if (v != null) {
				throw new IllegalArgumentException("Vertex already present" + k);
			}

			return vertex;
		});
	}

	Collection<V> sort() {
		final Deque<V> sorted = new LinkedList<>();
		final Deque<V> cycle = new LinkedList<>();
		final Map<V, Boolean> visited = new IdentityHashMap<>();

		for (final V v : this.vertices.values()) {
			visit(v, visited, sorted, cycle);
		}

		return unmodifiableCollection(sorted);
	}

	private void visit(V v, Map<V, Boolean> visited, Deque<V> stack, Deque<V> cycle) {
		if (cycle.contains(v)) {
			cycle.push(v);

			throw new CycleException(cycle);
		}

		cycle.push(v);

		if (visited.get(v) == null) {
			visited.put(v, true);

			stream(v.dependencies)
					.map(this::lookup)
					.forEach(dep -> visit(dep, visited, stack, cycle));

			stack.addLast(v);
		}

		cycle.pop();
	}

	private V lookup(T name) {
		final V v = this.vertices.get(name);

		if (v == null) {
			throw new LookupException(name);
		}

		return v;
	}
}
