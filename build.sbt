organization  := "javier.rengel"

version       := "1"

scalaVersion  := "2.12.8"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")
parallelExecution in Test := false

libraryDependencies ++= {
  val akkaV = "2.5.19"
  val restV = "10.1.7"
  Seq(
    "com.typesafe.akka" %% "akka-http"   % restV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.akka" %% "akka-http-testkit" % restV % Test,
    "com.typesafe.akka" %% "akka-http-spray-json" % restV,
    "com.typesafe.akka" %% "akka-testkit" % akkaV % Test,



  "org.flywaydb" % "flyway-core" % "4.2.0", // run migrations

    "com.typesafe.slick"  %%  "slick"         % "3.2.3",
    "postgresql"          % "postgresql"      % "9.1-901.jdbc4",

    "org.specs2"          %%  "specs2-core"   % "4.3.6" % Test,
    "com.h2database"      %   "h2"            % "1.3.175" % Test,

    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "org.slf4j" % "slf4j-api" % "1.7.25"

  )
}

Revolver.settings
