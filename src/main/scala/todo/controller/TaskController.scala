package todo.controller

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives
import org.slf4j.LoggerFactory
import spray.json._
import todo.model.Task
import todo.service.TaskService
import todo.view.TaskJsonProtocol._

import scala.util.{Failure, Success}

object TaskController extends Directives with DefaultJsonProtocol {
  private val logger = LoggerFactory.getLogger(this.getClass)

  val routes =
    path("task") {
      get {
        logger.info("GET all task")
        onComplete(TaskService.getAll()) {
          case Success(tasks) => complete(tasks)
          case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
        }
      } ~
        post {
          entity(as[Task]) { task =>
            logger.info(s"POST task $task")
            onComplete(TaskService.add(task)) {

              case Success(task) => complete(Created, task)

              case Failure(ex) => {
                logger.error(s"Unable to create task $task: $ex")
                complete(BadRequest, s"An error occurred: ${ex.getMessage}")
              }
            }
          }
        }
    } ~
      path("task" / JavaUUID) { taskId =>
        get {
          logger.info(s"GET task $taskId")

          onComplete(TaskService.get(taskId)) {
            case Success(Some(task)) => complete(task)
            case Success(None) => complete(NotFound, "Payment not found")
            case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")
          }
        } ~
          put {
            logger.info(s"UPDATE task $taskId")

            entity(as[Task]) { task =>
              onComplete(TaskService.update(taskId, task)) {
                case Success(Some(updatedPayment)) => complete(OK, updatedPayment)
                case Success(None) => complete(NotAcceptable, "Invalid payment")
                case Failure(ex) =>  complete(BadRequest, s"An error occurred: ${ex.getMessage}")
              }
            }
          } ~
          delete {
            logger.info(s"DELETE task $taskId")

            onComplete(TaskService.delete(taskId)) {
              case Success(ok) => complete(OK)
              case Failure(ex) => complete(BadRequest, s"An error occurred: ${ex.getMessage}")

            }
          }
      }
}