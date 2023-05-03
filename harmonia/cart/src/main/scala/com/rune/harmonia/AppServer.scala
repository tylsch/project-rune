package com.rune.harmonia

import akka.actor.typed.ActorSystem
import akka.grpc.scaladsl.{ServerReflection, ServiceHandler}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import com.rune.harmonia.proto.{HarmoniaCartService, HarmoniaCartServiceHandler}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object AppServer {

  def start(interface: String, port: Int, system: ActorSystem[_], grpcService: HarmoniaCartService): Unit = {
    implicit val sys: ActorSystem[_] = system
    implicit val ec: ExecutionContext = system.executionContext

    val service: HttpRequest => Future[HttpResponse] =
      ServiceHandler.concatOrNotFound(HarmoniaCartServiceHandler.partial(grpcService),
        // ServerReflection enabled to support grpcurl without import-path and proto parameters
        ServerReflection.partial(List(HarmoniaCartService))
      )

    val bound = Http().newServerAt(interface, port).bind(service).map(_.addToCoordinatedShutdown(3.seconds))

    bound.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(
          "Harmonia Cart service online at gRPC server {}:{}",
          address.getHostString,
          address.getPort
        )
      case Failure(exception) =>
        system.log.error("Failed to bind gRPC endpoint, terminating system", exception)
        system.terminate()
    }
  }
}
