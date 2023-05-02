package com.rune.harmonia

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.management.cluster.bootstrap.ClusterBootstrap
import akka.management.scaladsl.AkkaManagement
import com.rune.harmonia.app.cart.{CartEntity, CartServiceImpl}
import org.slf4j.LoggerFactory

import scala.util.control.NonFatal

object Main {
  val logger = LoggerFactory.getLogger("com.rune.harmonia.Main")

  def main(args: Array[String]): Unit = {
    val system = ActorSystem[Nothing](Behaviors.empty, "HarmoniaCartService")
    try {
      init(system)
    } catch {
      case NonFatal(e) =>
        logger.error("Terminating due to initialization failure.", e)
        system.terminate()
    }
  }

  def init(system: ActorSystem[_]): Unit = {
    AkkaManagement(system).start()
    ClusterBootstrap(system).start()

    CartEntity.init(system)

    val grpcInterface =
      system.settings.config.getString("harmonia-cart-service.grpc.interface")
    val grpcPort =
      system.settings.config.getInt("harmonia-cart-service.grpc.port")
    val grpcService = new CartServiceImpl(system)
    AppServer.start(
      grpcInterface,
      grpcPort,
      system,
      grpcService)
  }
}
