package todo


import com.typesafe.config.ConfigFactory
import org.slf4j.LoggerFactory
import slick.jdbc.JdbcProfile
import slick.jdbc.JdbcBackend._
import slick.jdbc.{H2Profile, PostgresProfile}

object DatabaseConfig {
  private val logger = LoggerFactory.getLogger(this.getClass)

  var db = Database.forConfig("db")
  val conf = ConfigFactory.load()
  val driver = conf.getString("db.driver")
  logger.info("Loading DB with driver " + driver)
  lazy val profile: JdbcProfile = driver match {
    case "org.postgresql.Driver" => PostgresProfile
    case "org.h2.Driver" => H2Profile
  }
}