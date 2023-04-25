package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.LineItem

import java.time.Instant

object Replies {
  private type Context = Option[Map[String, String]]

  final case class Summary(
                            customerId: String,
                            regionId: String,
                            salesChannelId: String,
                            countryCode: String,
                            lineItems: Map[String, LineItem],
                            context: Context,
                            checkoutDate: Option[Instant]
                          ) extends CborSerializable

}
