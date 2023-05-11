package com.rune.harmonia.tests

import akka.cluster.MemberStatus
import akka.cluster.typed.Cluster
import com.rune.harmonia.Main
import com.rune.harmonia.proto.{ContextPayload, CreateCartRequest, ItemMetadata, ItemMetadataPayload, LineItem}
import org.scalatest.concurrent.PatienceConfiguration

import scala.concurrent.duration.DurationInt
import scala.util.{Success, Failure}

class IntegrationSpec
  extends NodeFixtureSpec(Set(8101, 8102, 8103), Set(2551, 2552, 2553), Set(9101, 9102, 9103), "integration-test.conf") {

  "DockerComposeContainer" should {
    "retrieve non-0 port for any of services" in {
      withContainers { composedContainers =>
        assert(composedContainers.getServicePort("postgres-db", 5432) > 0)
      }
    }
  }

  "Harmonia Cart Service" should {
    "init and join cluster" in {
      withNodes { (nodeFixtures, systems3) =>
        nodeFixtures.foreach(node => Main.init(node.testKit.system))

        // let the nodes join and become Up
        eventually(PatienceConfiguration.Timeout(15.seconds)) {
          systems3.foreach { sys =>
            Cluster(sys).selfMember.status should ===(MemberStatus.Up)
            Cluster(sys).state.members.unsorted.map(_.status) should ===(Set(MemberStatus.Up))
          }
        }
      }
    }
    "create new cart" in {
      withNodes { (nodeFixtures, _) =>
        val testNode1 = nodeFixtures.head
        val response = testNode1.client.createCart(
          CreateCartRequest("cart-1", "C1", "R1", "SC-1", "US", Map("foo" -> 1),
            Some(ItemMetadataPayload(Map("foo" -> ItemMetadata(Map("K1" -> "V1"))))), Some(ContextPayload(Map("IP" -> "IP")))))
        response.onComplete {
          case Success(newCart) =>
            newCart.customerId shouldBe "C1"
            newCart.regionId shouldBe "R1"
            newCart.salesChannelId shouldBe "SC-1"
            newCart.countryCode shouldBe "US"
            newCart.items shouldBe Map("foo" -> LineItem(1, Some(ItemMetadata(Map("K1" -> "V1")))))
            newCart.context shouldBe Some(ContextPayload(Map("IP" -> "IP")))
            newCart.checkoutTimestamp shouldBe None
          case Failure(exception) =>
            fail(exception)
        }(testNode1.system.executionContext)
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