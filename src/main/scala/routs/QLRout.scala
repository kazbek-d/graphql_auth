package routs

import akka.actor.ActorRef
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.headers.{BasicHttpCredentials, OAuth2BearerToken, RawHeader}
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import common.Implicits._
import graphql._
import kamon.trace.Tracer
import sangria.execution.deferred.DeferredResolver
import sangria.execution.{ErrorWithResolver, Executor, HandledException, QueryAnalysisError}
import sangria.marshalling.ResultMarshaller
import sangria.marshalling.sprayJson._
import sangria.parser.QueryParser
import spray.json._

import scala.util.{Failure, Success}

class QLRout(actorRef: ActorRef) {

  val userRepo = new UserRepo ( actorRef )

  val getRoute =
    extractCredentials { httpCredentials =>
      (post & path ( "graphql" )) {

        def process(authToken: Option[String]) = entity ( as [JsValue] ) { requestJson ⇒
          val JsObject ( fields ) = requestJson

          val JsString ( query ) = fields ( "query" )

          val operation = fields.get ( "operationName" ) collect {
            case JsString ( op ) ⇒ op
          }

          val vars = fields.get ( "variables" ) match {
            case Some ( obj: JsObject ) ⇒ obj
            case _ ⇒ JsObject.empty
          }

          val exceptionHandler = sangria.execution ExceptionHandler(
            PartialFunction [(ResultMarshaller, Throwable), HandledException] {
              case (_, e: CustomExceptions.CustomException) ⇒ HandledException ( e.getMessage )
              case (_, CustomExceptions.AuthenticationException ( message )) ⇒ HandledException ( message )
              case (_, CustomExceptions.AuthorisationException ( message )) ⇒ HandledException ( message )
              case (_, e: Throwable) ⇒ throw e
            },
            PartialFunction.empty,
            PartialFunction.empty
          )

          val fetchers = DeferredResolver.fetchers ()

          //optionalHeaderValueByName ( "SecurityToken" ) { token ⇒

          QueryParser.parse ( query ) match {
            // query parsed successfully, time to execute it!
            case Success ( queryAst ) ⇒
              Tracer.withNewContext ( "graphql___query", autoFinish = true ) {
                complete {
                  Executor.execute (
                    schema = SchemaDefinition.AuthSchema,
                    queryAst = queryAst,
                    userContext = SecureContext ( authToken, userRepo ),
                    variables = vars,
                    operationName = operation,
                    deferredResolver = fetchers,
                    exceptionHandler = exceptionHandler
                  ).map ( OK → _ )
                  .recover {
                    case error: QueryAnalysisError ⇒ BadRequest → error.resolveError
                    case error: ErrorWithResolver ⇒ InternalServerError → error.resolveError
                  }
                }
              }
            // can't parse GraphQL query, return error
            case Failure ( error ) ⇒
              complete ( BadRequest, JsObject ( "error" → JsString ( error.getMessage ) ) )
          }

          // }
        }

        httpCredentials match {
          //  Basic Authorization
          case Some ( BasicHttpCredentials ( user, pass ) ) =>
            process ( None )

          // Authorization: Bearer SECURITY_TOKEN
          case Some ( OAuth2BearerToken ( token: String ) ) =>
            process ( Some ( token ) )

          case _ =>
            process ( None )
        }

      }
    } ~ get {
      getFromResource ( "graphiql.html" )
    } ~
      options {
        complete ( HttpResponse ( StatusCodes.OK )
                   .withHeaders ( List ( RawHeader ( "Access-Control-Allow-Methods", "OPTIONS, POST, PUT, GET, DELETE" ) ) ) )
      }

}