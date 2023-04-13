package com.run.harmonia.domain

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.rune.harmonia.app.cart._
import com.rune.harmonia.domain.entities.Cart
import com.rune.harmonia.domain.entities.Cart.OpenCart
import com.typesafe.config.ConfigFactory
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

object CartEntitySpec {
  val config = ConfigFactory
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
        replyTo => Commands.CreateCart("foo", 42, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary(Map("foo" -> 42), false)
        )
      )

      result.event should ===(Events.CartCreated(cartId, "foo", 42, None))

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.items shouldBe Map("foo" -> 42)
    }

    // TODO: Create test for CreateCart command with metadata for line item

    "reply with an error if quantity is less than or equal to zero" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("foo", 0, None, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Quantity must be greater than zero")
      )

      result.hasNoEvents shouldBe true
      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
    }

    "reply with an error if another command besides CreateCart is used" in {
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("foo", 0, replyTo)
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
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("foo", 42, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("bar", 35, replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary(Map("foo" -> 42, "bar" -> 35), false)
        )
      )

      result.event should ===(Events.LineItemAdded(cartId, "bar", 35))

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.items shouldBe Map("foo" -> 42, "bar" -> 35)
    }

    "reply with error when adding item to cart that already exists" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("foo", 42, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("foo", 0, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Item \"foo\" was already added to this shopping cart")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.items shouldBe Map("foo" -> 42)
    }

    "reply with error when adding item to cart with quantity less than or equal to zero" in {
      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("foo", 42, None, _))
      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.AddLineItem("bar", 0, replyTo)
      )

      result.reply should ===(
        StatusReply.Error("Quantity must be greater than zero")
      )

      result.hasNoEvents shouldBe true

      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
      result.stateOfType[Option[OpenCart]].get.items shouldBe Map("foo" -> 42)
    }

    // TODO: Enhance the AddLineItem process to include metadata updates
  }

}
