package userapi

import cats.effect.IO
import cats.syntax.all.*
import doobie.*
import doobie.implicits.*
import doobie.postgres.*

def validateEmail(email: String): Either[ApiError, String] = {
  if (email.trim.isEmpty) Left(InvalidEmail(email))
  else if (!email.contains("@")) Left(InvalidEmail(email))
  else Right(email)
}

class UserService(xa: Transactor[IO]):

  def getById(id: Int): IO[Either[ApiError, User]] =
    UserRepository.findById(id).transact(xa).map(opt => opt.toRight(UserNotFound(id)))

  def create(n: NewUser): IO[Either[ApiError, User]] =
    validateEmail(n.email) match
      case Left(err) => IO.pure(Left(err))
      case Right(_) =>
        UserRepository.insert(n)
          .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION => DuplicateUser(n.email) }
          .transact(xa)
          .map(_.map(id => User(id, n.name, n.email, List())))

  def delete(id: Int): IO[Either[ApiError, Unit]] =
    UserRepository.delete(id).transact(xa).map(rows =>
      if (rows == 0) Left(UserNotFound(id))
      else Right(())
    )

  def update(id: Int, n: NewUser): IO[Either[ApiError, User]] =
    validateEmail(n.email) match
      case Left(err) => IO.pure(Left(err))
      case Right(_) =>
        UserRepository.update(id, n)
          .attemptSomeSqlState { case sqlstate.class23.UNIQUE_VIOLATION => DuplicateUser(n.email) }
          .transact(xa)
          .flatMap {
            case Left(dup) => IO.pure[Either[ApiError, User]](Left(dup))
            case Right(0)  => IO.pure[Either[ApiError, User]](Left(UserNotFound(id)))
            case Right(_)  => getById(id)
          }

  def addRelative(userId: Int, relativeId: Int): IO[Either[ApiError, Unit]] =
    UserRepository.addRelative(userId, relativeId)
      .attemptSomeSqlState {
        case sqlstate.class23.FOREIGN_KEY_VIOLATION => Option[ApiError](UserNotFound(relativeId))
        case sqlstate.class23.UNIQUE_VIOLATION      => Option.empty[ApiError]
      }
      .transact(xa)
      .map {
        case Left(Some(err)) => Left(err)
        case Left(None)      => Right(())
        case Right(_)        => Right(())
      }

  def removeRelative(userId: Int, relativeId: Int): IO[Either[ApiError, Unit]] =
    UserRepository.removeRelative(userId, relativeId).transact(xa).map(_ => Right(()))    