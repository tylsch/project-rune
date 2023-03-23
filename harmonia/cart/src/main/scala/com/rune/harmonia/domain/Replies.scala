package com.rune.harmonia.domain

object Replies {
  final case class Summary(items: Map[String, Int]) extends CborSerializable
}
