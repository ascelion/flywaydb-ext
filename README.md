# Flyway DB extensions

Features:

- Supports versions 5.2 and 6.0 and 6.1
- CDI extension
- CSV migration support
- Spring Boot 2 support
- ... more to come

## General Usage ##

- Gradle

```
repositories {
	jcenter()
}

dependencies {
	implementation platform( 'ascelion.flywaydb:flyway-ext:1.0.3' )
}

```

- Maven

```
<dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>ascelion.flywaydb</groupId>
			<artifactId>flyway-ext</artifactId>
			<version>1.0.3</version>
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

### Dependencies ###

| Flyway Version | Extension Dependencies
| --- | ---
| 5.2.x | `ascelion.flywaydb:flyway-ext-util5:1.0.x`
|       | `org.flywaydb:flyway-core:5.2.x`
| 6.0.x | `ascelion.flywaydb:flyway-ext-util6:1.0.x`
|       | `org.flywaydb:flyway-core:6.0.x`
| 6.1.x | `ascelion.flywaydb:flyway-ext-util6:1.0.x`
|       | `org.flywaydb:flyway-core:6.1.x`

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

You have to create producers for each migration configuration and optionally use the qualifier @FlywayMigration for each producer.

The CSVMigrationResolver component is registered automatically if found on classpath.

Flyway directly instantiates Java based migrations that are found in the locations defined in the Flyway configuration. This extension allows to
create Java migrations as CDI components. The location of such component must not overlap with any of the locations provided by Flyway configuration.

Dependencies:
- __ascelion.flywaydb:flyway-ext-cdi__ (for both CDI-1 and CDI-2)


## Spring Boot 2 Support ##

The CSVMigrationResolver component is registered automatically.

Flyway directly instantiates Java based migrations that are found in the locations defined in the Flyway configuration. This extension allows to
create Java migrations as Spring components. The location of such component must not overlap with any of the locations provided by Flyway configuration.

Dependencies:
- __ascelion.flywaydb:flyway-ext-sb2__
