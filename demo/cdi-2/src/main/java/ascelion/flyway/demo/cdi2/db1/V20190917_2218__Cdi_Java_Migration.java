package ascelion.flyway.demo.cdi2.db1;

import java.util.Objects;
import java.util.UUID;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import ascelion.flyway.demo.cdi2.FlywayInit;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

@Dependent
public class V20190917_2218__Cdi_Java_Migration extends BaseJavaMigration {
	@Inject
	@Named(FlywayInit.INJECTED_VALUE)
	private UUID injectedValue;

	@Override
	public void migrate(Context context) {
		Objects.requireNonNull(this.injectedValue);
	}

}
