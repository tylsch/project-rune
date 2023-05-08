package com.rune.harmonia.tests

import akka.actor.CoordinatedShutdown
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.grpc.GrpcClientSettings
import com.dimafeng.testcontainers.DockerComposeContainer.ComposeFile
import com.dimafeng.testcontainers.scalatest.TestContainerForAll
import com.dimafeng.testcontainers.{DockerComposeContainer, ExposedService}
import com.rune.harmonia.proto.HarmoniaCartServiceClient
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should._
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.LoggerFactory
import org.testcontainers.containers.wait.strategy.Wait

import sys.process._
import java.io.File
import scala.language.postfixOps;

// TODO: Build out IntegrationSpec with Test Containers
//  in same fashion as https://github.com/akka/akka-projection/blob/6288ce6be10d8f23bb6546898687d91b7372336d/samples/grpc/shopping-cart-service-scala/src/test/scala/shopping/cart/IntegrationSpec.scala

object IntegrationSpec {
  val sharedConfig: Config = ConfigFactory.load("integration-test.conf")

  private def nodeConfig(
                          grpcPort: Int,
                          managementPorts: Seq[Int],
                          managementPortIndex: Int): Config =
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
              {host = "127.0.0.1", port = ${managementPorts(0)}},
              {host = "127.0.0.1", port = ${managementPorts(1)}},
              {host = "127.0.0.1", port = ${managementPorts(2)}}
            ]
          }
        }
        """)

  class TestNodeFixture(
                         grpcPort: Int,
                         managementPorts: Seq[Int],
                         managementPortIndex: Int) {
    val testKit =
      ActorTestKit(
        "IntegrationSpec",
        nodeConfig(grpcPort, managementPorts, managementPortIndex)
          .withFallback(sharedConfig)
          .resolve())

    def system: ActorSystem[_] = testKit.system

    private val clientSettings =
      GrpcClientSettings
        .connectToServiceAt("127.0.0.1", grpcPort)(testKit.system)
        .withTls(false)
    lazy val client: HarmoniaCartServiceClient =
      HarmoniaCartServiceClient(clientSettings)(testKit.system)
    CoordinatedShutdown
      .get(system)
      .addTask(
        CoordinatedShutdown.PhaseBeforeServiceUnbind,
        "close-test-client-for-grpc")(() => client.close());

  }
}
class IntegrationSpec
  extends AnyWordSpecLike
  with BeforeAndAfterAll
  with Matchers
  with ScalaFutures
  with Eventually
  with TestContainerForAll {

  private val logger =
    LoggerFactory.getLogger(classOf[IntegrationSpec])

  // TODO: Refactor nodes into a base class spec as a Seq[TestNodeFixture] property that can be set during the beforeAll.

//  implicit private val patience: PatienceConfig =
//    PatienceConfig(10.seconds, Span(100, org.scalatest.time.Millis))
//
//  private val (grpcPorts, managementPorts) =
//    SocketUtil
//      .temporaryServerAddresses(6, "127.0.0.1")
//      .map(_.getPort)
//      .splitAt(3)

  // one TestKit (ActorSystem) per cluster node
//  private val testNode1 =
//    new TestNodeFixture(grpcPorts(0), managementPorts, 0)
//  private val testNode2 =
//    new TestNodeFixture(grpcPorts(1), managementPorts, 1)
//  private val testNode3 =
//    new TestNodeFixture(grpcPorts(2), managementPorts, 2)
//
//  private val systems3 =
//    List(testNode1, testNode2, testNode3).map(_.testKit.system)

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

  "DockerComposeContainer" should {
    "retrieve non-0 port for any of services" in {
      withContainers { composedContainers =>
        assert(composedContainers.getServicePort("postgres-db", 5432) > 0)
      }
    }
  }

//  "Harmonia Cart Service" should {
//    "init and join cluster" in {
//      Main.init(testNode1.testKit.system)
//      Main.init(testNode2.testKit.system)
//      Main.init(testNode3.testKit.system)
//
//      // let the nodes join and become Up
//      eventually(PatienceConfiguration.Timeout(15.seconds)) {
//        systems3.foreach { sys =>
//          Cluster(sys).selfMember.status should ===(MemberStatus.Up)
//          Cluster(sys).state.members.unsorted.map(_.status) should ===(Set(MemberStatus.Up))
//        }
//      }
//    }
//  }

  "run integration test" should {
    "succeed without issue" in {
      println("Running tests in integration test scope")
      succeed
    }
  }
}