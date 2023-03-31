package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.Cart._

import java.time.Instant

object Events {
  sealed trait Event extends CborSerializable {
    def cartId: String
  }

  final case class CartCreated(cartId: String, variantId: String, quantity: Int) extends Event
  final case class LineItemAdded(cartId: String, variantId: String, quantity: Int) extends Event

  def handleEvent(state: Option[State], evt: Event): Option[State] = {
    state match {
      case None =>
        evt match {
          case CartCreated(_, variantId, quantity) => Some(OpenCart(Map(variantId -> quantity), None))
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
