# Flyway DB extensions

Features:

- Supports both version 5 and 6
- CDI extension
- CSV migration support
- ... more to come

## General Usage ##

- Gradle

```
repositories {
	jcenter()
}

dependencies {
	implementation platform( 'ascelion.flywaydb:flywaydb-ext:<LATEST VERSION>' )
}

```

- Maven

```
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>ascelion.flywaydb</groupId>
			<artifactId>flywaydb-ext</artifactId>
			<version><LATEST VERSION></version>
			<type>pom</type>
			<scope>import</scope>
	</dependencies>
</dependencyManagement>

```

## CSV migration support ##

Allows to use CSV files to import data. The file name follows the same naming convention and must sit in the same location as the SQL scripts.

The name of the table to import into is taken from the description; the first segment of the description is the table name and can be followed by the actual description of the import.

You must register the class CSVMigrationResolver to the Flyway configuration.

Dependencies:
- __ascelion.flywaydb:flywaydb-ext-util5__ for version 5
- __ascelion.flywaydb:flywaydb-ext-util6__ for version 6.

## CDI extension ##

Runs Flyway migrations on startup; multiple migrations are supported.

You have to create producers for each migration configuration and optionally use the qualifier @FlywayMigration for each of them.

The CSVMigrationResolver is registered automatically if found on classpath.

Dependencies:
- __ascelion.flywaydb:flywaydb-ext-cdi__ (for both CDI-1 and CDI-2)
- __org.flywaydb:flyway-core:5.+__ or __ascelion.flywaydb:flywaydb-ext-util5__ for version 5
- __org.flywaydb:flyway-core:6.+__ or __ascelion.flywaydb:flywaydb-ext-util5__ for version 6
