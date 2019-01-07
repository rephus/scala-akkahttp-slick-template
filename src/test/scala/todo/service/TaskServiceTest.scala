package todo.service

import org.specs2.mutable.{After, Specification}
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta._
import slick.lifted.TableQuery
import todo.DatabaseConfig
import todo.factory.TaskFactory
import todo.model.{Task, Tasks}

import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration


class TaskServiceTest extends Specification {

  lazy val db = DatabaseConfig.db

  val table = TableQuery[Tasks]
  val query = db.run(table.schema.create)

  Await.result(query, Duration.Inf)

  "Task service" should {
    "Inserting a task" in {
      val task = TaskFactory.full
      val insertedTask = Await.result( TaskService.add(task), Duration.Inf)
      insertedTask.title === task.title
      insertedTask.id !== task.id
      insertedTask.created === task.created
    }

    "Get a task" in {
      val task = TaskFactory.full
      val insertedTask = Await.result( TaskService.add(task), Duration.Inf)
      val getTask =  Await.result( TaskService.get(insertedTask.id.get), Duration.Inf).get
      insertedTask.title === getTask.title
      insertedTask.id === getTask.id
      insertedTask.created === getTask.created
    }

    "Get all" in {
      val getAll =  Await.result( TaskService.getAll(), Duration.Inf)
      getAll.size must be_>=(1)
    }
  }
}