package graphql

import common.Helpers.OptFuture_Revert
import graphql.CustomExceptions._
import graphql.UserModel.QlUser

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

case class SecureContext(token: Option[String], userRepo: UserRepo) {


  def getUserByToken: Future[Option[QlUser]] =
    OptFuture_Revert [Option[QlUser]]( token.map ( userRepo.authorise ) ).map ( _.flatten )


  def login(userName: String, password: String): Future[String] =
    userRepo.authenticate ( userName, password ).map {
      _.getOrElse ( throw AuthenticationException ( "UserName or password is incorrect" ) )
    }

  def set(userName: String, password: String, permission: Vector[String]): Future[String] =
    userRepo.upsert ( userName, password, permission.toList ).map {
      _.getOrElse ( throw AuthenticationException ( "UserName or password is incorrect" ) )
    }

  def authorised[T](permissions: String*)(fn: QlUser ⇒ T) =
    getUserByToken.map {
      _.fold ( throw AuthorisationException ( "Invalid token" ) ) { user ⇒
        if (permissions.forall ( user.permissions.contains ))
          fn ( user )
        else
          throw AuthorisationException ( "You do not have permission to do this operation" )
      }
    }


  def ensurePermissions(permissions: List[String]): Unit =
    getUserByToken.map {
      _.fold ( throw AuthorisationException ( "Invalid token" ) ) { user ⇒
        if (!permissions.forall ( user.permissions.contains ))
          throw AuthorisationException ( "You do not have permission to do this operation" )
      }
    }


  def user: Future[QlUser] = getUserByToken.map (
    _.fold ( throw AuthorisationException ( "Invalid token" ) ) { user ⇒ user } )

}