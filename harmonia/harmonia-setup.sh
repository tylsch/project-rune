#!/bin/sh

# Create PostgreSQL Databases for applications
docker exec -i harmonia-postgres-db-1 psql -U harmonia-admin harmonia -t < sql-scripts/create-db.sql

# Create Table Definitions for PostgreSQL databases
docker exec -i harmonia-postgres-db-1 psql -U harmonia-admin harmonia_cart -t < sql-scripts/table-setup.sql