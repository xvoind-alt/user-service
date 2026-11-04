package userapi

import cats.effect.IO
import doobie.Transactor

object Db:
  val xa: Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",
    sys.env.getOrElse("DB_URL", "jdbc:postgresql://localhost:5432/user_api"),
    sys.env.getOrElse("DB_USER", "postgres"),
    sys.env("DB_PASSWORD"),
    None
  )