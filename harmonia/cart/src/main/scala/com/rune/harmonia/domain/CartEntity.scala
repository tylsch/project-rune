package com.rune.harmonia.domain

import akka.actor.typed.{ActorSystem, Behavior}
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.{Entity, EntityTypeKey}
import akka.pattern.StatusReply
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.{Effect, EventSourcedBehavior, ReplyEffect}
import com.rune.harmonia.domain.CartReplies.Summary

import java.time.Instant

object CartEntity {
  import CartCommands._
  import CartEvents._
  import CartState._

  val EntityKey: EntityTypeKey[Command] = EntityTypeKey[Command]("Harmonia-Cart")

  def init(system: ActorSystem[_]): Unit = {
    ClusterSharding(system).init(Entity(EntityKey)(entityContext => CartEntity(entityContext.entityId)))
  }


  def apply(id: String): Behavior[Command] = {
    EventSourcedBehavior[Command, Event, Option[State]](
      persistenceId = PersistenceId(EntityKey.name, id),
      emptyState = None,
      commandHandler = (state, cmd) => handleCommand(id, state, cmd),
      eventHandler = (state, evt) => handleEvent(state, evt)
    )
  }

  private def handleCommand(cartId: String, state: Option[State], command: Command): ReplyEffect[Event, Option[State]] = {
    state match {
      case None => handleInitCommands(cartId, command)
      case Some(cart) => cart.applyCommand(cartId, command)
    }
  }

  private def handleEvent(state: Option[State], event: Event): Option[State] = {
    state match {
      case None => Some(handleInitEvents(event))
      case Some(cart) => Some(cart.applyEvent(event))
    }
  }

  private def handleInitCommands(cartId: String, command: Command): ReplyEffect[Event, Option[State]] = {
    command match {
      case _ =>
        Effect.unhandled.thenNoReply()
    }
  }

  private def handleInitEvents(event: Event): State = {
    event match {
      case ItemAdded(cartId, itemId, quantity) => OpenCart(Map(itemId -> quantity), Some(Instant.now()))
    }
  }

}
