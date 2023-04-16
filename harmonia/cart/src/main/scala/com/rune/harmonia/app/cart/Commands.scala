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
  // TODO: Refactor CreateCart command to match Medusa Create a Cart: https://docs.medusajs.com/api/store#tag/Carts/operation/PostCart
  final case class CreateCart(variantId: String, quantity: Int, metadata: Option[Map[String, String]], replyTo: ActorRef[StatusReply[Summary]]) extends Command

  /** Command to create a cart within a given region and sales channel.
  *
   * Modifications from Medusa.js Create Cart API:
   *
   * 1. Enhanced to include metadata for line items.
   *
   * @param regionId The ID of the Region to create the Cart in.
   * @param salesChannelId The ID of the Sales channel to create the Cart in.
   * @param countryCode The 2 character ISO country code to create the Cart in.
   * @param items Map of variantId and quantity pairs to generate list items from.
   * @param itemsMetadata Optional map of key/value pairs for a given line item in the cart.
   * @param context Optional map of key/value pairs that provide context about the cart.
   * */
  final case class CreateCartV2(
                                 regionId: String,
                                 salesChannelId: String,
                                 countryCode: String,
                                 items: Map[String, Int],
                                 itemsMetadata: Option[Map[String, String]],
                                 context: Option[Map[String, String]],
                                 replyTo: ActorRef[StatusReply[Summary]]
                               ) extends Command
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
                StatusReply.Success(Summary(openCart.get.items, openCart.get.metadata, openCart.get.checkoutDate.isDefined))
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
                StatusReply.Success(Summary(openCart.get.items, None, openCart.get.checkoutDate.isDefined))
            }
    }
  }
}
