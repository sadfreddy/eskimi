package eskimi.task

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import eskimi.task.BidMsg.In
import eskimi.task.BidMsg.In._
import eskimi.task.BidMsg.Out.BidResponse
import eskimi.task.JsonFormats._

import scala.concurrent.Future
import scala.concurrent.duration._

class Routes(biddingAgent: ActorRef[Command])(implicit val system: ActorSystem[_]) {
  implicit val timeout: Timeout = 5.seconds

  def createBidResponse(bidRequest: BidRequest): Future[Option[BidResponse]] = {
    biddingAgent.ask(BidCommand(bidRequest, _))
  }

  val biddingRoutes: Route = pathPrefix("bids"){
    post {
      entity(as[In.BidRequest]) { bidRequest =>
        onSuccess(createBidResponse(bidRequest)) {
          case Some(value) => complete(value)
          case None        => complete(StatusCodes.NoContent)
        }
      }
    }
  }
}
