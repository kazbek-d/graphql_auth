package model

import java.util.UUID


object AST {

  trait Requests

  trait DbRequests extends Requests
  case class RqDbUser(login: String, password: String) extends DbRequests
  case class RqDbUserUpsert(login: String, password: String, permissions: String) extends DbRequests
  case class RqDbUserToken(token: UUID) extends DbRequests



  
  trait Responces

  trait Err extends Responces
  case object Ok extends Responces
  case object UnhandledDbTask extends Err
  case class AnyErr(message: String) extends Err

  trait DBResponces extends Responces
  case class RsDbUser(dbUser: Option[DbUser]) extends DBResponces
  case class RsDbUserToken(dbUserToken: Option[DbUserToken]) extends DBResponces
}