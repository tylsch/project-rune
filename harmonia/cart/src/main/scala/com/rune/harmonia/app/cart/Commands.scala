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
  final case class CreateCart(variantId: String, quantity: Int, metadata: Option[Map[String, String]], replyTo: ActorRef[StatusReply[Summary]]) extends Command
  final case class AddLineItem(variantId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  def handleCommand(cartId: String, state: Option[State], cmd: Command): ReplyEffect[Event, Option[State]] = {
    state match {
      case None => handleInitialCommand(cartId, cmd)
      case Some(openCart: OpenCart) => handleOpenCartCommand(cartId, openCart, cmd)
    }
  }

  private def unSupportedCommandReply(replyTo: ActorRef[StatusReply[Summary]]): ReplyEffect[Event, Option[State]] =
    Effect.reply(replyTo)(StatusReply.Error("Command not supported in current state"))

  private def handleInitialCommand(cartId: String, cmd: Command): ReplyEffect[Event, Option[State]] = {
    cmd match {
      case CreateCart(variantId, quantity, metadata, replyTo) =>
        if (quantity <= 0)
          Effect
            .reply(replyTo)(
              StatusReply.Error("Quantity must be greater than zero")
            )
        else
          Effect
            .persist(CartCreated(cartId, variantId, quantity, metadata))
            .thenReply(replyTo) {
              case openCart: Option[OpenCart] =>
                StatusReply.Success(Summary(openCart.get.items, openCart.get.checkoutDate.isDefined))
            }
      case AddLineItem(_, _, replyTo) =>
        unSupportedCommandReply(replyTo)

    }
  }

  private def handleOpenCartCommand(cartId: String, state: OpenCart, cmd:Command): ReplyEffect[Event, Option[State]] = {
    cmd match {
      case CreateCart(_, _, _, replyTo) =>
        unSupportedCommandReply(replyTo)
      case AddLineItem(variantId, quantity, replyTo) =>
        if (state.hasItem(variantId))
          Effect
            .reply(replyTo)(
              StatusReply.Error(s"Item \"$variantId\" was already added to this shopping cart")
            )
        else if (quantity <= 0)
          Effect
            .reply(replyTo)(
              StatusReply.Error("Quantity must be greater than zero")
            )
        else
          Effect
            .persist(LineItemAdded(cartId, variantId, quantity))
            .thenReply(replyTo) {
              case openCart: Option[OpenCart] =>
                StatusReply.Success(Summary(openCart.get.items, openCart.get.checkoutDate.isDefined))
            }
    }
  }
}
