package eskimi.task

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import eskimi.task.BidMsg.In.Command

import scala.concurrent.ExecutionContext
import scala.io.StdIn

object Main {
  implicit val system: ActorSystem[Command] = ActorSystem(BiddingAgent.apply, "biddingAgent")
  implicit val executionContext: ExecutionContext = system.executionContext
  private val biddingAgent: ActorRef[Command] = system


  private def startHttpServer(routes: Route)(implicit system: ActorSystem[_]): Unit = {
    val httpServer = Http().newServerAt("localhost", 8080).bind(routes)

    println(s"Server started at http://localhost:8080/")
    StdIn.readLine()

    httpServer
      .flatMap(_.unbind())
      .onComplete(_ => system.terminate())
  }


  def main(args: Array[String]): Unit = {
    val routes = new Routes(biddingAgent)
    startHttpServer(routes.biddingRoutes)
  }
}
