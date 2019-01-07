package todo.controller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.server.Directives
import com.typesafe.config.ConfigFactory
import spray.json._


object StatusController extends Directives with DefaultJsonProtocol {
  val conf = ConfigFactory.load()

  val routes =
    path("") {
      get {
        complete(Map(
          "status" -> "ok",
          "environment" -> conf.getString("env"),
          "name" -> conf.getString("service") )
        )
      }
    } ~ path("ping" ) {
      get {
        complete("pong")
      }

    }
}
