package com.rune.harmonia.app.cart

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import com.rune.harmonia.app.cart.Events._
import com.rune.harmonia.app.cart.Replies._
import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.Cart._

object Commands {
  sealed trait Command extends CborSerializable
  final case class CreateCart(variantId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  def handleCommand(cartId: String, state: Option[State], cmd: Command): ReplyEffect[Event, Option[State]] = {
    state match {
      case None =>
        cmd match {
          case CreateCart(variantId, quantity, replyTo) =>
            if (quantity <= 0)
              Effect
                .reply(replyTo)(
                  StatusReply.Error("Quantity must be greater than zero")
                )
            else
              Effect
                .persist(CartCreated(cartId, variantId, quantity))
                .thenReply(replyTo) {
                  case openCart: Option[OpenCart] =>
                    StatusReply.Success(Summary(openCart.get.items, openCart.get.checkoutDate.isDefined))
                }
          case _ => Effect.unhandled.thenNoReply()
        }
    }
  }
}
