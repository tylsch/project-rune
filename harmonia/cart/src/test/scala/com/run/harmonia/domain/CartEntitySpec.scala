package com.run.harmonia.domain

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.rune.harmonia.app.cart._
import com.rune.harmonia.domain.entities.{Cart, LineItem}
import com.rune.harmonia.domain.entities.Cart.OpenCart
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.wordspec.AnyWordSpecLike

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
      fail("Not Implemented")
      // TODO: Refactor for new Create commands, events, and replies
//      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
//        replyTo => Commands.CreateCart("foo", 42, None, replyTo)
//      )
//
//      result.reply should ===(
//        StatusReply.Success(
//          Replies.Summary(Map("foo" -> LineItem(42, None)), None, checkedOut = false)
//        )
//      )
//
//      result.event should ===(Events.CartCreated(cartId, "foo", 42, None))
//
//      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
//      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
//      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
    }

    "attach metadata to item when included" in {
      fail("Not Implemented")
      // TODO: Refactor for new Create commands, events, and replies
//      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
//        replyTo => Commands.CreateCart("foo", 42, Some(Map("K1" -> "V1")), replyTo)
//      )
//
//      result.reply should ===(
//        StatusReply.Success(
//          Replies.Summary(Map("foo" -> LineItem(42, None)), Some(Map("K1" -> "V1")), checkedOut = false)
//        )
//      )
//
//      result.event should ===(Events.CartCreated(cartId, "foo", 42, Some(Map("K1" -> "V1"))))
//
//      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
//      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
//      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None))
//      result.stateOfType[Option[OpenCart]].get.metadata shouldBe Some(Map("K1" -> "V1"))
    }

    // TODO: Add tests to error for missing region, sales channel, and country code

    "reply with an error if quantity is less than or equal to zero" in {
      fail("Not Implemented")
      // TODO: Refactor for new Create commands, events, and replies
//      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
//        replyTo => Commands.CreateCart("foo", 0, None, replyTo)
//      )
//
//      result.reply should ===(
//        StatusReply.Error("Quantity must be greater than zero")
//      )
//
//      result.hasNoEvents shouldBe true
//      result.stateOfType[Option[Cart.State]].isEmpty shouldBe true
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
      fail("Not Implemented")
      // TODO: Refactor for new Create and Add Line Item commands, events, and replies

//      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("foo", 42, None, _))
//      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
//        replyTo => Commands.AddLineItem("bar", 35, replyTo)
//      )
//
//      result.reply should ===(
//        StatusReply.Success(
//          Replies.Summary(Map("foo" -> LineItem(42, None), "bar" -> LineItem(35, None)), None, checkedOut = false)
//        )
//      )
//
//      result.event should ===(Events.LineItemAdded(cartId, "bar", 35))
//
//      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
//      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
//      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> LineItem(42, None), "bar" -> LineItem(35, None))
    }

    // TODO: Refactor, if item already exists then it should just increase the quantity that was passed in, not error.
    //  If supplied metadata will apply new key/value and overwrite value for existing keys
    "reply with error when adding item to cart that already exists" in {
      fail("Not Implemented")
      // TODO: Refactor for new Create and Add Line Item commands, events, and replies

//      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("foo", 42, None, _))
//      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
//        replyTo => Commands.AddLineItem("foo", 0, replyTo)
//      )
//
//      result.reply should ===(
//        StatusReply.Error("Item \"foo\" was already added to this shopping cart")
//      )
//
//      result.hasNoEvents shouldBe true
//
//      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
//      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
//      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> 42)
    }

    "reply with error when adding item to cart with quantity less than or equal to zero" in {
      fail("Not Implemented")
      // TODO: Refactor for new Create and Add Line Item commands, events, and replies

//      eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](Commands.CreateCart("foo", 42, None, _))
//      val result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
//        replyTo => Commands.AddLineItem("bar", 0, replyTo)
//      )
//
//      result.reply should ===(
//        StatusReply.Error("Quantity must be greater than zero")
//      )
//
//      result.hasNoEvents shouldBe true
//
//      result.stateOfType[Option[OpenCart]].isDefined shouldBe true
//      result.stateOfType[Option[OpenCart]].get.checkoutDate shouldBe None
//      result.stateOfType[Option[OpenCart]].get.lineItems shouldBe Map("foo" -> 42)
    }

    // TODO: Enhance the AddLineItem process to include metadata updates
  }

}
