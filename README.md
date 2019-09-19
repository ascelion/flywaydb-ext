# flyway-ext

Flyway DB extensions, supporting both version 5 and 6.

Features:

- CSV migration support
- CDI extension

## CSV migration support ##

Allows to use CSV files to import data. The file name follows the same naming convention and must sit in the same location as the SQL scripts.

The name of the table to import into is taken from the description; the first segment of the description is the table name and can be followed by the actual description of the import.

You must register the class CSVMigrationResolver to the Flyway configuration.

Add the artefact __ascelion.public:flywaydb-ext-util[56]:\<LATEST VERSION\>__ as a dependency.

## CDI extension ##

Runs Flyway migrations on startup; multiple migrations are supported.

You have to create producers for each migration configuration and optionally use the qualifier @FlywayMigration for each of them.

Add the artefact __ascelion.public:flywaydb-ext-cdi:\<LATEST VERSION\>__ as a dependency.
