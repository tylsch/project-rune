package com.rune.harmonia.app.cart

import akka.actor.CoordinatedShutdown
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ShardedDaemonProcess
import akka.kafka.ProducerSettings
import akka.kafka.scaladsl.SendProducer
import akka.persistence.query.Offset
import akka.persistence.query.typed.EventEnvelope
import akka.persistence.r2dbc.query.scaladsl.R2dbcReadJournal
import akka.projection.{ProjectionBehavior, ProjectionId}
import akka.projection.eventsourced.scaladsl.EventSourcedProvider
import akka.projection.r2dbc.scaladsl.R2dbcProjection
import akka.projection.scaladsl.{AtLeastOnceProjection, SourceProvider}
import com.rune.harmonia.cart.proto.CartEvent
import io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig
import io.confluent.kafka.serializers.protobuf._
import org.apache.kafka.common.serialization._

import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt
import scala.jdk.CollectionConverters._
import scala.language.postfixOps

object KafkaEventProjection {
  def init(system: ActorSystem[_]): Unit = {
    implicit val sys: ActorSystem[_] = system
    implicit val ec: ExecutionContext = system.executionContext

    val topic = sys.settings.config.getString("harmonia-cart-service.kafka-topic")
    val schemaRegistryUrl = sys.settings.config.getString("harmonia-cart-service.schema-registry-url")
    val numberOfSliceRanges: Int = 4
    val sliceRanges = EventSourcedProvider.sliceRanges(system, R2dbcReadJournal.Identifier, numberOfSliceRanges)

    def sourceProvider(sliceRange: Range): SourceProvider[Offset, EventEnvelope[Events.Event]] =
      EventSourcedProvider
        .eventsBySlices[Events.Event](
          system,
          readJournalPluginId = R2dbcReadJournal.Identifier,
          CartEntity.EntityKey.name,
          sliceRange.min,
          sliceRange.max
        )

    def projection(sliceRange: Range): AtLeastOnceProjection[Offset, EventEnvelope[Events.Event]] = {
      val projectionId = ProjectionId("HarmoninaCarts", s"h-carts-${sliceRange.min}-${sliceRange.max}")

      R2dbcProjection
        .atLeastOnceAsync[Offset, EventEnvelope[Events.Event]](
          projectionId,
          settings = None,
          sourceProvider(sliceRange),
          handler = () => new KafkaProjectionHandler(system, topic, createProducer(system, schemaRegistryUrl))
        )
        .withSaveOffset(afterEnvelopes = 100, afterDuration = 500 millis)
    }

    ShardedDaemonProcess(system).init(
      name = "HarmoniaCartProjection",
      numberOfInstances = sliceRanges.size,
      behaviorFactory = i => ProjectionBehavior(projection(sliceRanges(i))),
      stopMessage = ProjectionBehavior.Stop
    )
  }

  private def createProducer(system: ActorSystem[_], schemaRegistryUrl: String): SendProducer[String, CartEvent] = {
    val producerSettings: ProducerSettings[String, CartEvent] = {
      val kafkaProtobufSerDeConfig = Map[String, Any](
        AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG -> schemaRegistryUrl
      )
      val kafkaProtobufSerializer = new KafkaProtobufSerializer()
      kafkaProtobufSerializer.configure(kafkaProtobufSerDeConfig.asJava, false)
      val valueSerializer = kafkaProtobufSerializer.asInstanceOf[Serializer[CartEvent]]

      ProducerSettings(system, new StringSerializer, valueSerializer)
    }

    val producer = SendProducer(producerSettings)(system)
    CoordinatedShutdown(system).addTask(
      CoordinatedShutdown.PhaseBeforeActorSystemTerminate, "close-producer"){ () =>
      producer.close()
    }

    producer
  }

}
