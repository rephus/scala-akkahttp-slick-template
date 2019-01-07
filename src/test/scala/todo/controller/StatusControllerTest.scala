package todo.controller


import org.specs2.mutable.Specification
import akka.http.scaladsl.testkit.Specs2RouteTest

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server._

class StatusControllerTest  extends Specification with Specs2RouteTest {

  val routes = StatusController.routes
  "The service" should {

    "return a greeting for GET requests to the root path" in {
      Get() ~> routes ~> check {
        responseAs[String] === """{"status":"ok","environment":"test","name":"todo"}"""
    }}

    "return a 'pong' response for GET requests to /ping" in {
      Get("/ping") ~> routes ~> check {
        responseAs[String] shouldEqual "pong"
      }
    }

    "leave GET requests to other paths unhandled" in {
      // tests:
      Get("/kermit") ~> routes ~> check {
        handled === false
      }
    }

    "return a MethodNotAllowed error for PUT requests to the root path" in {
      // tests:
      Put() ~> Route.seal(routes) ~> check {
        status === StatusCodes.MethodNotAllowed
        responseAs[String] shouldEqual "HTTP method not allowed, supported methods: GET"
      }
    }
  }
}