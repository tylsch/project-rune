package com.run.harmonia.domain

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.rune.harmonia.app.cart._
import com.rune.harmonia.domain.entities.{Cart, LineItem}
import com.rune.harmonia.domain.entities.Cart.{CheckedOutCart, OpenCart}
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

import java.time.Instant

object CartEntitySpec {
  val config: Config = ConfigFactory
    .parseString(
      """
        akka.actor.serialization-bindings {
          "com.rune.harmonia.domain.CborSerializable" = jackson-cbor
        }
        """)
    .withFallback(EventSourcedBehaviorTestKit.config)
}
class CartEntitySpec
  extends ScalaTestWithActorTestKit(CartEntitySpec.config)
    with AnyWordSpecLike
    with BeforeAndAfterEach {

  private val cartId = "testCart"
  private val eventSourcedTestKit =
    EventSourcedBehaviorTestKit[
      Commands.Command,
      Events.Event,
      Option[Cart.State]](system, CartEntity(cartId))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  "The cart during creation" should {
    "be created with a item" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map("foo" -> LineItem(42, None)), None, checkoutDate = None)
        )
      )

      result.event should ===(Events.CartCreated(cartId, "C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None))

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }

    "attach metadata to item and context to cart when included" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), Some(Map("foo" -> Map("K1" -> "V1"))), Some(Map("KC1" -> "VC1")), replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map("foo" -> LineItem(42, Some(Map("K1" -> "V1")))), Some(Map("KC1" -> "VC1")), checkoutDate = None)
        )
      )

      result.event should ===(Events.CartCreated(cartId, "C1", "R1", "SC-1", "US", Map("foo" -> 42), Some(Map("foo" -> Map("K1" -> "V1"))), Some(Map("KC1" -> "VC1"))))

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe Some(Map("KC1" -> "VC1"))
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, Some(Map("K1" -> "V1"))))
    }

    "reply with an error if customer ID is empty" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("", "R1", "SC-1", "US", Map("foo" -> 1), None, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("customerId must be set for cart")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
    }

    "reply with an error if region is empty" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("C1", "", "SC-1", "US", Map("foo" -> 1), None, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("regionId must be set for cart")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
    }

    "reply with an error if region is sales channel is empty" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("C1", "RC-1", "", "US", Map("foo" -> 1), None, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("salesChannelId must be set for cart")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
    }

    "reply with an error if country code is empty" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("C1", "RC-1", "SC-1", "", Map("foo" -> 1), None, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("countryCode must be set for cart")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
    }

    "reply with an error if quantity is less than or equal to zero" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 0), None, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Item was found with zero quantity, all items must have a quantity greater than zero")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
    }

    "reply with an error if another command besides CreateCart is used" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("foo", 0, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Command not supported in current state")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
    }
  }

  "An open cart" should {
    "be able to add an item with a quantity greater than zero" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("bar", 35, Some(Map("K1" -> "V1")), replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map("foo" -> LineItem(42, None), "bar" -> LineItem(35, Some(Map("K1" -> "V1")))), None, checkoutDate = None)
        )
      )

      result.event should ===(Events.LineItemAdded(cartId, "bar", 35, Some(Map("K1" -> "V1"))))

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None), "bar" -> LineItem(35, Some(Map("K1" -> "V1"))))
    }
    "reply with error when adding item to cart that already exists" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("foo", 1, Some(Map("K2" -> "V2")), replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Item \"foo\" was already added to this shopping cart")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }
    "reply with error when adding item to cart with quantity less than or equal to zero" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("bar", 0, Some(Map("K2" -> "V2")), replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Quantity must be greater than zero")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }
    "be able to update an item with a quantity greater than zero" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), Some(Map("foo" -> Map("K1" -> "V1"))), None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.UpdateLineItem("foo", 22, Some(Map("K2" -> "V2")), replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map("foo" -> LineItem(22, Some(Map("K2" -> "V2")))), None, checkoutDate = None)
        )
      )

      result.event should ===(Events.LineItemUpdated(cartId, "foo", 22, Some(Map("K2" -> "V2"))))

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(22, Some(Map("K2" -> "V2"))))
    }
    "reply with error when updating item to cart that doesn't exists" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.UpdateLineItem("barr", 1, Some(Map("K2" -> "V2")), replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Item \"barr\" does not exist in the cart")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }
    "reply with error when updating an item to cart with quantity less than or equal to zero" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.UpdateLineItem("foo", 0, Some(Map("K2" -> "V2")), replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Quantity must be greater than zero")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }
    "remove item from cart" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.RemoveLineItem("foo", replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map.empty, None, checkoutDate = None)
        )
      )

      result.event should ===(Events.LineItemRemoved(cartId, "foo"))

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map.empty
    }
    "reply with error when attempting to remove an item that doesn't exist" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.RemoveLineItem("bar", replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Item \"bar\" does not exist in the cart")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }
    "check out cart" in {
      val checkoutDateTime = Instant.now()

      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CheckoutCart(checkoutDateTime, replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map("foo" -> LineItem(42, None)), None, Some(checkoutDateTime))
        )
      )

      result.event should ===(Events.CheckedOut(cartId, checkoutDateTime))

      result.stateOfType[Option[CheckedOutCart]].isDefined shouldBe true
      result.stateOfType[Option[CheckedOutCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[CheckedOutCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[CheckedOutCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[CheckedOutCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[CheckedOutCart]].get.checkoutDate shouldBe checkoutDateTime
      result.stateOfType[Option[CheckedOutCart]].get.context shouldBe None
      result.stateOfType[Option[CheckedOutCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }
    "reply with error when attempting check out an empty cart" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.RemoveLineItem("foo", _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CheckoutCart(Instant.now(), replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Cannot checkout an empty shopping cart")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[OpenCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[OpenCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[OpenCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.context shouldBe None
      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map.empty
    }
    "retrieve cart details from an open cart" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.Get(replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map("foo" -> LineItem(42, None)), None, checkoutDate = None)
        )
      )

      result.hasNoEvents shouldBe true
    }
  }

  "A checked out cart" should {
    "retrieve cart details from a checked out cart" in {
      val checkoutDateTime = Instant.now()

      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CheckoutCart(checkoutDateTime, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.Get(replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary("C1", "R1", "SC-1", "US", Map("foo" -> LineItem(42, None)), None, Some(checkoutDateTime))
        )
      )

      result.hasNoEvents shouldBe true
    }
    "reply with an error if a mutation command is sent" in {
      val checkoutDateTime = Instant.now()

      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("C1", "R1", "SC-1", "US", Map("foo" -> 42), None, None, _))
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CheckoutCart(checkoutDateTime, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.RemoveLineItem("foo", replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Cart is completed.  No longer accepting any new commands.")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[CheckedOutCart]].isDefined shouldBe true
      result.stateOfType[Option[CheckedOutCart]].get.customerId shouldBe "C1"
      result.stateOfType[Option[CheckedOutCart]].get.regionId shouldBe "R1"
      result.stateOfType[Option[CheckedOutCart]].get.salesChannelId shouldBe "SC-1"
      result.stateOfType[Option[CheckedOutCart]].get.countryCode shouldBe "US"
      result.stateOfType[Option[CheckedOutCart]].get.checkoutDate shouldBe checkoutDateTime
      result.stateOfType[Option[CheckedOutCart]].get.context shouldBe None
      result.stateOfType[Option[CheckedOutCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }
  }

}
