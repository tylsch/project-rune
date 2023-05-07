import akka.grpc.sbt.AkkaGrpcPlugin
import com.typesafe.sbt.packager.Keys.*
import com.typesafe.sbt.packager.archetypes.JavaAppPackaging
import com.typesafe.sbt.packager.docker.DockerPlugin
import sbt.Keys.*
import sbt.*
import sbtdynver.DynVerPlugin.autoImport.dynverSeparator

object AkkaAppDependencies {

  val AkkaVersion = "2.8.0"
  val AkkaHttpVersion = "10.5.0"
  val AkkaManagementVersion = "1.2.0"
  val AkkaPersistenceR2dbcVersion = "1.0.1"
  val AlpakkaKafkaVersion = "4.0.0"
  val AkkaProjectionVersion = "1.3.1"
  val AkkaDiagnosticsVersion = "2.0.0"

  def appModule(moduleName: String, fileName: String): Project =
    Project(moduleName, file(fileName))
      .enablePlugins(AkkaGrpcPlugin, JavaAppPackaging, DockerPlugin)
      .configs(IntegrationTest)
      .settings(
        name := moduleName,
        scalaVersion := "2.13.10",
        version := "0.1.0-SNAPSHOT",
        Defaults.itSettings,
        dockerBaseImage := "docker.io/library/adoptopenjdk:17-jre-hotspot",
        dockerUsername := sys.props.get("docker.username"),
        dockerRepository := sys.props.get("docker.registry"),
        dynverSeparator := "-",
        Compile / scalacOptions ++= Seq(
          "-target:17",
          "-deprecation",
          "-feature",
          "-unchecked",
          "-Xlog-reflective-calls",
          "-Xlint"),
        Compile / javacOptions ++= Seq("-Xlint:unchecked", "-Xlint:deprecation"),
        Test / parallelExecution := false,
        Test / testOptions += Tests.Argument("-oDF"),
        Test / logBuffered := false,
        IntegrationTest / fork := true,
        run / fork := true,
        run / javaOptions ++= sys.props
          .get("config.resource")
          .fold(Seq.empty[String])(res => Seq(s"-Dconfig.resource=$res")),
        Global / cancelable := false,
        libraryDependencies ++= Seq(
          "com.rune.harmonia" %% "harmonia-exchange" % "0.1.0-SNAPSHOT" % "protobuf-src",
          // 1. Basic dependencies for a clustered application
          "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
          "com.typesafe.akka" %% "akka-cluster-typed" % AkkaVersion,
          "com.typesafe.akka" %% "akka-cluster-sharding-typed" % AkkaVersion,
          "com.typesafe.akka" %% "akka-actor-testkit-typed" % AkkaVersion % "test,it",
          "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion % "test,it",
          // Akka Management powers Health Checks and Akka Cluster Bootstrapping
          "com.lightbend.akka.management" %% "akka-management" % AkkaManagementVersion,
          "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
          "com.typesafe.akka" %% "akka-http-spray-json" % AkkaHttpVersion,
          "com.lightbend.akka.management" %% "akka-management-cluster-http" % AkkaManagementVersion,
          "com.lightbend.akka.management" %% "akka-management-cluster-bootstrap" % AkkaManagementVersion,
          "com.typesafe.akka" %% "akka-discovery" % AkkaVersion,
          "com.lightbend.akka" %% "akka-diagnostics" % AkkaDiagnosticsVersion,
          // Common dependencies for logging and testing
          "com.typesafe.akka" %% "akka-slf4j" % AkkaVersion,
          "ch.qos.logback" % "logback-classic" % "1.4.6",
          "org.scalatest" %% "scalatest" % "3.2.15" % "test,it",
          "com.dimafeng" %% "testcontainers-scala-scalatest" % "0.40.15" % "it",
          // Using gRPC and/or protobuf
          "com.typesafe.akka" %% "akka-http2-support" % AkkaHttpVersion,
          // Using Akka Persistence
          "com.typesafe.akka" %% "akka-persistence-typed" % AkkaVersion,
          "com.typesafe.akka" %% "akka-serialization-jackson" % AkkaVersion,
          "com.lightbend.akka" %% "akka-persistence-r2dbc" % AkkaPersistenceR2dbcVersion,
          "com.typesafe.akka" %% "akka-persistence-testkit" % AkkaVersion % "test,it",
          // Querying and publishing data from Akka Persistence
          "com.typesafe.akka" %% "akka-persistence-query" % AkkaVersion,
          "com.lightbend.akka" %% "akka-projection-r2dbc" % AkkaPersistenceR2dbcVersion,
          "com.lightbend.akka" %% "akka-projection-grpc" % AkkaProjectionVersion,
          "com.lightbend.akka" %% "akka-projection-eventsourced" % AkkaProjectionVersion,
          "com.lightbend.akka" %% "akka-projection-testkit" % AkkaProjectionVersion % "test,it")
      )

}
