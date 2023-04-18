package com.rune.harmonia.domain.entities

import com.rune.harmonia.domain.CborSerializable

import java.time.Instant

object Cart {
  private type Context = Option[Map[String, String]]

  sealed trait State extends CborSerializable {
  }

  final case class OpenCart(
                             regionId: String,
                             salesChannelId: String,
                             countryCode: String,
                             lineItems: Map[String, LineItem],
                             context: Context,
                             checkoutDate: Option[Instant]
                           ) extends State {

    def hasItem(variantId: String): Boolean =
      lineItems.contains(variantId)

    def updateItem(variantId: String, quantity: Int): State = {
      quantity match {
        case 0 => copy(lineItems = lineItems - variantId)
        case _ => copy(lineItems = lineItems + (variantId -> LineItem(quantity, None)))
      }
    }

    def updateItem(variantId: String, quantity: Int, metadata: Option[Map[String, String]]): State = {
      quantity match {
        case 0 => copy(lineItems = lineItems - variantId)
        case _ => copy(lineItems = lineItems + (variantId -> LineItem(quantity, metadata)))
      }
    }
  }

  final case class CompletedCart(items: Map[String, Int], checkoutDate: Option[Instant]) extends State {

  }
}
