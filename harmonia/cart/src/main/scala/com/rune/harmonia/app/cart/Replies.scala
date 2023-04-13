package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable

object Replies {
  private type CartMetadata = Option[Map[String, Map[String, String]]]

  final case class Summary(items: Map[String, Int], metadata: CartMetadata, checkedOut: Boolean) extends CborSerializable
}
