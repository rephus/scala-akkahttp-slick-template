package todo.factory

import java.sql.{Date, Timestamp}
import java.util.{Date, UUID}

import todo.model.Task

import scala.util.Random

object TaskFactory {
  def randomString = Random.alphanumeric.take(Random.nextInt(10)).mkString



  def simple = Task(id = Some(UUID.randomUUID()), title = randomString)
  def full =  Task(id = Some(UUID.randomUUID()), title = randomString,
    description = Some(randomString), completed = Random.nextBoolean(),
    created = new Timestamp(Random.nextInt()) , due = Some( new Timestamp(Random.nextInt())))
}