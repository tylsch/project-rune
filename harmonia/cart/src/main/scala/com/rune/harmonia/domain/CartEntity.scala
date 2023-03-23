package com.rune.harmonia.domain

import akka.actor.typed.Behavior
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior

object CartEntity {
  import Commands._
  import Events._
  import State._


  def apply(id: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, Option[CartState]](
      persistenceId = PersistenceId.ofUniqueId(id),
      emptyState = None,
      commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }
}
