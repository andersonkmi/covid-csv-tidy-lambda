import Dependencies._

val appVersion = "1.0.0"

val appName = "covid-csv-tidy-lambda"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.codecraftlabs",
      scalaVersion := "2.13.2",
      version      := appVersion
    )),
    name := appName,
    assemblyJarName in assembly := appName + "-" + appVersion + ".jar",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += scalacticTest % Test,
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "2.0.1",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-core" % "1.11.804",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-s3" % "1.11.804",
    libraryDependencies += "org.json4s" %% "json4s-native" % "3.6.9",
    libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.6.9",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2"
  )