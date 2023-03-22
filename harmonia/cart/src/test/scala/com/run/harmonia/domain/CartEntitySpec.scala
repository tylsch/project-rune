package com.run.harmonia.domain

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import akka.pattern.StatusReply
import akka.persistence.testkit.scaladsl.EventSourcedBehaviorTestKit
import com.rune.harmonia.domain.CartEntity
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
      CartEntity.Command,
      CartEntity.Event,
      CartEntity.CartState](system, CartEntity(cartId))

  override protected def beforeEach(): Unit = {
    super.beforeEach()
    eventSourcedTestKit.clear()
  }

  "The Cart" should {
    "add item" in {
      var result = eventSourcedTestKit.runCommand[StatusReply[CartEntity.Summary]](
        replyTo => CartEntity.AddItem("foo", 42, replyTo)
      )

      result.reply should ===(
        StatusReply.Success(
          CartEntity.Summary(Map("foo" -> 42))
        )
      )

      result.event should ===(CartEntity.ItemAdded(cartId, "foo", 42))
    }
  }

}
