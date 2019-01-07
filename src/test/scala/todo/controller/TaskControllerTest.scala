package todo.controller

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import slick.jdbc.H2Profile.api._
import slick.lifted.TableQuery
import todo.DatabaseConfig
import todo.factory.TaskFactory
import todo.model.{Task, Tasks}
import todo.service.TaskService
import todo.view.TaskJsonProtocol._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class TaskControllerTest  extends Specification with Specs2RouteTest {

  lazy val db = DatabaseConfig.db
  try {
    Await.result(db.run(table.schema.create),Duration.Inf)
  } catch {
    case e: Exception => {}
  }

  val table = TableQuery[Tasks]
  val taskFuture = db.run{
    for {
     // _ <- table.schema.create
      _ <- table += TaskFactory.full
      res <- table.result
    } yield res
  }

  val task = Await.result(taskFuture, Duration.Inf).head

  val routes = TaskController.routes
  "The service" should {

    "leave GET requests to other paths unhandled" in {
      // tests:
      Get("/tasks") ~> routes ~> check {
        handled === false
      }
    }
    "return all tasks" in {
      Get("/task") ~> routes ~> check {
        responseAs[String] must contain( task.title)
        val responseTasks = responseAs[Seq[Task]]
        responseTasks.size must be_>=( 1)
        responseTasks.head.id === task.id
        responseTasks.head.title === task.title
        responseTasks.head.description === task.description
      }}

    "return task by id" in {
      Get(s"/task/${task.id.get}") ~> routes ~> check {
        val responseTask = responseAs[Task]
        responseTask.id === task.id
        responseTask.title === task.title
        responseTask.description === task.description
      }
    }
    "return 404 if task does not exist" in {
      Get(s"/task/${UUID.randomUUID().toString}") ~> routes  ~> check {
        status ===  NotFound
      }
    }

    "delete a task" in {

      val taskFuture = for {
        _ <- db.run(table += TaskFactory.full)
        res <- db.run(table.result)
      } yield res

      val task = Await.result(taskFuture, Duration.Inf).head

      Delete(s"/task/${task.id.get}") ~> routes ~> check {
        responseAs[String] must contain("OK")

        // Check that it doesn't exist after deletion
        val payment = db.run(table.filter(_.id === task.id.get).result.headOption)
        Await.result(payment, Duration.Inf) === None
      }
    }

    "create a task via POST" in {
      val task = TaskFactory.full
      Post("/task", task) ~> routes ~> check {
        status mustEqual StatusCodes.Created
        val responseTask = responseAs[Task]

        responseTask.title === task.title
        responseTask.description === task.description

        val payment = db.run(table.filter(_.id === responseTask.id).result.headOption)
        val insertedTask = Await.result(payment, Duration.Inf).head

        responseTask.id === insertedTask.id
        responseTask.title === insertedTask.title
        responseTask.description === insertedTask.description
      }
    }

    "update a payment via PUT" in {
      val task = Await.result(TaskService.add(TaskFactory.simple), Duration.Inf)

      val updatedTask = task.copy(title = "new title")

      Put(s"/task/${task.id.get}", updatedTask) ~> routes ~> check {
        status mustEqual StatusCodes.OK
        val responseTask = responseAs[Task]

        responseTask.id === task.id
        responseTask.title !== task.title
        responseTask.title === updatedTask.title
        responseTask.description === updatedTask.description

      }
    }

    "Updating an invalid payment id via PUT should return an error" in {
      Put(s"/task/${UUID.randomUUID().toString}", task) ~> routes ~> check {
        status mustEqual StatusCodes.NotAcceptable
      }
    }

  }
}
