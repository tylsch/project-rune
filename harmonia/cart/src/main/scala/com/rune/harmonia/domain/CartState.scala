package com.rune.harmonia.domain

import akka.pattern.StatusReply
import akka.persistence.typed.scaladsl.{Effect, ReplyEffect}

import java.time.Instant

object CartState {
  import CartCommands._
  import CartEvents._
  import CartReplies._

  sealed trait State extends CborSerializable {
    def applyCommand(cartId: String, cmd: Command): ReplyEffect[Event, Option[State]]
    def applyEvent(event: Event): State
  }

  case class OpenCart(items: Map[String, Int], checkoutDate: Option[Instant]) extends State {
    require(checkoutDate.isEmpty)
    override def applyCommand(cartId: String, cmd: Command): ReplyEffect[Event, Option[State]] = ???
    override def applyEvent(event: Event): State = ???
  }

  case class CompletedCart(items: Map[String, Int], checkoutDate: Option[Instant]) extends State {
    require(checkoutDate.isDefined)
    override def applyCommand(cartId: String, cmd: Command): ReplyEffect[Event, Option[State]] = ???
    override def applyEvent(event: Event): State = ???
  }
}
