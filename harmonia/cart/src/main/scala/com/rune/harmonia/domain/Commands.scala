package com.rune.harmonia.domain

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply

object Commands {
  import Replies._
  sealed trait Command extends CborSerializable
  final case class AddItem(itemId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command
}
