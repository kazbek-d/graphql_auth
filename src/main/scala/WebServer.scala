import actors.SimpleDbRequest
import akka.actor._
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Directives.respondWithHeaders
import akka.routing.RoundRobinPool
import com.typesafe.config.ConfigValueFactory
import kamon.Kamon

import scala.concurrent.Await
import scala.concurrent.duration.Duration


object WebServer extends App {

  import common.Implicits._
  import common.Settings._

  println("WebServer Started. v.1.0.0")

  val config = getConfig
               .withValue("kamon.statsd.hostname", ConfigValueFactory.fromAnyRef(kamonStatsdHostname))
               .withValue("kamon.statsd.port", ConfigValueFactory.fromAnyRef(kamonStatsdPort))
  Kamon.start(config)


  lazy val simpleDbRequest = system.actorOf(
    Props(new SimpleDbRequest).withRouter(RoundRobinPool(nrOfInstances = 100)), "simpleDbRequest")

  val headers = List (
    RawHeader ( "Access-Control-Allow-Origin", "*" ),
    RawHeader ( "Access-Control-Allow-Credentials", "true" ),
    RawHeader ( "Access-Control-Allow-Headers", "Authorization, Content-Type, X-Requested-With" )
  )

  val ql = new routs.QLRout(simpleDbRequest)

  val route = respondWithHeaders ( headers ) {
    ql.getRoute
  }

  Http().bindAndHandle(route, webserverAddress, webserverPort)


  Await.result(system.whenTerminated, Duration.Inf)

}