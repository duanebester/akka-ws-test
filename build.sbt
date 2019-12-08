name := "akka-ws-test"
version := "0.1"
scalaVersion := "2.13.0"

resolvers += "mvnrepository".at("http://mvnrepository.com/artifact/")
resolvers += Resolver.sonatypeRepo("public")
resolvers += Resolver.bintrayRepo("hseeberger", "maven")

addCompilerPlugin(scalafixSemanticdb)
scalacOptions ++= Seq("-deprecation", "-feature", "-Yrangepos", "-Ywarn-unused")

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.10",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.10",
  "com.typesafe.akka" %% "akka-stream" % "2.5.26"
)

Revolver.settings
