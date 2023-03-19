# Harmonia

This project is based on the [Implementing Microservices with Akka](Implementing Microservices with Akka) tutorial by Lightbend and [Akka gRPC Shopping Cart Example](https://github.com/akka/akka-projection/tree/main/samples/grpc/shopping-cart-service-scala) with the following modifications:

- Upgraded to scala 2.13.10
- Upgraded library dependencies and plugins to the latest versions
- Uses Red Panda instead of Apache Kafka for message broker
- Defines a separate database schema for Akka Persistence tables
- Project structure inspired by Clean Architecture pattern

## Running the sample code

### Database Setup

From the akka directory, run the following command:
```shell
docker-compose up -d
```
Create the PostgresSQL tables from the SQL script located inside the ddl-scripts at the root of the project:
```shell
docker exec -i {TBD} psql -U harmonia-admin harmonia -t < ddl-scripts/create_tables.sql
```

### Running Application(s)

1. Start a first node:

    ```
    sbt -Dconfig.resource=local1.conf {Application Name}/run
    ```

2. (Optional) Start another node with different ports:

    ```
    sbt -Dconfig.resource=local2.conf {Application Name}/run
    ```

3. Check for service readiness

    ```
    curl http://localhost:9101/ready
    ```

## TODO Items
1. Finish project setup for Cart Service
2. Implement Shopping Cart Tutorial
    2. Use Route DSL of Akka HTTP
    2. Replace ScalalikeJdbc with Slick for read-side repository
2. Implement Security Directives (OAuth2) (To be used with Krackend API Key or Keycloak)
3. Implement STTP APISpec/Tapir (Open API specification)
4. Implement Sangria (GraphQL)
5. Implement gRPC Project Handler (Akka gRPC Project Example)