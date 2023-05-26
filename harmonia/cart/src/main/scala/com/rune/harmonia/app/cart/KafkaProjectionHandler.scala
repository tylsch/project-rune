package com.rune.harmonia.app.cart

import akka.Done
import akka.projection.eventsourced.EventEnvelope
import akka.projection.scaladsl.Handler

import scala.concurrent.Future

// TODO: Implement R2DBC projection handler to Kafka
class KafkaProjectionHandler extends Handler[EventEnvelope[Events.Event]] {
  override def process(envelope: EventEnvelope[Events.Event]): Future[Done] = ???
}
