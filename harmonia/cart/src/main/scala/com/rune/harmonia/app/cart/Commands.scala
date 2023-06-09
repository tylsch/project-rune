package com.rune.harmonia.app.cart

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}
import com.rune.harmonia.app.cart.Events._
import com.rune.harmonia.app.cart.Replies._
import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.Cart._

import java.time.Instant

object Commands {
  sealed trait Command extends CborSerializable {
    def replyTo: ActorRef[StatusReply[Summary]]
  }

  /** Command to create a cart within a given region and sales channel.
  *
   * Modifications from Medusa.js Create Cart API:
   *
   * 1. Enhanced to include metadata for line items.
   *
   * @param customerId The ID of the customer who placed the order
   * @param regionId The ID of the Region to create the Cart in.
   * @param salesChannelId The ID of the Sales channel to create the Cart in.
   * @param countryCode The 2 character ISO country code to create the Cart in.
   * @param items Map of variantId and quantity pairs to generate list items from.
   * @param itemsMetadata Optional map of key/value pairs for a given line item in the cart.
   * @param context Optional map of key/value pairs that provide context about the cart.
   * @param replyTo ActorRef to send reply back to
   * */
  final case class CreateCart(customerId: String,
                              regionId: String,
                              salesChannelId: String,
                              countryCode: String,
                              items: Map[String, Int],
                              itemsMetadata: Option[Map[String, Map[String, String]]],
                              context: Option[Map[String, String]],
                              replyTo: ActorRef[StatusReply[Summary]]
                               ) extends Command

  /** Command to add line item to cart.
   *
   * Modifications from Medusa.js Create Cart API:
   *
   * 1. Enhanced to include metadata for line items.
   *
   * @param variantId The id of the Product Variant to generate the Line Item from.
   * @param quantity The quantity of the Product Variant to add to the Line Item.
   * @param metadata Optional map of key/value pairs for the given line item in the cart.
   * @param replyTo ActorRef to send reply back to
   * */
  final case class AddLineItem(variantId: String, quantity: Int, metadata: Option[Map[String, String]], replyTo: ActorRef[StatusReply[Summary]]) extends Command

  final case class UpdateLineItem(variantId: String, quantity: Int, metadata: Option[Map[String, String]], replyTo: ActorRef[StatusReply[Summary]]) extends Command

  final case class RemoveLineItem(variantId: String, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  final case class CheckoutCart(checkoutDate: Instant, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  final case class Get(replyTo: ActorRef[StatusReply[Summary]]) extends Command

  def handleCommand(cartId: String, state: Option[State], cmd: Command): ReplyEffect[Event, Option[State]] = {
    state match {
      case None => handleInitialCommand(cartId, cmd)
      case Some(openCart: OpenCart) => handleOpenCartCommand(cartId, openCart, cmd)
      case Some(checkedOutCart: CheckedOutCart) => handleCompletedCartCommand(checkedOutCart, cmd)
    }
  }

  private def unSupportedCommandReply(replyTo: ActorRef[StatusReply[Summary]]): ReplyEffect[Event, Option[State]] =
    errorReply("Command not supported in current state", replyTo)

  private def errorReply(msg: String, replyTo: ActorRef[StatusReply[Summary]]): ReplyEffect[Event, Option[State]] =
    Effect.reply(replyTo)(StatusReply.Error(msg))

  private def handleInitialCommand(cartId: String, cmd: Command): ReplyEffect[Event, Option[State]] = {
    cmd match {
      case CreateCart(customerId, regionId, salesChannelId, countryCode, items, itemsMetadata, context, replyTo) =>
        if (customerId.isEmpty)
          errorReply("customerId must be set for cart", replyTo)
        else if (regionId.isEmpty)
          errorReply("regionId must be set for cart", replyTo)
        else if (salesChannelId.isEmpty)
          errorReply("salesChannelId must be set for cart", replyTo)
        else if (countryCode.isEmpty)
          errorReply("countryCode must be set for cart", replyTo)
        else if (items.exists { case (_, quantity) => quantity <= 0 })
          errorReply("Item was found with zero quantity, all items must have a quantity greater than zero", replyTo)
        else
          Effect
            .persist(CartCreated(cartId, customerId, regionId, salesChannelId, countryCode, items, itemsMetadata, context))
            .thenReply(replyTo) {
              case Some(openCart: OpenCart) =>
                StatusReply.Success(openCart.toSummary)

            }
      case _ => unSupportedCommandReply(cmd.replyTo)
    }
  }

  private def handleOpenCartCommand(cartId: String, state: OpenCart, cmd:Command): ReplyEffect[Event, Option[State]] = {
    cmd match {
      case CreateCart(_, _, _, _, _, _, _, replyTo) =>
        unSupportedCommandReply(replyTo)
      case Get(replyTo) =>
        Effect.reply(replyTo)(StatusReply.Success(state.toSummary))
      case AddLineItem(variantId, quantity, metadata, replyTo) =>
        if (state.hasItem(variantId))
          errorReply(s"Item \"$variantId\" was already added to this shopping cart", replyTo)
        else if (quantity <= 0)
          errorReply("Quantity must be greater than zero", replyTo)
        else
          Effect
            .persist(LineItemAdded(cartId, variantId, quantity, metadata))
            .thenReply(replyTo) {
              case Some(openCart: OpenCart) =>
                StatusReply.Success(openCart.toSummary)
            }
      case UpdateLineItem(variantId, quantity, metadata, replyTo) =>
        if (!state.hasItem(variantId))
          errorReply(s"Item \"$variantId\" does not exist in the cart", replyTo)
        else if (quantity <= 0)
          errorReply("Quantity must be greater than zero", replyTo)
        else
          Effect
            .persist(LineItemUpdated(cartId, variantId, quantity, metadata))
            .thenReply(replyTo) {
              case Some(openCart: OpenCart) =>
                StatusReply.Success(openCart.toSummary)
            }
      case RemoveLineItem(variantId, replyTo) =>
        if (!state.hasItem(variantId))
          errorReply(s"Item \"$variantId\" does not exist in the cart", replyTo)
        else
          Effect
            .persist(LineItemRemoved(cartId, variantId))
            .thenReply(replyTo) {
              case Some(openCart: OpenCart) =>
                StatusReply.Success(openCart.toSummary)
            }
      case CheckoutCart(checkoutDate, replyTo) =>
        if (state.isEmpty)
          errorReply("Cannot checkout an empty shopping cart", replyTo)
        else
          Effect.persist(CheckedOut(cartId, checkoutDate))
          .thenReply(replyTo) {
            case Some(checkedOutCart: CheckedOutCart) =>
              StatusReply.Success(checkedOutCart.toSummary)
          }
    }
  }

  private def handleCompletedCartCommand(state: CheckedOutCart, cmd: Command): ReplyEffect[Event, Option[State]] = {
    cmd match {
      case Get(replyTo) =>
        Effect.reply(replyTo)(StatusReply.Success(state.toSummary))
      case _ => errorReply("Cart is completed.  No longer accepting any new commands.", cmd.replyTo)
    }
  }
}
