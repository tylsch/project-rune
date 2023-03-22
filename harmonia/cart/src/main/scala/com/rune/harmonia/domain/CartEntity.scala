package com.rune.harmonia.domain

import akka.actor.typed.{ActorRef, Behavior}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import com.rune.harmonia.domain.CartEntity.CartState

import java.time.Instant

object CartEntity {

  // Commands
  sealed trait Command extends CborSerializable
  final case class AddItem(itemId: String, quantity: Int, replyTo: ActorRef[StatusReply[Summary]]) extends Command

  // Replies
  final case class Summary(items: Map[String, Int]) extends CborSerializable

  // Events
  sealed trait Event extends CborSerializable {
    def cartId: String
  }
  final case class ItemAdded(cartId: String, itemId: String, quantity: Int) extends Event

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[Event, CartState]

  // TODO: Refactor Commands, Events, and State into separate files
  abstract class CartState(items: Map[String, Int], checkoutDate: Option[Instant]) extends CborSerializable {
    def hasItem(itemId: String): Boolean =
      items.contains(itemId)
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

  def apply(id: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, Option[CartState]](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = None,
      commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }
}
