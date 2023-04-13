package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable
import com.rune.harmonia.domain.entities.LineItem

object Replies {
  private type CartMetadata = Option[Map[String, String]]

  final case class Summary(items: Map[String, LineItem], metadata: CartMetadata, checkedOut: Boolean) extends CborSerializable
}
