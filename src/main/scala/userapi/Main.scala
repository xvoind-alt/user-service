package userapi

import cats.effect.*
import com.comcast.ip4s.*
import org.http4s.ember.server.EmberServerBuilder

object Main extends IOApp.Simple:
  def run: IO[Unit] =
    val service = UserService(Db.xa)
    EmberServerBuilder.default[IO]
      .withHost(host"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(Routes(service).orNotFound)
      .build
      .useForever