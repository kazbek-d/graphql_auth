import com.typesafe.sbt.packager.docker.{Cmd, ExecCmd}

name := "graphql-auth"

organization := "com.graphql_auth"

version := "1.0"

scalaVersion := "2.12.4"

scalacOptions += "-Ypartial-unification"

val akkaVersion = "2.5.6"
val akkaHttpVersion = "10.0.10"
val kamon = "0.6.7"


val main = Project(id = "graphql-auth", base = file("."))
           .enablePlugins(JavaAppPackaging)
           .settings(
             dockerCommands :=
               dockerCommands.value.flatMap {
                 case ExecCmd("ENTRYPOINT", args @ _*) => Seq(Cmd("ENTRYPOINT", args.mkString(" ")))
                 case v => Seq(v)
               }
           )
           .settings(
             dockerUpdateLatest := true
           )
           .settings(
             dockerBaseImage := "local/openjdk-jre-8-bash"
           )



libraryDependencies ++= Seq(
  // DataStax Java Driver For Apache Cassandra Core
  "com.datastax.cassandra" % "cassandra-driver-core" % "3.3.0",
  "com.datastax.dse" % "dse-java-driver-core" % "1.4.2",
  
  // Joda
  "joda-time" % "joda-time" % "2.9.7",
  
  // Akka
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-agent" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion,

  // Akka Http
  "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-jackson" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-xml" % akkaHttpVersion,

  // Sangria
  "org.sangria-graphql" %% "sangria" % "1.3.0",
  "org.sangria-graphql" %% "sangria-spray-json" % "1.0.0",


  // Kamon
  "io.kamon" %% "kamon-core" % kamon,
  "io.kamon" %% "kamon-statsd" % kamon,
  "io.kamon" %% "kamon-datadog" % kamon
)

resolvers += "Akka Snapshots" at "http://repo.akka.io/snapshots/"

mainClass in Compile := Some("WebServer")