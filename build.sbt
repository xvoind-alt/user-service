ThisBuild / scalaVersion := "3.3.3"
ThisBuild / version      := "0.1.0-SNAPSHOT"

val Http4sVersion  = "0.23.27"
val CirceVersion   = "0.14.6"
val DoobieVersion  = "1.0.0-RC5"
val LogbackVersion = "1.4.14"

lazy val root = (project in file("."))
  .settings(
    name := "user-service",
    libraryDependencies ++= Seq(
      // HTTP
      "org.http4s" %% "http4s-ember-server" % Http4sVersion,
      "org.http4s" %% "http4s-circe"        % Http4sVersion,
      "org.http4s" %% "http4s-dsl"          % Http4sVersion,

      // JSON
      "io.circe" %% "circe-core"    % CirceVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "io.circe" %% "circe-parser"  % CirceVersion,

      // База данных
      "org.tpolecat" %% "doobie-core"     % DoobieVersion,
      "org.tpolecat" %% "doobie-postgres" % DoobieVersion,

      // Логи
      "ch.qos.logback" % "logback-classic" % LogbackVersion,

      // Тесты
      "org.scalatest" %% "scalatest"                     % "3.2.18" % Test,
      "org.typelevel" %% "cats-effect-testing-scalatest" % "1.5.0"  % Test
    )
  )
