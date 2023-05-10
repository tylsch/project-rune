#!/bin/sh

while getopts s:d:u: option
do
  case "${option}"
    in
    s)serviceName=${OPTARG};;
    d)database=${OPTARG};;
    u)user=${OPTARG};;
  esac
done

if [ -z "$serviceName"]
then
  serviceName=harmonia-postgres-db-1
fi

if [ -z "$database"]
then
  database=harmonia_cart
fi

if [ -z "$user"]
then
  user=harmonia-admin
fi

echo "Service Name : $serviceName";

# Create PostgreSQL Databases for applications
docker exec -i $serviceName psql -U $user harmonia -t < sql-scripts/create-db.sql

# Create Table Definitions for PostgreSQL databases
docker exec -i $serviceName psql -U $user $database -t < sql-scripts/table-setup.sql