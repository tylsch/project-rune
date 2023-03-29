package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable

object Events {
  sealed trait Event extends CborSerializable {
    def cartId: String
  }

  final case class CartCreated(cartId: String, itemId: String, quantity: Int) extends Event
}
