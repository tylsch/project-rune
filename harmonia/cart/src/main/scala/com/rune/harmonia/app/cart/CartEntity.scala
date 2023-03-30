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

  def apply(cartId: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, Option[State]](
      persistenceId = PersistenceId(EntityKey.name, cartId),
      emptyState = None,
      commandHandler = (state, cmd) => handleCommand(cartId, state, cmd),
      eventHandler = (state, evt) => handleEvent(state, evt)
    )
  }
}
