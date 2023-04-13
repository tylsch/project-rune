package com.rune.harmonia.domain.entities

final case class LineItem(quantity: Int, metadata: Option[Map[String, String]]) extends ChildEntity
