package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.Cart._
import com.rune.harmonia.domain.entities.LineItem

import java.time.Instant

object Events {

  sealed trait Event extends CborSerializable {
    def cartId: String
  }

  final case class CartCreated(
                                cartId: String,
                                customerId: String,
                                regionId: String,
                                salesChannelId: String,
                                countryCode: String,
                                items: Map[String, Int],
                                itemsMetadata: Option[Map[String, Map[String, String]]],
                                context: Option[Map[String, String]]
                                 ) extends Event
  final case class LineItemAdded(cartId: String, variantId: String, quantity: Int, metadata: Option[Map[String, String]]) extends Event
  final case class LineItemUpdated(cartId: String, variantId: String, quantity: Int, metadata: Option[Map[String, String]]) extends Event
  final case class LineItemRemoved(cartId: String, variantId: String) extends Event
  final case class CheckedOut(cartId: String, checkoutDate: Instant) extends Event

  def handleEvent(state: Option[State], evt: Event): Option[State] = {
    state match {
      case None =>
        evt match {
          case CartCreated(_, customerId, regionId, salesChannelId, countryCode, items, itemsMetadata, context) =>
            val lineItems = items.map {
              case (variantId, quantity) =>
                if (itemsMetadata.isEmpty)
                  (variantId, LineItem(quantity, None))
                else
                  (variantId, LineItem(quantity = quantity, metadata = itemsMetadata.get.get(variantId)))
            }

            Some(OpenCart(customerId, regionId, salesChannelId, countryCode, lineItems, context = context, checkoutDate = None))
          case _ => throw new IllegalStateException(s"Invalid event [$evt] in state [NonExistingCart]")
        }

      case Some(openCart: OpenCart) =>
        evt match {
          case LineItemAdded(_, variantId, quantity, metadata) => Some(openCart.updateItem(variantId, quantity, metadata))
          case LineItemUpdated(_, variantId, quantity, metadata) => Some(openCart.updateItem(variantId, quantity, metadata))
          case LineItemRemoved(_, variantId) => Some(openCart.removeItem(variantId))
          case CheckedOut(_, checkoutDate) =>
            Some(CheckedOutCart(openCart.customerId, openCart.regionId, openCart.salesChannelId, openCart.countryCode, openCart.lineItems, openCart.context, checkoutDate))
          case _ => throw new IllegalStateException(s"Invalid event [$evt] in state [OpenCart]")
        }

      case _ => throw new IllegalStateException(s"Invalid event [$evt] in state [CheckedOutCart]")
    }
  }
}
