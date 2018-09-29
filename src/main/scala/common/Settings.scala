package common

import com.typesafe.config.ConfigFactory

object Settings {

  private val config = ConfigFactory.load()

  def getConfig = config

  val cassandraAddress: Array[String] = scala.util.Properties.envOrElse("CASSANDRA_ADDRESS", "localhost").split(",")
  val cassandraPort = scala.util.Properties.envOrElse("CASSANDRA_PORT", "9042").toInt
  val cassandraKeyspace: String = scala.util.Properties.envOrElse("CASSANDRA_KEYSPACE", "")
  val cassandraLogin: String = scala.util.Properties.envOrElse("CASSANDRA_LOGIN", "")
  val cassandraPassword: String = scala.util.Properties.envOrElse("CASSANDRA_PASSWORD", "")


  val webserverAddress = scala.util.Properties.envOrElse("WEBSERVER_ADDRESS", "")
  val webserverPort = scala.util.Properties.envOrElse("WEBSERVER_PORT", "8101").toInt


  val kamonStatsdHostname: String = scala.util.Properties.envOrElse("KAMON_STATSD_HOSTNAME", "127.0.0.1")
  val kamonStatsdPort: String = scala.util.Properties.envOrElse("KAMON_STATSD_PORT", "8125")
}
