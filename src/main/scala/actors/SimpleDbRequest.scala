package actors

import java.util.UUID

import akka.actor.{Actor, ActorLogging}
import kamon.trace.Tracer
import common.Implicits.repositoryCassyJavaDriver
import model.AST._
import model._
import org.joda.time.DateTime

class SimpleDbRequest extends Actor with ActorLogging {

  override def receive: Receive = {

    case RqDbUser ( login, password )  =>
      Tracer.withNewContext ( "RqDbUser comes", autoFinish = true ) {
        log.info ( "RqDbUser comes." )
        sender ! RsDbUser ( repositoryCassyJavaDriver.getDbUserAndUpdateToken ( login, password ) )
      }

    case RqDbUserToken ( token ) =>
      Tracer.withNewContext ( "RqDbUserToken comes", autoFinish = true ) {
        log.info ( "RqDbUserToken comes." )
        sender ! RsDbUserToken ( repositoryCassyJavaDriver.getDbUserToken ( token ) )
      }

    case RqDbUserUpsert ( login, password, permissions )  =>
      Tracer.withNewContext ( "RqDbUserUpsert comes", autoFinish = true ) {
        log.info ( "RqDbUserUpsert comes." )

        val dbUser = repositoryCassyJavaDriver
                     .getDbUser ( login ).map ( _.copy ( password = password, permissions = permissions ) )
                     .getOrElse ( DbUser ( login, password, permissions, UUID.randomUUID, DateTime.now ) )

        repositoryCassyJavaDriver.setDbUser (
          DbUser ( dbUser.login, dbUser.password, dbUser.permissions, dbUser.token, dbUser.tokenCreatedOn ) )
        
        repositoryCassyJavaDriver.setDbUserToken (
          DbUserToken ( dbUser.token, dbUser.login, dbUser.permissions, dbUser.tokenCreatedOn ) )

        sender ! RsDbUser ( repositoryCassyJavaDriver.getDbUser ( login ) )
      }

    case _ =>
      Tracer.withNewContext ( "Something comes", autoFinish = true ) {
        log.info ( "Something comes." )
      }
  }

}
