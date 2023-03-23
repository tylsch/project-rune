package com.rune.harmonia.domain

object Events {
  sealed trait Event extends CborSerializable {
    def cartId: String
  }

  final case class ItemAdded(cartId: String, itemId: String, quantity: Int) extends Event
}
