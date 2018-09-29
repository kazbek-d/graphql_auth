package graphql

import java.util.UUID

import akka.actor.ActorRef
import UserModel.QlUser
import akka.util.Timeout
import akka.pattern.ask
import graphql.CustomExceptions.AuthorisationException

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import model.AST._
import org.joda.time.DateTime

import scala.concurrent.Future


class UserRepo(actorRef: ActorRef) {

  val timeout10Minutes = 10.minute
  implicit val timeout: Timeout = timeout10Minutes


  /** Gives back a token or sessionId or anything else that identifies the user session  */
  def authenticate(userName: String, password: String): Future[Option[String]] =
    (actorRef ? RqDbUser ( userName, password )).map {
      case rsDbUser: RsDbUser => rsDbUser.dbUser.flatMap { dbUser =>
        if (dbUser.password == password)
          Some ( dbUser.token.toString )
        else None
      }
      case ex@_ =>
        println(ex)
        throw AuthorisationException ( "Authentication Exception" )
    }

  /** Gives `User` object with his/her permissions */
  def authorise(token: String): Future[Option[QlUser]] =  {
    try {
      (actorRef ? RqDbUserToken ( UUID.fromString ( token ) )).map {
        case rsDbUserToken: RsDbUserToken => rsDbUserToken.dbUserToken.flatMap { dbUserToken =>
          if (dbUserToken.tokenCreatedOn.plusDays(1).getMillis >= DateTime.now.getMillis)
            Some ( QlUser ( dbUserToken.login, dbUserToken.permissions.split ( ',' ).toList ) )
          else None
        }
        case _ => throw AuthorisationException ( "Invalid token" )
      }
    } catch {
      case _: Throwable => throw AuthorisationException ( "Invalid token" )
    }
  }

  /** Gives back a token or sessionId or anything else that identifies the user session  */
  def upsert(userName: String, password: String, permissions: List[String]): Future[Option[String]] =
    (actorRef ? RqDbUserUpsert ( userName, password, permissions.map(_.toUpperCase).mkString(","))).map {
      case rsDbUser: RsDbUser => rsDbUser.dbUser.map ( _.token.toString )
      case ex@_ => throw new CustomExceptions.CustomException ( ex.toString )
    }


}

