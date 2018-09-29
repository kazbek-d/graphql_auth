package model

import java.util.UUID
import org.joda.time.DateTime


// CREATE KEYSPACE "file_io" WITH REPLICATION = { 'class' : 'SimpleStrategy' , 'replication_factor' :1 };

// CREATE TABLE file_io.db_user (login varchar, password varchar, permissions varchar, token_id uuid, token_created_on timestamp, PRIMARY KEY (login));
case class DbUser(login: String, password: String, permissions: String,
                  token: UUID, tokenCreatedOn: DateTime) extends Cassandraable {

  override def toString: String =
    s"DbUser: login:$login, password:$password, permissions:$permissions, token:$token, tokenCreatedOn: $tokenCreatedOn"

  override def values = Array [AnyRef](
    login,
    password,
    permissions,
    token,
    tokenCreatedOn.getMillis.asInstanceOf [AnyRef]
  )
}
object DbUser {
  val tableName = "db_user"
  val cols = Array("login", "password", "permissions", "token_id", "token_created_on")
}



// CREATE TABLE file_io.db_user_token (token_id uuid, login varchar, permissions varchar, token_created_on timestamp, PRIMARY KEY (token_id));
case class DbUserToken(token: UUID, login: String, permissions: String, tokenCreatedOn: DateTime)
  extends Cassandraable {

  override def toString: String =
    s"DbUserToken: token:$token, login:$login, permissions:$permissions, tokenCreatedOn: $tokenCreatedOn"

  override def values = Array [AnyRef](
    token,
    login,
    permissions,
    tokenCreatedOn.getMillis.asInstanceOf [AnyRef]
  )

}
object DbUserToken {
  val tableName = "db_user_token"
  val cols = Array("token_id", "login", "permissions", "token_created_on")
}