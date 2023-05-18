package com.rune.harmonia.tests

import akka.cluster.MemberStatus
import akka.cluster.typed.Cluster
import akka.grpc.GrpcServiceException
import com.rune.harmonia.Main
import com.rune.harmonia.proto._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.Span

import scala.concurrent.duration._

/*
* Proposal to drop IntegrationTest from sbt 2.0
* https://eed3si9n.com/sbt-drop-custom-config/
*
* */

// TODO: Finish Integration Tests for other requests.

class IntegrationSpec
  extends NodeFixtureSpec(Set(8101, 8102, 8103), Set(2551, 2552, 2553), Set(9101, 9102, 9103), "integration-test.conf") {

  implicit private val patience: PatienceConfig =
    PatienceConfig(10.seconds, Span(100, org.scalatest.time.Millis))

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
    "throw exception for invalid parameters on cart creation" in {
      withNodes { (nodeFixtures, _) =>
        val testNode1 = nodeFixtures.head
        val response = testNode1.client.createCart(
          CreateCartRequest("cart-2", "", "R1", "SC-1", "US", Map("foo" -> 1),
            Some(ItemMetadataPayload(Map("foo" -> ItemMetadata(Map("K1" -> "V1"))))), Some(ContextPayload(Map("IP" -> "IP")))))

        response.failed.futureValue.isInstanceOf[GrpcServiceException]
        response.failed.futureValue.getMessage shouldBe "INVALID_ARGUMENT: customerId must be set for cart"
      }
    }

    "throw exception for retrieving cart that does not exist" in {
      withNodes { (nodeFixtures, _) =>
        val testNode1 = nodeFixtures.head
        val response = testNode1.client.get(GetRequest("cart-X"))

        response.failed.futureValue.isInstanceOf[GrpcServiceException]
        response.failed.futureValue.getMessage shouldBe "NOT_FOUND: Cart cart-X not found"
      }
    }

    "take cart through lifecycle" in {
      withNodes { (nodeFixtures, _) =>
        val testNode1 = nodeFixtures.head
        val testNode2 = nodeFixtures.tail.head
        val eventualCart = testNode1.client.createCart(
          CreateCartRequest("cart-1", "C1", "R1", "SC-1", "US", Map("foo" -> 1),
            Some(ItemMetadataPayload(Map("foo" -> ItemMetadata(Map("K1" -> "V1"))))), Some(ContextPayload(Map("IP" -> "IP")))))

        whenReady(eventualCart) { newCart =>
          newCart.customerId shouldBe "C1"
          newCart.regionId shouldBe "R1"
          newCart.salesChannelId shouldBe "SC-1"
          newCart.countryCode shouldBe "US"
          newCart.items shouldBe Map("foo" -> LineItem(1, Some(ItemMetadata(Map("K1" -> "V1")))))
          newCart.context shouldBe Some(ContextPayload(Map("IP" -> "IP")))
          newCart.checkOutTimestamp shouldBe None
        }

        val addItemResponse = testNode2.client.addItem(
          AddItemRequest("cart-1", "bar", 45, Some(ItemMetadata(Map("K2" -> "V2"))))
        )

        whenReady(addItemResponse) { updatedCart =>
          updatedCart.items shouldBe Map("foo" -> LineItem(1, Some(ItemMetadata(Map("K1" -> "V1")))), "bar" -> LineItem(45, Some(ItemMetadata(Map("K2" -> "V2")))))
        }
      }
    }
  }
}