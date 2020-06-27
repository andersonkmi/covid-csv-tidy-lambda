import sbtassembly.Log4j2MergeStrategy
import sbtrelease.Version

name := "covid-csv-tidy-lambda"

resolvers += Resolver.sonatypeRepo("public")
scalaVersion := "2.13.1"
releaseNextVersion := { ver =>
  Version(ver).map(_.bumpMinor.string).getOrElse("Error")
}
assemblyJarName in assembly := "covid-csv-tidy-lambda.jar"

libraryDependencies ++= Seq(
  "com.amazonaws" % "aws-lambda-java-events" % "2.2.7",
  "com.amazonaws" % "aws-lambda-java-core" % "1.2.1",
  "com.amazonaws" % "aws-lambda-java-log4j2" % "1.1.0",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.11.811",
  "com.amazonaws" % "aws-java-sdk-sqs" % "1.11.811",
)

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfatal-warnings"
)

assemblyMergeStrategy in assembly := {
  case PathList(ps @ _*) if ps.last == "Log4j2Plugins.dat" =>
    Log4j2MergeStrategy.plugincache
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}
