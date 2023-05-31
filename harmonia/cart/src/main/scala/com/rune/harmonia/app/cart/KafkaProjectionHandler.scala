package com.rune.harmonia.app.cart

import akka.Done
import akka.actor.typed.ActorSystem
import akka.kafka.scaladsl.SendProducer
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler
import com.rune.harmonia.cart.proto._
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

// TODO: Implement R2DBC projection handler to Kafka
class KafkaProjectionHandler(system: ActorSystem[_], topic: String, producer: SendProducer[String, CartEvent])
  extends Handler[EventEnvelope[Events.Event]] {

  private val log = LoggerFactory.getLogger(getClass)
  private implicit val ec: ExecutionContext = system.executionContext
  override def process(envelope: EventEnvelope[Events.Event]): Future[Done] = {
    val event = envelope.event
    val key = event.cartId
    val msg = new ProducerRecord[String, CartEvent](topic, key, toCartEvent(event))

    val result = producer.send(msg).map(_ => Done)
    result
  }

  private def toCartEvent(event: Events.Event): CartEvent = {
    event match {
      case Events.CartCreated(cartId, customerId, regionId, salesChannelId, countryCode, items, itemsMetadata, context) =>
        val itemsMetadataProto: Option[ItemMetadataPayload] = itemsMetadata match {
          case None => None
          case Some(content) =>
            val payload = content.iterator.map {
              case (variantId, metadata) =>
                variantId -> ItemMetadata(metadata)
            }.toMap

            Some(ItemMetadataPayload(payload))
        }

        val contextProto: Option[ContextPayload] = context match {
          case None => None
          case Some(context) => Some(ContextPayload(context))
        }

        CartEvent(cartId,
          CartEvent.Action.CartCreated(CartCreated(customerId, regionId, salesChannelId, countryCode, items, itemsMetadataProto, contextProto)))

      case Events.LineItemAdded(cartId, variantId, quantity, metadata) =>
        val metadataProto: Option[ItemMetadata] = metadata match {
          case None => None
          case Some(metadata) => Some(ItemMetadata(metadata))
        }

        CartEvent(cartId, CartEvent.Action.ItemAdded(ItemAdded(variantId, quantity, metadataProto)))
    }
  }
}
