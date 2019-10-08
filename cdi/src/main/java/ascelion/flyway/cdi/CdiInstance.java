package ascelion.flyway.cdi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Set;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.AmbiguousResolutionException;
import javax.enterprise.inject.UnsatisfiedResolutionException;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;

@SuppressWarnings("unchecked")
class CdiInstance<T> {
	private final CreationalContext<T> context;
	private final T instance;
	private final Bean<T> bean;

	CdiInstance(BeanManager bm, Type type, Annotation... qualifiers) {
		final Set<Bean<?>> beans = bm.getBeans(type, qualifiers);

		if (beans.isEmpty()) {
			throw new UnsatisfiedResolutionException("for " + type.getTypeName());
		}
		if (beans.size() > 1) {
			throw new AmbiguousResolutionException("for " + type.getTypeName());
		}

		this.bean = (Bean<T>) beans.iterator().next();
		this.context = bm.createCreationalContext(this.bean);
		this.instance = this.bean.create(this.context);
	}

	CdiInstance(BeanManager bm, Bean<T> bean) {
		this.bean = bean;
		this.context = bm.createCreationalContext(this.bean);
		this.instance = this.bean.create(this.context);
	}

	T get() {
		return this.instance;
	}

	void destroy() {
		this.bean.destroy(this.instance, this.context);
	}
}
