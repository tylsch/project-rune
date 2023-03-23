package com.rune.harmonia.domain

import java.time.Instant

object State {
  import Commands._
  import Events._

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[Event, CartState]
  abstract class CartState(items: Map[String, Int], checkoutDate: Option[Instant]) extends CborSerializable {
    def applyCommand(cmd: Command): ReplyEffect
    def applyEvent(event: Event): CartState
  }

  case class OpenCart(items: Map[String, Int], checkoutDate: Option[Instant]) extends CartState(items, checkoutDate) {
    require(checkoutDate.isEmpty)
    override def applyCommand(cmd: Command): ReplyEffect = ???
    override def applyEvent(event: Event): CartState = ???
  }

  case class CompletedCart(items: Map[String, Int], checkoutDate: Option[Instant]) extends CartState(items, checkoutDate) {
    require(checkoutDate.isDefined)
    override def applyCommand(cmd: Command): ReplyEffect = ???
    override def applyEvent(event: Event): CartState = ???
  }
}
