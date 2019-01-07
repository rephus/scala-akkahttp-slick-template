package todo.model

import java.sql.Timestamp
import java.util.Date
import java.util.UUID
//import slick.driver.H2Driver.api._
//import slick.jdbc.PostgresProfile.api._
import todo.DatabaseConfig.profile.api._

case class Task(id: Option[UUID], title: String,
                description: Option[String] = None, completed: Boolean = false,
                created: Timestamp = new Timestamp(new Date().getTime), due: Option[Timestamp] = None)

class Tasks(tag: Tag) extends Table[Task](tag, "task") {

  def id = column[Option[UUID]]("id", O.SqlType("UUID"), O.PrimaryKey)

  def title = column[String]("title")
  def description = column[Option[String]]("description")
  def completed = column[Boolean]("completed")
  def created = column[Timestamp]("created")
  def due = column[Option[Timestamp]]("due")

  //How to use foreign keys: https://github.com/rephus/paytest/blob/master/src/main/scala/paytest/model/Payment.scala#L55
  //How to extend a class with more than 22 fields: https://github.com/rephus/paytest/blob/master/src/main/scala/paytest/model/Payment.scala#L64

  def * = (id, title, description, completed, created, due) <>(Task.tupled, Task.unapply)
}
