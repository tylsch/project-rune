package com.rune.harmonia.domain

object CartReplies {
  final case class Summary(items: Map[String, Int], checkedOut: Boolean) extends CborSerializable
  final case class InvalidCommand(msg: String) extends CborSerializable
}
