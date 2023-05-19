package com.rune.harmonia.app.cart

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.grpc.GrpcServiceException
import akka.pattern.StatusReply
import akka.util.Timeout
import com.rune.harmonia.proto.{AddItemRequest, Cart, CheckOutRequest, ContextPayload, CreateCartRequest, GetRequest, HarmoniaCartService, ItemMetadata, LineItem, RemoveItemRequest, UpdateItemRequest}
import io.grpc.Status
import org.slf4j.LoggerFactory

import java.time.Instant
import java.util.concurrent.TimeoutException
import scala.concurrent.Future

class CartServiceImpl(system: ActorSystem[_]) extends HarmoniaCartService {
  import system.executionContext

  private val logger = LoggerFactory.getLogger(getClass)
  private val sharding = ClusterSharding(system)

  implicit private val timeout: Timeout =
    Timeout.create(
      system.settings.config.getDuration("harmonia-cart-service.ask-timeout"))

  private def toProtoCart(cart: Replies.Summary): Cart = {
    val lineItems: Map[String, LineItem] = cart.lineItems.iterator.map {
      case (variantId, item) =>
        val lineItem: LineItem = item.metadata match {
          case None => LineItem(item.quantity, None)
          case Some(value) => LineItem(item.quantity, Some(ItemMetadata(value)))
        }

        variantId -> lineItem
    }.toMap

    val context: Option[ContextPayload] =
      cart.context match {
        case None => None
        case Some(payload) => Some(ContextPayload(payload))
      }

    val checkoutTimestamp: Option[Long] =
      cart.checkoutDate match {
        case None => None
        case Some(value) => Some(value.getEpochSecond)
      }

    Cart(cart.customerId, cart.regionId, cart.salesChannelId, cart.countryCode, lineItems, context, checkoutTimestamp)
  }

  private def convertError[T](response: Future[T], overrideCatchAllFuture: Option[Throwable => Future[T]]): Future[T] = {
    response.recoverWith {
      case _: TimeoutException =>
        Future.failed(
          new GrpcServiceException(
            Status.UNAVAILABLE.withDescription("Operation timed out")))
      case exc =>
        overrideCatchAllFuture match {
          case None =>
            Future.failed(
              new GrpcServiceException(
                Status.INVALID_ARGUMENT.withDescription(exc.getMessage)))
          case Some(func) =>
            func(exc)
        }
    }
  }

  private def convertError[T](response: Future[T]): Future[T] = {
    response.recoverWith {
      case _: TimeoutException =>
        Future.failed(
          new GrpcServiceException(
            Status.UNAVAILABLE.withDescription("Operation timed out")))
      case exc =>
        Future.failed(
          new GrpcServiceException(
            Status.INVALID_ARGUMENT.withDescription(exc.getMessage)))
    }
  }
  override def createCart(in: CreateCartRequest): Future[Cart] = {
    val itemMetadata: Option[Map[String, Map[String, String]]] = {
      in.itemMetadata match {
        case None => None
        case Some(payload) => Some(payload.itemsMetadata.map {
          case (variantId, metadataPayload) =>
            variantId -> metadataPayload.metadata
        })
      }
    }

    val context: Option[Map[String, String]] = {
      in.context match {
        case None => None
        case Some(payload) => Some(payload.context)
      }
    }

    val entityRef = sharding.entityRefFor(CartEntity.EntityKey, in.cartId)
    val reply = entityRef.askWithStatus(
      Commands.CreateCart(in.customerId, in.regionId, in.salesChannelId, in.countryCode, in.items, itemMetadata, context, _)
    )

    convertError(reply.map(cart => toProtoCart(cart)), None)
  }

  override def get(in: GetRequest): Future[Cart] = {
    val entityRef = sharding.entityRefFor(CartEntity.EntityKey, in.cartId)
    val reply = entityRef.askWithStatus(Commands.Get)

    reply
      .recoverWith {
        case _: TimeoutException =>
          Future.failed(
            new GrpcServiceException(
              Status.UNAVAILABLE.withDescription("Operation timed out")))
        case _: StatusReply.ErrorMessage =>
          Future.failed(
            new GrpcServiceException(
              Status.NOT_FOUND.withDescription(s"Cart ${in.cartId} not found")))
        case exc =>
          Future.failed(
            new GrpcServiceException(
              Status.INVALID_ARGUMENT.withDescription(exc.getMessage)))
      }
      .map(cart => toProtoCart(cart))
  }

  override def addItem(in: AddItemRequest): Future[Cart] = {
    val itemMetadata: Option[Map[String, String]] = {
      in.itemMetadata match {
        case None => None
        case Some(payload) => Some(payload.metadata)
      }
    }

    val entityRef = sharding.entityRefFor(CartEntity.EntityKey, in.cartId)
    val response = entityRef.askWithStatus(
      Commands.AddLineItem(in.variantId, in.quantity, itemMetadata, _)
    )

    convertError(response.map(cart => toProtoCart(cart)))
  }

  override def updateItem(in: UpdateItemRequest): Future[Cart] = {
    val itemMetadata: Option[Map[String, String]] = {
      in.itemMetadata match {
        case None => None
        case Some(payload) => Some(payload.metadata)
      }
    }

    val entityRef = sharding.entityRefFor(CartEntity.EntityKey, in.cartId)
    val response = entityRef.askWithStatus(
      Commands.UpdateLineItem(in.variantId, in.quantity, itemMetadata, _)
    )

    convertError(response.map(cart => toProtoCart(cart)))
  }

  override def removeItem(in: RemoveItemRequest): Future[Cart] = {
    val entityRef = sharding.entityRefFor(CartEntity.EntityKey, in.cartId)
    val response = entityRef.askWithStatus(
      Commands.RemoveLineItem(in.variantId, _)
    )

    convertError(response.map(cart => toProtoCart(cart)))
  }

  override def checkOut(in: CheckOutRequest): Future[Cart] = {
    val entityRef = sharding.entityRefFor(CartEntity.EntityKey, in.cartId)
    val response = entityRef.askWithStatus(
      Commands.CheckoutCart(Instant.ofEpochSecond(in.checkOutTimestamp), _)
    )

    convertError(response.map(cart => toProtoCart(cart)))
  }
}
