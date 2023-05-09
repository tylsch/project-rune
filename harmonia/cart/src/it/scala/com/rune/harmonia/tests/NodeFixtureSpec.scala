package com.rune.harmonia.tests

import akka.actor.CoordinatedShutdown
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.testkit.SocketUtil
import com.dimafeng.testcontainers.DockerComposeContainer.ComposeFile
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import com.rune.harmonia.proto.HarmoniaCartServiceClient
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should._
import org.scalatest.time.Span
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.{Logger, LoggerFactory}
import org.testcontainers.containers.wait.strategy.Wait

import java.io.File
import scala.collection.immutable.Seq
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.sys.process._;

/*
* Link to IntegrationSpec since they have deleted this file
* https://github.com/akka/akka-projection/blob/6288ce6be10d8f23bb6546898687d91b7372336d/samples/grpc/shopping-cart-service-scala/src/test/scala/shopping/cart/IntegrationSpec.scala
* */
class TestNodeFixture(
                       grpcPort: Int,
                       managementPorts: Seq[Int],
                       managementPortIndex: Int,
                       configFile: String) {

  private val sharedConfig: Config = ConfigFactory.load(configFile)

  val testKit: ActorTestKit =
    ActorTestKit(
      "IntegrationSpec",
      nodeConfig(grpcPort, managementPorts, managementPortIndex)
        .withFallback(sharedConfig)
        .resolve())

  lazy val client: HarmoniaCartServiceClient =
    HarmoniaCartServiceClient(clientSettings)(testKit.system)
  CoordinatedShutdown
    .get(system)
    .addTask(
      CoordinatedShutdown.PhaseBeforeServiceUnbind,
      "close-test-client-for-grpc")(() => client.close());

  private val clientSettings =
    GrpcClientSettings
      .connectToServiceAt("127.0.0.1", grpcPort)(testKit.system)
      .withTls(false)

  def system: ActorSystem[_] = testKit.system

  private def nodeConfig(
                          grpcPort: Int,
                          managementPorts: Seq[Int],
                          managementPortIndex: Int): Config = {

    def buildEndpoints(managementPorts: Seq[Int]): String = {
      managementPorts.foldLeft("")((cfg, port) => s"$cfg,{host = \"127.0.0.1\", port = $port}").tail
    }

    ConfigFactory.parseString(
      s"""
        harmonia-cart-service.grpc {
          interface = "localhost"
          port = $grpcPort
        }
        akka.management.http.port = ${managementPorts(managementPortIndex)}
        akka.discovery.config.services {
          "harmonia-cart-service" {
            endpoints = [
              ${buildEndpoints(managementPorts)}
            ]
          }
        }
        """)
  }

}

abstract class NodeFixtureSpec(numOfNodes: Int, configFile: String)
  extends AnyWordSpecLike
    with BeforeAndAfterAll
    with Matchers
    with ScalaFutures
    with Eventually
    with TestContainerForAll {

  val logger: Logger =
    LoggerFactory.getLogger(classOf[NodeFixtureSpec])

  implicit private val patience: PatienceConfig =
    PatienceConfig(10.seconds, Span(100, org.scalatest.time.Millis))

//  private val (grpcPorts, managementPorts) =
//    SocketUtil
//      .temporaryServerAddresses(numOfNodes * 2, "127.0.0.1")
//      .map(_.getPort)
//      .splitAt(numOfNodes)
//
//  val nodeFixtures: Seq[TestNodeFixture] =
//    (1 to numOfNodes)
//      .map(portIndex => new TestNodeFixture(grpcPorts(portIndex), managementPorts, portIndex, configFile))
//
//  val systems3: Seq[ActorSystem[Nothing]] = nodeFixtures.map(_.testKit.system)

  override val containerDef: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      ComposeFile(Left(new File("src/it/resources/docker-compose-it.yml"))),
      tailChildContainers = true,
      exposedServices = Seq(
        ExposedService("postgres-db", 5432, Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
      ),
      localCompose = true
    )

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    // Create DB tables (Execute harmonia-setup.sh command for integration tests)
    // TODO: Refactor Shell Script to pass in name of service running so integration tests can run.
    //  Will need confirm shell script can access test containers docker compose.
    withContainers { container =>
      logger.info(s"Container Name: ${container.getContainerByServiceName("postgres-db").get.getContainerInfo.getName}")
      logger.info(s"Postgres Url: ${container.getServiceHost("postgres-db", 5432)}:${container.getServicePort("postgres-db", 5432)}")
    }
    val result = "../harmonia-setup.sh" lazyLines_!;
    logger.info(result.mkString("\n"))
    // avoid concurrent creation of tables
    //    val timeout = 10.seconds
    //    Await.result(
    //      PersistenceInit.initializeDefaultPlugins(testNode1.system, timeout),
    //      timeout)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    //    testNode3.testKit.shutdownTestKit()
    //    testNode2.testKit.shutdownTestKit()
    //    // testNode1 must be the last to shutdown
    //    // because responsible to close ScalikeJdbc connections
    //    testNode1.testKit.shutdownTestKit()
  }
}
