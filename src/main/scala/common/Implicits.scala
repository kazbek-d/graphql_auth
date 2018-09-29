package common

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import data.CassandraRepositoryJavaDriver



object Implicits {

  implicit val system = ActorSystem("GraphqlAuth")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher


  implicit val repositoryCassyJavaDriver = new CassandraRepositoryJavaDriver


}