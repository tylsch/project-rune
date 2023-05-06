package com.rune.harmonia.tests

import org.scalatest.wordspec.AnyWordSpecLike

// TODO: Build out IntegrationSpec with Test Containers
//  in same fashion as https://github.com/akka/akka-projection/blob/6288ce6be10d8f23bb6546898687d91b7372336d/samples/grpc/shopping-cart-service-scala/src/test/scala/shopping/cart/IntegrationSpec.scala

class IntegrationSpec extends AnyWordSpecLike {
  "run integration test" should {
    "succeed without issue" in {
      println("Running tests in integration test scope")
      succeed
    }
  }
}