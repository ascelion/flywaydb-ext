package ascelion.flyway.demo.cdi2.db2;

import java.util.Objects;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.inject.Named;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

@Dependent
public class V20190917_2218__Cdi_Java_Migration extends BaseJavaMigration {

	@Inject
	@Named("database.importFile")
	private String importFile;

	@Override
	public void migrate(Context context) throws Exception {
		Objects.requireNonNull(this.importFile);
	}

}
