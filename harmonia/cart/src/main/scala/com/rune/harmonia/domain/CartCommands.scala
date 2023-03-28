package com.rune.harmonia.domain

import akka.actor.typed.ActorRef
import akka.pattern.StatusReply

object CartCommands {
  import CartReplies._
  sealed trait Command extends CborSerializable
  // TODO: Add CreateCart command, model all commands similar to Medusa.js
  final case class AddItem(itemId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command
}
