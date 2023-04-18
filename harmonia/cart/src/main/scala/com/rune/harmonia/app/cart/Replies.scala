package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.LineItem

object Replies {
  private type Context = Option[Map[String, String]]

  final case class Summary(
                            regionId: String,
                            salesChannelId: String,
                            countryCode: String,
                            lineItems: Map[String, LineItem],
                            context: Context,
                            checkoutDate: Boolean
                          ) extends CborSerializable

}
