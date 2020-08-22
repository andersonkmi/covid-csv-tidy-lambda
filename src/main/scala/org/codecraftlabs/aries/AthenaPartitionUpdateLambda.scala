package org.codecraftlabs.aries

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{CovidAthenaDatabase, CovidTable, DestinationS3Bucket, DestinationS3Prefix}
import org.codecraftlabs.aries.util.AthenaUtil.executeQuery

import scala.util.Properties

class AthenaPartitionUpdateLambda {
  private val logger: Logger = LogManager.getLogger(getClass)

  def handleRequest(scheduleEvent: ScheduledEvent): Unit = {
    logger.info("Starting Athena partition update lambda")
    val dateTime = scheduleEvent.getTime
    val year = dateTime.getYear.toString
    val month = dateTime.toString("MM")
    val day = dateTime.toString("dd")
    processPartition(year, month, day)
  }

  private def processPartition(year: String, month: String, day: String): Unit = {
    val path = "year=" + year + "/month=" + month + "/day=" + day + "/"
    val covidDatabase = Properties.envOrNone(CovidAthenaDatabase)
    val covidTable = Properties.envOrNone(CovidTable)

    if (covidDatabase.isDefined && covidTable.isDefined) {
      val s3Bucket = Properties.envOrElse(DestinationS3Bucket, "")
      val s3Prefix = Properties.envOrElse(DestinationS3Prefix, "")
      val statement = "ALTER TABLE " + covidDatabase.get + "." + covidTable.get + " ADD IF NOT EXISTS PARTITION (year='" + year + "', month='" + month + "', day='" + day + "') LOCATION 's3://" + s3Bucket + "/" + s3Prefix + "/" + path + "'"
      executeQuery(statement)
    }
  }
}
