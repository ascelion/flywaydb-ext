package ascelion.flyway.demo.sb2;

import java.util.Objects;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;

@Component
@Slf4j
public class V20190917_2218__Sb2_Java_Migration extends BaseJavaMigration {

	@Autowired
	private UUID injectedValue;

	@Override
	public void migrate(Context context) throws Exception {
		log.info("Injected Value: {}", this.injectedValue);

		Objects.requireNonNull(this.injectedValue);
	}

}
