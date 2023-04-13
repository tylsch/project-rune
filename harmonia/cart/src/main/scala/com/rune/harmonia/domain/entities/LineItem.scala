package com.rune.harmonia.domain.entities

final case class LineItem(quantity: Int, metadata: Option[Map[String, String]]) extends ChildEntity {
  def update(quantity: Int, metadata: Option[Map[String, String]]): LineItem =
    copy(quantity = quantity, metadata = metadata)
}
