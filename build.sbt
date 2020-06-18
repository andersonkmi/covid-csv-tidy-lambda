val appVersion = "1.0.0"

val appName = "covid-csv-tidy-lambda"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "org.codecraftlabs",
      scalaVersion := "2.12.4",
      version      := appVersion
    )),
    name := appName,
    assemblyJarName in assembly := appName + "-" + appVersion + ".jar",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-core" % "1.2.0",
    libraryDependencies += "com.amazonaws" % "aws-lambda-java-events" % "2.0.1",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.281",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-core" % "1.11.281",
    libraryDependencies += "com.amazonaws" % "aws-java-sdk-dynamodb" % "1.11.280",
    libraryDependencies += "org.json4s" %% "json4s-native" % "3.5.2",
    libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.5.2",
    libraryDependencies += "ch.qos.logback" % "logback-classic" % "1.2.3",
    libraryDependencies += "com.typesafe.scala-logging" %% "scala-logging" % "3.7.2",
    libraryDependencies += "org.scalactic" %% "scalactic" % "3.1.2",
    libraryDependencies += "org.scalatest" %% "scalatest" % "3.1.2" % "test"
  )