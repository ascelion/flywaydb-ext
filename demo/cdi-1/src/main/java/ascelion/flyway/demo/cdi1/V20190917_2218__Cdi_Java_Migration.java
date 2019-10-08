package ascelion.flyway.demo.cdi1;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Named;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

public class V20190917_2218__Cdi_Java_Migration extends BaseJavaMigration {

	@Inject
	@Named(FlywayInit.INJECTED_VALUE)
	private UUID injectedValue;

	@Override
	public void migrate(Context context) throws Exception {
		Objects.requireNonNull(this.injectedValue);
	}

}
