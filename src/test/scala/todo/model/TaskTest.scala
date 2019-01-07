package todo.model

import slick.lifted.TableQuery
import org.specs2.mutable.{After, Before, BeforeAfter, Specification}
import slick.jdbc.H2Profile.api._
import slick.jdbc.meta._
import todo.factory.TaskFactory

import scala.concurrent.duration.Duration
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global


class TaskTest extends Specification {

  val table = TableQuery[Tasks]

  trait Context extends After {
    val dbName = s"test${util.Random.nextInt}"
    val db = Database.forURL(s"jdbc:h2:mem:$dbName;MODE=PostgreSQL", driver = "org.h2.Driver", keepAliveConnection = true)
    def after: Any = db.close()

  }

  "Creating a test schema should work" >> new Context {
    val numberOfTables = for {
      _ <- db.run(table.schema.create)
      numberOfTables <- db.run(MTable.getTables).map(_.size)
    } yield numberOfTables
    Await.result(numberOfTables, Duration.Inf) mustEqual (1)
  }

  "Schema should match our specification" >> new Context {
    table.schema.create.statements.toList === List(
      """create table "task" (
        |"id" UUID PRIMARY KEY,
        |"title" VARCHAR NOT NULL,
        |"description" VARCHAR,
        |"completed" BOOLEAN NOT NULL,
        |"created" TIMESTAMP NOT NULL,
        |"due" TIMESTAMP)""".stripMargin.replaceAll("\n", "")
    )
  }

  "Inserting a task works" >> new Context {
    val task = TaskFactory.full

    val inserted = for {
      _ <- db.run(table.schema.create)
      inserted <- db.run(table += task)
    } yield inserted

    Await.result(inserted, Duration.Inf) === 1
  }

  "Querying account table works" >> new Context {
    val task = TaskFactory.full

    val resultsFuture = for {
      _ <- db.run(table.schema.create)
      _ <-  db.run(table += task)
      res <- db.run(table.result)
    } yield res

    val results = Await.result(resultsFuture, Duration.Inf)
    results.size === 1
    results.head.id === task.id
    results.head.title === task.title
    results.head === task
  }
}