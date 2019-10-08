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
	implementation platform( 'ascelion.flywaydb:flywaydb-ext:1.0.0' )
}

```

- Maven

```
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>ascelion.flywaydb</groupId>
			<artifactId>flywaydb-ext</artifactId>
			<version>1.0.0</version>
			<type>pom</type>
			<scope>import</scope>
	</dependencies>
</dependencyManagement>

```

## CSV imports support ##

Allows to use CSV files to import data. The file name follows the same naming convention and must be placed in the same location as the SQL scripts.

The CSV file must provide a header containing the names of the columns that shall be imported.

The name of the table to import into is taken from the description; the first segment of the description is the table name and can be followed by the actual description of the import.

You must register the class CSVMigrationResolver to the Flyway configuration.

Dependencies:
- __ascelion.flywaydb:flywaydb-ext-util5__ for version 5
- __ascelion.flywaydb:flywaydb-ext-util6__ for version 6.

### Foreign key support ###

The CSV importer can handle foreign keys that reference primary keys generated for records imported by a previous script. To accomplish this, the column name specified in the CSV header must
have the form:

```
	COLUMN1|REFERENCED_TABLE_1,COLUMN2|REFERENCED_TABLE_2
```

... then for each column we must either use the index of the record imported previously, or use an empty value meaning the index of the current row.

The following will associate the users at row 0, 1, 2 with the role at row 2, then the user at row 0 with the roles at row 0 and 1

```
	role_id|roles,user_id|users
	2,
	2,
	2,
	0, 0
	1, 0
```

Note that this feature works for scripts that are imported **in the same migration session**.

## CDI extension ##

Runs Flyway migrations on startup; multiple migrations are supported.

You have to create producers for each migration configuration and optionally use the qualifier @FlywayMigration for each of them.

The CSVMigrationResolver is registered automatically if found on classpath.

Dependencies:
- __ascelion.flywaydb:flywaydb-ext-cdi__ (for both CDI-1 and CDI-2)
- __org.flywaydb:flyway-core:5.+__ or __ascelion.flywaydb:flywaydb-ext-util5__ for version 5
- __org.flywaydb:flyway-core:6.+__ or __ascelion.flywaydb:flywaydb-ext-util5__ for version 6
