package userapi

import cats.effect.*
import doobie.implicits.*

object Check extends IOApp.Simple:
  def run: IO[Unit] =
    val service = UserService(Db.xa)
    for
      a <- service.removeRelative(4, 5)
      b <- service.getById(4)
      c <- service.getById(5)
      _ <- IO.println(s"удаление: $a")
      _ <- IO.println(s"4: $b")
      _ <- IO.println(s"5: $c")
    yield ()