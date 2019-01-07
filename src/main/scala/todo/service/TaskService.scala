package todo.service

import java.util.UUID

import slick.lifted.TableQuery
import todo.DatabaseConfig
import todo.model.{Task, Tasks}

import scala.concurrent.Future
//import slick.jdbc.PostgresProfile.api._
//import slick.jdbc.H2Profile.api._
import todo.DatabaseConfig.profile.api._

import scala.concurrent.ExecutionContext.Implicits.global

object TaskService {

  var db = DatabaseConfig.db
  val table = TableQuery[Tasks]

  def delete(id: UUID) = db.run {
    table.filter(_.id === id).delete
  }
  def getAll(limit: Int = 10, from: Int = 0): Future[Seq[Task]] = db.run {
    table.drop(from).take(limit).result
  }

  def get(id: UUID): Future[Option[Task]] = db.run {

    table.filter(_.id === id).result.headOption
  }
  def update(id: UUID, task: Task) = db.run {

    for {
      _ <- table.filter(_.id === id).update(task.copy(id=Some(id)))
      res <- table.filter(_.id === id).result.headOption

    } yield res

  }

  def add(task: Task)= {
    val insertTask = task.copy(id=Some(UUID.randomUUID()) )
    db.run {
      for {
        _ <-  table += insertTask
        res <- table.filter(_.id === insertTask.id.get).result.head
      } yield res
    }
  }
}