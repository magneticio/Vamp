import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import sbt.Keys._

import scala.language.postfixOps
import scalariform.formatter.preferences._

organization in ThisBuild := "io.vamp"

name := """vamp"""

version in ThisBuild := VersionHelper.versionByTag // version based on env VAMP_VERSION and "git describe"

scalaVersion := "2.12.8"

scalaVersion in ThisBuild := scalaVersion.value

description in ThisBuild := """Vamp"""

organizationName in ThisBuild  := "Magnetic.io"
organizationHomepage in ThisBuild  := Some(url("http://magnetic.io"))

bintrayOrganization in ThisBuild  := Some("magnetic-io")
bintrayRepository in ThisBuild := "vamp"

licenses in ThisBuild += ("Apache-2.0", url("https://opensource.org/licenses/Apache-2.0"))

resolvers in ThisBuild ++= Seq(
  Resolver.typesafeRepo("releases"),
  Resolver.sonatypeRepo("releases"),
  Resolver.jcenterRepo
)

// Libraries
val akka = "com.typesafe.akka" %% "akka-stream" % "2.5.23" ::
  "com.typesafe.akka" %% "akka-actor" % "2.5.23" ::
  "com.typesafe.akka" %% "akka-http" % "10.1.8" ::
  "com.typesafe.akka" %% "akka-parsing" % "10.1.8" ::
  "com.lightbend.akka" %% "akka-stream-alpakka-sse" % "1.0.2" ::
  "ch.megard" %% "akka-http-cors" % "0.4.0" ::
  ("com.typesafe.akka" %% "akka-slf4j" % "2.5.9" exclude("org.slf4j", "slf4j-api")) :: Nil

val bouncyCastle =
  "org.bouncycastle" % "bcprov-jdk15on" % "1.61" ::
  "org.bouncycastle" % "bcprov-ext-jdk15on" % "1.61" :: 
  "org.bouncycastle" % "bcpkix-jdk15on" % "1.61" :: Nil

val json4s = "org.json4s" %% "json4s-native" % "3.5.0" ::
  "org.json4s" %% "json4s-core" % "3.5.0" ::
  "org.json4s" %% "json4s-ext" % "3.5.0" :: Nil

val snakeYaml = "org.yaml" % "snakeyaml" % "1.16" :: Nil

val kamon = ("io.kamon" %% "kamon-core" % "0.6.4" excludeAll ExclusionRule(organization = "com.typesafe.akka")) ::
  "org.slf4j" % "jul-to-slf4j" % "1.7.21" :: Nil

val logging = "org.slf4j" % "slf4j-api" % "1.7.21" ::
  "ch.qos.logback" % "logback-classic" % "1.2.0" ::
  ("com.typesafe.scala-logging" %% "scala-logging" % "3.5.0" exclude("org.slf4j", "slf4j-api")) :: Nil

val testing = "junit" % "junit" % "4.11" % "test" ::
  "org.scalatest" %% "scalatest" % "3.0.1" % "test" ::
  "org.scalacheck" %% "scalacheck" % "1.13.4" % "test" ::
  "com.typesafe.akka" %% "akka-testkit" % "2.5.9" % "test" :: Nil

val templating = Seq(
  "org.jtwig" % "jtwig-core" % "5.87.0.RELEASE" exclude("com.google.guava", "guava"),
  "com.google.guava" % "guava" % "27.1-jre"
)

val sql = Seq(
  "org.postgresql" % "postgresql" % "42.2.5",
  "mysql" % "mysql-connector-java" % "8.0.16",
  "com.microsoft.sqlserver" % "mssql-jdbc" % "7.2.2.jre8" excludeAll ExclusionRule(organization = "org.bouncycastle"),
  "org.xerial" % "sqlite-jdbc" % "3.19.3"
) ++ bouncyCastle

val caching = Seq("com.github.cb372" %% "scalacache-caffeine" % "0.22.0")

val config = Seq("com.typesafe"  % "config" % "1.3.1")

val redislbs = Seq("com.github.etaty" %% "rediscala" % "1.8.0")

val zookeeperlbs = Seq("org.apache.zookeeper" % "zookeeper" % "3.5.5"
  exclude("org.slf4j", "slf4j-log4j12") exclude("log4j", "log4j"))

val dockerlbs = Seq(
  "com.spotify" % "docker-client" % "8.15.3" excludeAll ExclusionRule(organization = "org.bouncycastle")
) ++ bouncyCastle

val apache = Seq("org.apache.commons" % "commons-dbcp2" % "2.0.1")

val k8s = Seq(
  "io.kubernetes" % "client-java" % "2.0.0" excludeAll ExclusionRule(organization = "org.bouncycastle")
) ++ bouncyCastle

val natslibs = Seq("io.nats" % "java-nats-streaming" % "2.1.2")

// Force scala version for the dependencies
dependencyOverrides in ThisBuild ++= Set(
  "org.scala-lang" % "scala-compiler" % scalaVersion.value,
  "org.scala-lang" % "scala-library" % scalaVersion.value
)

