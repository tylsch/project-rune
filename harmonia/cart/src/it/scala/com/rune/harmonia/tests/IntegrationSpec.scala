package com.rune.harmonia.tests

import akka.actor.CoordinatedShutdown
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.actor.typed.ActorSystem
import akka.cluster.MemberStatus
import akka.cluster.typed.Cluster
import akka.grpc.GrpcClientSettings
import akka.persistence.testkit.scaladsl.PersistenceInit
import akka.testkit.SocketUtil
import com.rune.harmonia.Main
import com.rune.harmonia.proto.HarmoniaCartServiceClient
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.Eventually
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should._
import org.scalatest.time.Span
import org.scalatest.wordspec.AnyWordSpecLike
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

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
        shopping-cart-service.grpc {
          interface = "localhost"
          port = $grpcPort
        }
        akka.management.http.port = ${managementPorts(managementPortIndex)}
        akka.discovery.config.services {
          "shopping-cart-service" {
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
  with Eventually {

  import IntegrationSpec.TestNodeFixture

  private val logger =
    LoggerFactory.getLogger(classOf[IntegrationSpec])

  implicit private val patience: PatienceConfig =
    PatienceConfig(10.seconds, Span(100, org.scalatest.time.Millis))

  private val (grpcPorts, managementPorts) =
    SocketUtil
      .temporaryServerAddresses(6, "127.0.0.1")
      .map(_.getPort)
      .splitAt(3)

  // one TestKit (ActorSystem) per cluster node
  private val testNode1 =
    new TestNodeFixture(grpcPorts(0), managementPorts, 0)
  private val testNode2 =
    new TestNodeFixture(grpcPorts(1), managementPorts, 1)
  private val testNode3 =
    new TestNodeFixture(grpcPorts(2), managementPorts, 2)

  private val systems3 =
    List(testNode1, testNode2, testNode3).map(_.testKit.system)

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    // Test-Container setup
    // Create DB tables
    // avoid concurrent creation of tables
    val timeout = 10.seconds
    Await.result(
      PersistenceInit.initializeDefaultPlugins(testNode1.system, timeout),
      timeout)
  }

  override protected def afterAll(): Unit = {
    super.afterAll()
    testNode3.testKit.shutdownTestKit()
    testNode2.testKit.shutdownTestKit()
    // testNode1 must be the last to shutdown
    // because responsible to close ScalikeJdbc connections
    testNode1.testKit.shutdownTestKit()
  }

  // TODO: Integrate Test-Containers library to spin up PostgresSQL, need before integration test can be ran.

  "Harmonia Cart Service" should {
    "init and join cluster" in {
      Main.init(testNode1.testKit.system)
      Main.init(testNode2.testKit.system)
      Main.init(testNode3.testKit.system)

      // let the nodes join and become Up
      eventually(PatienceConfiguration.Timeout(15.seconds)) {
        systems3.foreach { sys =>
          Cluster(sys).selfMember.status should ===(MemberStatus.Up)
          Cluster(sys).state.members.unsorted.map(_.status) should ===(Set(MemberStatus.Up))
        }
      }
    }
  }

  "run integration test" should {
    "succeed without issue" in {
      println("Running tests in integration test scope")
      succeed
    }
  }
}