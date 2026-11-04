package userapi

import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto.*

final case class User(id: Int, name: String, email: String, relatives: List[Int])
final case class NewUser(name: String, email: String)

object User:
  given Encoder[User] = deriveEncoder
object NewUser:
  given Decoder[NewUser] = deriveDecoder

sealed trait ApiError:
  def message: String
final case class UserNotFound(id: Int) extends ApiError:
  def message = s"User $id not found"
final case class DuplicateUser(email: String) extends ApiError:
  def message = s"User with email $email already exists"
final case class InvalidEmail(email: String) extends ApiError:
  def message = s"Invalid email: $email"