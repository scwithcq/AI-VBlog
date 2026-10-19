FROM mysql:8.0
COPY VBlogbackend/complete_database_tables.sql /docker-entrypoint-initdb.d/01-schema.sql
