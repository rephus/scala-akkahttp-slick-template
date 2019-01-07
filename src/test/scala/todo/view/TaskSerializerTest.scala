package todo.view

import java.sql.Timestamp
import java.util.UUID

import org.specs2.mutable.Specification
import spray.json._
import todo.factory._
import todo.model.Task
import todo.view.TaskJsonProtocol._

class TaskSerializerTest extends Specification {

  // We don't serialize the miliseconds on the timestamp, therefore the === method on a serialized timestamp will fail
  // We use this method to remove the miliseconds and compare properly
  // It would be nice to make this method an implicit infix operator
  def compareTimestamp(ts1: Timestamp, ts2: Timestamp) = {
    ts1.toString.split('.').head === ts2.toString.split('.').head
  }

  "Task serializer" should {

    "should serialize datetime to JSON" in {
      val timestamp = new Timestamp(1546789943777l)
      val json = timestamp.toJson

      json.convertTo[String] === "2019-01-06T16:52:23Z"
      json.convertTo[Timestamp] === new Timestamp(1546789943000l) // without MS
    }

    "should serialize to JSON" in {

      val task = TaskFactory.full
      val json = task.toJson.asJsObject

      json.getFields("id").head.convertTo[UUID] === task.id.get
      json.getFields("title").head.convertTo[String] === task.title
      json.getFields("description").head.convertTo[String] === task.description.get
      json.getFields("completed").head.convertTo[Boolean] === task.completed
      compareTimestamp( json.getFields("created").head.convertTo[Timestamp],  task.created )
      compareTimestamp( json.getFields("due").head.convertTo[Timestamp],  task.due.get)
    }

    "should serialize to JSON optional fields" in {

      val task = TaskFactory.simple
      val json = task.toJson.asJsObject

      json.getFields("description") isEmpty
    }


    "should serialize and parse simple task from JSON" in {

      val task = TaskFactory.simple
      val json = task.toJson
      // We can't compare tasks directly because our timestamps don't contain miliseconds, therefore they fail
      val parsedTask = TaskJsonProtocol.taskFormat.read(json)

      parsedTask.id === task.id
      parsedTask.title === task.title
      parsedTask.description === None
      parsedTask.completed === task.completed
      compareTimestamp(parsedTask.created, task.created)
      parsedTask.due === None


    }
    "should serialize and parse full task from JSON" in {

      val task = TaskFactory.full
      val json = task.toJson
      val parsedTask = TaskJsonProtocol.taskFormat.read(json)

      parsedTask.id === task.id
      parsedTask.title === task.title
      parsedTask.description === task.description
      parsedTask.completed === task.completed
      compareTimestamp(parsedTask.created, task.created)
      compareTimestamp(parsedTask.due.get, task.due.get)
    }
  }


}
