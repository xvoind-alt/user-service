package userapi


import cats.effect.IO
import cats.implicits.catsSyntaxApplicativeId
import doobie.*
import doobie.implicits.*
import doobie.postgres.sqlstate
import userapi.Db.xa

object UserRepository:

  def findUserRow(id: Int): ConnectionIO[Option[(Int, String, String)]] =
    sql"SELECT id, name, email FROM users WHERE id = $id"
      .query[(Int, String, String)]
      .option

  def relativeIds(userId: Int): ConnectionIO[List[Int]] =
    sql"SELECT relative_id FROM user_relatives WHERE user_id = $userId"
      .query[Int]
      .to[List]
      
  def findById(id: Int): ConnectionIO[Option[User]] =
    findUserRow(id).flatMap {
      case None => None.pure[ConnectionIO]
      case Some((uid, name, email)) => relativeIds(uid).map(ids => Some(User(uid, name, email, ids)))
    }

  def insert(n: NewUser): ConnectionIO[Int] =
    sql"INSERT INTO users (name, email) VALUES (${n.name}, ${n.email})"
      .update
      .withUniqueGeneratedKeys[Int]("id")

  def delete(id: Int): ConnectionIO[Int] =
    sql"DELETE FROM users WHERE id = $id"
      .update
      .run

  def update(id: Int, n: NewUser): ConnectionIO[Int] =
    sql"UPDATE users SET name = ${n.name}, email = ${n.email} WHERE id = $id"
      .update
      .run

  def addRelative(userId: Int, relativeId: Int): ConnectionIO[Int] =
    for
      a <- sql"INSERT INTO user_relatives (user_id, relative_id) VALUES ($userId, $relativeId) ON CONFLICT DO NOTHING".update.run
      b <- sql"INSERT INTO user_relatives (user_id, relative_id) VALUES ($relativeId, $userId) ON CONFLICT DO NOTHING".update.run
    yield a + b

  def removeRelative(userId: Int, relativeId: Int): ConnectionIO[Int] =
    sql"""DELETE FROM user_relatives
          WHERE (user_id = $userId AND relative_id = $relativeId)
             OR (user_id = $relativeId AND relative_id = $userId)"""
      .update
      .run