lazy val formatting = scalariformSettings ++ Seq(ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(CompactControlReadability, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(DanglingCloseParenthesis, Preserve)
  .setPreference(RewriteArrowSymbols, true))

lazy val root = project.in(file(".")).settings(
  // Disable publishing root empty pom
  packagedArtifacts in RootProject(file(".")) := Map.empty,
  // allows running main classes from subprojects
  run := {
    (run in bootstrap in Compile).evaluated
  },
  publishLocal := {},
  publish := { },
  bintrayUnpublish := {}
).aggregate(
  common,
  persistence,
  model,
  operation,
  bootstrap,
  container_driver,
  workflow_driver,
  pulse,
  http_api,
  gateway_driver,
  dcos,
  elasticsearch,
  nats,
  haproxy,
  redis,
  zookeeper,
  consul,
  etcd,
  kubernetes)

lazy val bootstrap = project.settings(packAutoSettings).settings(
  description := "Bootstrap for Vamp",
  name := "vamp-bootstrap",
  formatting,
  bintrayRepository := "vamp"
).dependsOn(common,
  persistence,
  model,
  operation,
  container_driver,
  workflow_driver,
  pulse,
  http_api,
  gateway_driver,
  dcos,
  elasticsearch,
  nats,
  haproxy,
  redis,
  zookeeper,
  consul,
  etcd,
  kubernetes)

lazy val http_api = project.settings(
  description := "Http Api for Vamp",
  name := "vamp-http_api",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(operation)

lazy val operation = project.settings(
  description := "The control center of Vamp",
  name := "vamp-operation",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(persistence, container_driver, workflow_driver, gateway_driver, pulse)

lazy val pulse = project.settings(
  description := "Enables Vamp to connect to event storage - Elasticsearch",
  name := "vamp-pulse",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(model)

lazy val gateway_driver = project.settings(
  description := "Enables Vamp to talk to Vamp Gateway Agent",
  name := "vamp-gateway_driver",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(model, pulse, persistence)

lazy val container_driver = project.settings(
  description := "Enables Vamp to talk to container managers",
  name := "vamp-container_driver",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(model, persistence, pulse)

lazy val workflow_driver = project.settings(
  description := "Enables Vamp to talk to workflow managers",
  name := "vamp-workflow_driver",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(model, pulse, persistence, container_driver)

lazy val persistence = project.settings(
  description := "Stores Vamp artifacts",
  name := "vamp-persistence",
  formatting,
  libraryDependencies ++= testing ++ sql ++ apache,
  bintrayRepository := "vamp"
).dependsOn(model, pulse)

lazy val model = project.settings(
  description := "Definitions of Vamp artifacts",
  name := "vamp-model",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(common)

lazy val common = project.settings(
  description := "Vamp common",
  name := "vamp-common",
  formatting,
  libraryDependencies ++= akka ++ json4s ++ snakeYaml ++ kamon ++ logging ++ caching ++ config ++ testing,
  bintrayRepository := "vamp"
)

lazy val dcos = project.settings(
  description := "Container driver for DCOS and Marathon/Mesos",
  name := "vamp-dcos",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(pulse, workflow_driver, container_driver)

lazy val elasticsearch = project.settings(
  description := "Pulse and metrics driver for Elasticsearch",
  name := "vamp-elasticsearch",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(pulse, persistence)

lazy val nats = project.settings(
  description := "Pulse and metrics driver for Nats",
  name := "vamp-nats",
  formatting,
  libraryDependencies ++= testing ++ natslibs ++ bouncyCastle,
  bintrayRepository := "vamp"
).dependsOn(pulse)

lazy val haproxy = project.settings(
  description := "HAProxy driver",
  name := "vamp-haproxy",
  formatting,
  libraryDependencies ++= testing ++ templating,
  bintrayRepository := "vamp"
).dependsOn(gateway_driver)

lazy val redis = project.settings(
  description := "Redis driver for VAMP",
  name := "vamp-redis",
  formatting,
  libraryDependencies ++= testing ++ redislbs,
  bintrayRepository := "vamp"
).dependsOn(persistence)

lazy val zookeeper = project.settings(
  description := "Zookeeper driver for VAMP K/V Store",
  name := "vamp-zookeeper",
  formatting,
  libraryDependencies ++= testing ++ zookeeperlbs,
  bintrayRepository := "vamp"
).dependsOn(persistence)

lazy val consul = project.settings(
  description := "Driver for Consul",
  name := "vamp-consul",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(persistence)

lazy val etcd =  project.settings(
  description := "Driver for ETCD",
  name := "vamp-etcd",
  formatting,
  libraryDependencies ++= testing,
  bintrayRepository := "vamp"
).dependsOn(persistence)

lazy val kubernetes = project.settings(
  description := "Container driver for Kubernetes",
  name := "vamp-kubernetes",
  formatting,
  libraryDependencies ++= k8s ++ testing,
  bintrayRepository := "vamp"
).dependsOn(pulse, workflow_driver, container_driver)

lazy val docker = project.settings(
  description := "Container driver for docker",
  name := "vamp-docker",
  formatting,
  libraryDependencies ++= testing ++ dockerlbs,
  bintrayRepository := "vamp"
).dependsOn(pulse, workflow_driver, container_driver)

// Java version and encoding requirements
scalacOptions += "-target:jvm-1.8"

javacOptions ++= Seq("-encoding", "UTF-8")

scalacOptions in ThisBuild ++= Seq(Opts.compile.deprecation, Opts.compile.unchecked) ++
  Seq("-Ywarn-unused-import", "-Ywarn-unused", "-Xlint", "-feature")
