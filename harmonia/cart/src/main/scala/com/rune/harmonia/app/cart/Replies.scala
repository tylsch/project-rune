package com.rune.harmonia.app.cart

import com.rune.harmonia.domain.CborSerializable

object Replies {
  final case class Summary(items: Map[String, Int], checkedOut: Boolean) extends CborSerializable
}
