package com.run.harmonia.domain

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.rune.harmonia.app.cart._
import com.rune.harmonia.domain.entities.Cart
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
      Cart.State](system, CartEntity(cartId))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  "The cart" should {
    "be created with a item" in {
      var result = eventSourcedTestKit.runCommand[StatusReply[Replies.Summary]](
        replyTo => Commands.CreateCart("foo", 42, replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          Replies.Summary(Map("foo" -> 42), false)
        )
      )

      result.event should ===(Events.CartCreated(cartId, "foo", 42))
    }
  }

}
