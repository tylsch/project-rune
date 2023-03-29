package com.rune.harmonia.app.cart

import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, Entity, EntityTypeKey}
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.EventSourcedBehavior


object CartEntity {
  import Commands._
  import Events._
  import com.rune.harmonia.domain.entities.Cart._

  val EntityKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Harmonia-Cart")

  def init(system: ActorSystem[_]): Unit = {
    ClusterSharding(system).init(Entity(EntityKey)(entityContext => CartEntity(entityContext.entityId)))
  }

  def apply(id: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, Option[State]](
      persistenceId = PersistenceId(EntityKey.name, id),
      emptyState = None,
      commandHandler = (state, cmd) => throw new NotImplementedError("TODO: process the command & return an Effect"),
      eventHandler = (state, evt) => throw new NotImplementedError("TODO: process the event return the next state")
    )
  }
}
