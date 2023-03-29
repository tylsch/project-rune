package com.rune.harmonia.app.cart

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply
import com.rune.harmonia.app.cart.Replies._
import com.rune.harmonia.domain.CborSerializable

object Commands {
  sealed trait Command extends CborSerializable
  final case class CreateCart(itemId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command
}
