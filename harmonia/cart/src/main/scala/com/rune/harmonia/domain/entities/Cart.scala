package com.rune.harmonia.domain.entities

import com.rune.harmonia.domain.CborSerializable

import java.time.Instant

object Cart {
  sealed trait State extends CborSerializable {
  }

  final case class OpenCart(items: Map[String, Int], checkoutDate: Option[Instant]) extends State {
    def updateItem(itemId: String, quantity: Int): State = {
      quantity match {
        case 0 => copy(items = items - itemId)
        case _ => copy(items = items + (itemId -> quantity))
      }
    }
  }

  final case class CompletedCart(items: Map[String, Int], checkoutDate: Option[Instant]) extends State {

  }
}
