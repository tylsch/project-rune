package com.rune.harmonia.domain

import akka.actor.typed.Behavior
import akka.persistence.Persistence
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior

object CartEntity {
  sealed trait Command extends CborSerializable
  sealed trait Event extends CborSerializable {
    def id: String
  }

  type ReplyEffect = akka.persistence.typed.scaladsl.ReplyEffect[Event, State]

  sealed trait State extends CborSerializable {
    def applyCommand(cmd: Command): ReplyEffect
    def applyEvent(event: Event): State
  }

  case object EmptyCart extends State {
    override def applyCommand(cmd: Command): ReplyEffect = ???

    override def applyEvent(event: Event): State = ???
  }

  def apply(id: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, State](
      persistenceId = PersistenceId.ofUniqueId("abc"),
      emptyState = EmptyCart,
      commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }
}
