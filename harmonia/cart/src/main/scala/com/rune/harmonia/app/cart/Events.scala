package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.Cart._
import com.rune.harmonia.domain.entities.LineItem

import java.time.Instant

object Events {

  sealed trait Event extends CborSerializable {
    def cartId: String
  }

  final case class CartCreated(cartId: String, variantId: String, quantity: Int, metadata: Option[Map[String, String]]) extends Event
  final case class LineItemAdded(cartId: String, variantId: String, quantity: Int) extends Event

  def handleEvent(state: Option[State], evt: Event): Option[State] = {
    state match {
      case None =>
        evt match {
          case CartCreated(_, variantId, quantity, None) => Some(OpenCart(Map(variantId -> LineItem(quantity, None)), None, None))
          case CartCreated(_, variantId, quantity, Some(metadata)) => Some(OpenCart(Map(variantId -> LineItem(quantity, None)), Some(metadata), None))
          case _ => throw new IllegalStateException(s"Invalid event [$evt] in state [NonExistingCart]")
        }

      case Some(openCart: OpenCart) =>
        evt match {
          case LineItemAdded(_, variantId, quantity) => Some(openCart.updateItem(variantId, quantity))
          case _ => throw new IllegalStateException(s"Invalid event [$evt] in state [OpenCart]")
        }
    }
  }
}
