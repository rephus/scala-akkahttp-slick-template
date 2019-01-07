package todo

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import todo.controller._
import org.flywaydb.core.Flyway

import scala.io.StdIn


object WebServer {
  private val logger = LoggerFactory.getLogger(this.getClass)

  def main(args: Array[String]) {

    // DB setup
    val db = DatabaseConfig.db

    val conf = ConfigFactory.load()
    // Run database migrations
    val url = conf.getString("db.url")
    logger.info(s"Migrating database on url ${url}")
    val flyway = new Flyway()
    flyway.setDataSource(url, conf.getString("db.user"), conf.getString("db.password"))
    flyway.migrate()

    // Run webserver with akka-http
    implicit val system = ActorSystem("my-system")
    implicit val materializer = ActorMaterializer()
    implicit val executionContext = system.dispatcher

    val routes = TaskController.routes ~ StatusController.routes
    Http().bindAndHandle(routes, "0.0.0.0", 8080)

    println(s"Server online at http://localhost:8080/")
  }
}