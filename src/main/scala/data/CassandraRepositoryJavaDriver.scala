package data

import java.lang.System.nanoTime
import java.util.UUID

import com.datastax.driver.core._
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.dse.auth.DsePlainTextAuthProvider
import com.google.common.cache.{CacheBuilder, CacheLoader, LoadingCache}
import common.Settings._
import kamon.trace.Tracer
import model._
import org.joda.time.{DateTime, DateTimeZone}

import scala.collection.JavaConverters._

class CassandraRepositoryJavaDriver {

  private def profile[R](code: => R, t: Long = nanoTime): (R, Long) = (code, nanoTime - t)

  val nrOfCacheEntries: Int = 100
  val poolingOptions = new PoolingOptions
  val cluster: Cluster = Cluster.builder ()
                         .withAuthProvider ( new DsePlainTextAuthProvider ( cassandraLogin, cassandraPassword ) )
                         .addContactPoints ( cassandraAddress: _* )
                         .withPort ( cassandraPort )
                         .withPoolingOptions ( poolingOptions )
                         .build ()
  val session: Session = cluster.newSession ()
  val cache: LoadingCache[String, PreparedStatement] =
    CacheBuilder.newBuilder ().
    maximumSize ( nrOfCacheEntries ).
    build (
      new CacheLoader[String, PreparedStatement]() {
        def load(key: String): PreparedStatement = session.prepare ( key.toString )
      }
    )


  object CassandraObject {

    def toDateTime(row: Row, col: String): Option[DateTime] =
      if (row.getObject ( col ) == null) None
      else Some ( new DateTime ( row.getTimestamp ( col ).getTime, DateTimeZone.getDefault ) )

    def toUUID(row: Row, col: String): Option[UUID] =
      if (row.getObject ( col ) == null) None
      else Some ( row.getUUID ( col ) )

    def toString(row: Row, col: String): Option[String] =
      if (row.getObject ( col ) == null) None
      else Some ( row.getString ( col ) )

    def toBigDecimal(row: Row, col: String): Option[BigDecimal] =
      if (row.getObject ( col ) == null) None
      else Some ( row.getDecimal ( col ) )


    def getDbUser(cache: LoadingCache[String, PreparedStatement], session: Session)(login: String): Option[DbUser] = {
      val query: Statement =
        QueryBuilder.select ().
        all ().
        from ( cassandraKeyspace, DbUser.tableName ).
        where ( QueryBuilder.eq ( "login", QueryBuilder.bindMarker () ) )

      session.execute ( cache.get ( query.toString ).bind ( login ) ).all ().asScala.map ( row => DbUser (
        row.getString ( "login" ),
        row.getString ( "password" ),
        row.getString ( "permissions" ),
        row.getUUID ( "token_id" ),
        toDateTime ( row, "token_created_on" ).get ) )
      .headOption
    }

    val getDbUser: (String) => Option[DbUser] = getDbUser ( cache, session )


    def getDbUserToken(cache: LoadingCache[String, PreparedStatement], session: Session)(token: UUID): Option[DbUserToken] = {
      val query: Statement =
        QueryBuilder.select ().
        all ().
        from ( cassandraKeyspace, DbUserToken.tableName ).
        where ( QueryBuilder.eq ( "token_id", QueryBuilder.bindMarker () ) )

      session.execute ( cache.get ( query.toString ).bind ( token ) ).all ().asScala.map ( row => DbUserToken (
        row.getUUID ( "token_id" ),
        row.getString ( "login" ),
        row.getString ( "permissions" ),
        toDateTime ( row, "token_created_on" ).get ) )
      .headOption
    }

    val getDbUserToken: (UUID) => Option[DbUserToken] = getDbUserToken ( cache, session )
  }

  def getDbUserAndUpdateToken(login: String, password: String): Option[DbUser] =
    Tracer.withNewContext ( "CassandraRepositoryJavaDriver___getDbUserAndUpdateToken", autoFinish = true ) {
      val dbUserOptional: Option[DbUser] = CassandraObject.getDbUser ( login )
      dbUserOptional match {
        case Some ( dbUser ) =>
          if (dbUser.password == password) {
            if (dbUser.tokenCreatedOn.plusDays ( 1 ).getMillis < DateTime.now.getMillis) {
              println("Regenerate token")
              session.execute ( s"DELETE FROM $cassandraKeyspace.db_user_token WHERE token_id = ${dbUser.token}" )
              val dbUserUpdated = dbUser.copy ( token = UUID.randomUUID, tokenCreatedOn = DateTime.now )
              setDbUser ( dbUserUpdated )
              setDbUserToken ( DbUserToken ( dbUserUpdated.token, dbUserUpdated.login, dbUserUpdated.permissions, dbUserUpdated.tokenCreatedOn ) )
              CassandraObject.getDbUser ( login )
            } else {
              dbUserOptional
            }
          }
          else dbUserOptional
        case None => dbUserOptional
      }
    }

  def getDbUser(login: String): Option[DbUser] =
    Tracer.withNewContext ( "CassandraRepositoryJavaDriver___getDbUser", autoFinish = true ) {
      CassandraObject.getDbUser ( login )
    }

  def setDbUser(dbUser: DbUser): Unit =
    Tracer.withNewContext ( "CassandraRepositoryJavaDriver___setDbUser", autoFinish = true ) {
      session.execute (
        QueryBuilder.insertInto ( cassandraKeyspace, DbUser.tableName ).values ( DbUser.cols, dbUser.values ) )
    }

  def getDbUserToken(token: UUID): Option[DbUserToken] =
    Tracer.withNewContext ( "CassandraRepositoryJavaDriver___getDbUserToken", autoFinish = true ) {
      CassandraObject.getDbUserToken ( token )
    }

  def setDbUserToken(dbUserToken: DbUserToken): Unit =
    Tracer.withNewContext ( "CassandraRepositoryJavaDriver___setDbUserToken", autoFinish = true ) {
      session.execute (
        QueryBuilder.insertInto ( cassandraKeyspace, DbUserToken.tableName ).values ( DbUserToken.cols, dbUserToken.values ) )
    }

}
