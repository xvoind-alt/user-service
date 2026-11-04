package userapi

import cats.effect.IO
import io.circe.Json
import io.circe.syntax.*
import org.http4s.*
import org.http4s.circe.*
import org.http4s.circe.CirceEntityDecoder.*
import org.http4s.dsl.io.*

object Routes:

  def apply(service: UserService): HttpRoutes[IO] = HttpRoutes.of[IO] {

    case GET -> Root / "users" / IntVar(id) =>
      service.getById(id).flatMap {
        case Right(user) => Ok(user.asJson)
        case Left(err)   => errorResponse(err)
      }

    case req @ POST -> Root / "users" =>
      req.as[NewUser].flatMap { newUser =>
        service.create(newUser).flatMap {
          case Right(user) => Created(user.asJson)
          case Left(err)   => errorResponse(err)
        }
      }

    case DELETE -> Root / "users" / IntVar(id) =>
      service.delete(id).flatMap {
        case Right(_)  => NoContent()
        case Left(err) => errorResponse(err)
      }

    case req @ PUT -> Root / "users" / IntVar(id) =>
      req.as[NewUser].flatMap { newUser =>
        service.update(id, newUser).flatMap {
          case Right(user) => Ok(user.asJson)
          case Left(err)   => errorResponse(err)
        }
      }

    case PUT -> Root / "users" / IntVar(id) / "relatives" / IntVar(relativeId) =>
      service.addRelative(id, relativeId).flatMap {
        case Right(_)  => NoContent()
        case Left(err) => errorResponse(err)
      }

    case DELETE -> Root / "users" / IntVar(id) / "relatives" / IntVar(relativeId) =>
      service.removeRelative(id, relativeId).flatMap {
        case Right(_)  => NoContent()
        case Left(err) => errorResponse(err)
      }
  }

  private def errorResponse(err: ApiError): IO[Response[IO]] =
    val body = Json.obj("error" -> Json.fromString(err.message))
    err match
      case _: UserNotFound  => NotFound(body)
      case _: DuplicateUser => Conflict(body)
      case _: InvalidEmail  => BadRequest(body)