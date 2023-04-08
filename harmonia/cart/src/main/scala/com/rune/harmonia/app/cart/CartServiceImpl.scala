package com.rune.harmonia.app.cart

import com.harmonia.grpc.service.{AddLineItemRequest, Cart, HarmoniaCartService}

import scala.concurrent.Future

class CartServiceImpl extends HarmoniaCartService {
  override def addItem(in: AddLineItemRequest): Future[Cart] = ???
}
