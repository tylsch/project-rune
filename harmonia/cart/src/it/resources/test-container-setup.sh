#!/bin/sh

while getopts s:d:u:p: option
do
  case "${option}"
    in
    s)serviceName=${OPTARG};;
    d)database=${OPTARG};;
    u)user=${OPTARG};;
    p)pw=${OPTARG};;
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

if [ -z "$pw"]
then
  pw=harmonia
fi

echo "Service Name : $serviceName";

# Create PostgreSQL Databases for applications
#docker exec -i harmonia-postgres-db-1 psql -U harmonia-admin harmonia -t < sql-scripts/create-db.sql

# Create Table Definitions for PostgreSQL databases
#docker exec -i harmonia-postgres-db-1 psql -U harmonia-admin harmonia_cart -t < sql-scripts/table-setup.sql