package com.rune.harmonia.domain.entities

import com.rune.harmonia.app.cart.Replies.Summary
import com.rune.harmonia.domain.CborSerializable

import java.time.Instant

object Cart {
  private type Context = Option[Map[String, String]]

  sealed trait State extends CborSerializable {
  }

  final case class OpenCart(
                             customerId: String,
                             regionId: String,
                             salesChannelId: String,
                             countryCode: String,
                             lineItems: Map[String, LineItem],
                             context: Context,
                             checkoutDate: Option[Instant]
                           ) extends State {

    def hasItem(variantId: String): Boolean =
      lineItems.contains(variantId)

    def updateItem(variantId: String, quantity: Int, metadata: Option[Map[String, String]]): State = {
      quantity match {
        case 0 => copy(lineItems = lineItems - variantId)
        case _ => copy(lineItems = lineItems + (variantId -> LineItem(quantity, metadata)))
      }
    }

    def removeItem(variantId: String): State =
      copy(lineItems = lineItems.removed(variantId))

    def isEmpty: Boolean =
      lineItems.isEmpty

    def toSummary: Summary =
      Summary(customerId, regionId, salesChannelId, countryCode, lineItems, context, checkoutDate)
  }

  final case class CheckedOutCart(
                                  customerId: String,
                                  regionId: String,
                                  salesChannelId: String,
                                  countryCode: String,
                                  lineItems: Map[String, LineItem],
                                  context: Context,
                                  checkoutDate: Instant) extends State {

    def toSummary: Summary =
      Summary(customerId, regionId, salesChannelId, countryCode, lineItems, context, Some(checkoutDate))
  }
}
