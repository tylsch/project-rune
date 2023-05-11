package com.rune.harmonia.tests

import akka.actor.CoordinatedShutdown
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.grpc.GrpcClientSettings
import akka.persistence.testkit.scaladsl.PersistenceInit
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
import scala.concurrent.Await
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
                       databasePort: Int,
                       canonicalPort: Int,
                       configFile: String) {

  private val sharedConfig: Config = ConfigFactory.load(configFile)

  val testKit: ActorTestKit =
    ActorTestKit(
      "IntegrationSpec",
      nodeConfig(grpcPort, managementPorts, managementPortIndex, databasePort, canonicalPort)
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
                          managementPortIndex: Int,
                          databasePort: Int,
                          canonicalPort: Int): Config = {

    def buildEndpoints(managementPorts: Seq[Int]): String = {
      managementPorts.foldLeft("")((cfg, port) => s"$cfg,{host = \"127.0.0.1\", port = $port}").tail
    }

    ConfigFactory.parseString(
      s"""
        harmonia-cart-service.grpc {
          interface = "localhost"
          port = $grpcPort
        }
        akka.remote.artery.canonical {
          hostname = "127.0.0.1"
          port = $canonicalPort
        }
        akka.management.http.port = ${managementPorts(managementPortIndex)}
        akka.management.cluster.bootstrap.contact-point-discovery {
          required-contact-point-nr = ${managementPorts.length}
          contact-with-all-contact-points = true
        }
        akka.discovery.config.services {
          "harmonia-cart-service" {
            endpoints = [
              ${buildEndpoints(managementPorts)}
            ]
          }
        }
        akka.persistence.r2dbc.connection-factory {
          port = $databasePort
        }
        """)
  }

}

abstract class NodeFixtureSpec(grpcPorts: Set[Int], managementPorts: Set[Int], canonicalPorts: Set[Int], configFile: String)
  extends AnyWordSpecLike
    with BeforeAndAfterAll
    with Matchers
    with ScalaFutures
    with Eventually
    with TestContainerForAll {

  require(grpcPorts.nonEmpty && managementPorts.nonEmpty && canonicalPorts.nonEmpty)
  require(grpcPorts != managementPorts && grpcPorts != canonicalPorts && managementPorts != canonicalPorts)
  require(configFile.nonEmpty)

  private val grpcPortsSeq = grpcPorts.toSeq
  private val managementPortsSeq = managementPorts.toSeq
  private val canonicalPortsSeq = canonicalPorts.toSeq

  val logger: Logger =
    LoggerFactory.getLogger(classOf[NodeFixtureSpec])

  implicit private val patience: PatienceConfig =
    PatienceConfig(10.seconds, Span(100, org.scalatest.time.Millis))

//  private val (grpcPorts, managementPorts) =
//    SocketUtil
//      .temporaryServerAddresses(numOfNodes * 2, "127.0.0.1")
//      .map(_.getPort)
//      .splitAt(numOfNodes)

  private var nodeFixtures: Option[Seq[TestNodeFixture]] = None

  private var systems3: Option[Seq[ActorSystem[Nothing]]] = None

  override val containerDef: DockerComposeContainer.Def =
    DockerComposeContainer.Def(
      ComposeFile(Left(new File("src/it/resources/docker-compose-it.yml"))),
      tailChildContainers = true,
      exposedServices = Seq(
        ExposedService("postgres-db", 5432, Wait.forLogMessage(".*database system is ready to accept connections.*", 2))
      ),
      localCompose = true
    )

  def withNodes[A](runTest: (Seq[TestNodeFixture], Seq[ActorSystem[_]]) => A): A = {
    val nodes = nodeFixtures.getOrElse(throw new IllegalStateException())
    val system3 = systems3.getOrElse(throw new IllegalStateException())
    runTest(nodes, system3)
  }

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    // Create DB tables (Execute harmonia-setup.sh command for integration tests)
    withContainers { container =>
      val serviceName = container.getContainerByServiceName("postgres-db").get.getContainerInfo.getName
      val servicePort = container.getServicePort("postgres-db", 5432)
      val database = "harmonia_cart"
      val user = "harmonia-admin"

      val result = s"./cart-service-setup.sh -s $serviceName -d $database -u $user" lazyLines_!;
      logger.info(result.mkString("\n"))

      nodeFixtures = Some(grpcPortsSeq.indices
        .map(i => new TestNodeFixture(grpcPortsSeq(i), managementPortsSeq, i, servicePort, canonicalPortsSeq(i), configFile)))

      systems3 = Some(nodeFixtures.get.map(_.testKit.system))
    }

    // avoid concurrent creation of tables
    val timeout = 10.seconds
    Await.result(
      PersistenceInit.initializeDefaultPlugins(nodeFixtures.get.head.system, timeout),
      timeout)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    // shutdown nodes in reverse order to properly close connections to database
    nodeFixtures match {
      case None => logger.info("No nodes to shutdown")
      case Some(nodes) => nodes.reverse.foreach(node => node.testKit.shutdownTestKit())
    }
  }
}
