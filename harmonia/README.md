# Harmonia

This project is based on the [Implementing Microservices with Akka](Implementing Microservices with Akka) tutorial by Lightbend and [Akka gRPC Shopping Cart Example](https://github.com/akka/akka-projection/tree/main/samples/grpc/shopping-cart-service-scala) with the following modifications:

- Upgraded to scala 2.13.10
- Upgraded library dependencies and plugins to the latest versions
- Uses Red Panda instead of Apache Kafka for message broker
- Defines a separate database schema for Akka Persistence tables
- Project structure inspired by Clean Architecture pattern

## Running Integration Tests
### Prerequisites
1. Ensure that latest version Exchange Library is published locally (See Publish Exchange Library to "Local" Ivy Repository section below).
2. Clean and compile services via sbt
   ```
   sbt harmonia-cart/clean
   sbt harmonia-cart/compile
   ```
3. Use sbt IntegrationTest command to run tests
   ```
   sbt harmonia-cart/IntegrationTest/test
   ```

## Running the sample code

### Database Setup

From the akka directory, run the following command:
```shell
docker-compose up -d
```
Create the PostgresSQL tables from the SQL script located inside the ddl-scripts at the root of the project:
```shell
./harmonia-setup.sh
```
**Note**
Ensure you have the correct permissions granted to run setup script.

### Publish Exchange Library to "Local" Ivy Repository
Execute the following command to compile and publish exchange module (contains all .proto files)
```shell
sbt exchange/publishLocal
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
1. Create Akka Projection for cart service to produce multi-event topic to Redpanda
2. Create ZIO App based on Medusa.js checkout processing using zio-temporal & Temporal
3. Create Region aggregate service modeled around Medusa.js region implementation
