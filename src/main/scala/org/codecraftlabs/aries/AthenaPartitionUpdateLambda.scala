package org.codecraftlabs.aries

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{CovidAthenaDatabase, CovidTable, DestinationS3Bucket, DestinationS3Prefix}

import scala.jdk.CollectionConverters._
import scala.util.Properties

class AthenaPartitionUpdateLambda extends RequestHandler [S3Event, String] {
  private val logger: Logger = LogManager.getLogger(getClass)

  override def handleRequest(input: S3Event, context: Context): String = {
    logger.info("Starting Athena partition update lambda")
    input.getRecords.asScala.foreach(processItem)
    "Done"
  }

  private def processItem(event: S3EventNotificationRecord): Unit = {
    val key = event.getS3.getObject.getKey
    val fields = key.split("/")
    val year = fields(1).split("%3D")(1)
    val month = fields(2).split("%3D")(1)
    val day = fields(3).split("%3D")(1)

    val path = "year=" + year + "/month=" + month + "/day=" + day + "/"
    val covidDatabase = Properties.envOrNone(CovidAthenaDatabase)
    val covidTable = Properties.envOrNone(CovidTable)

    if (covidDatabase.isDefined && covidTable.isDefined) {
      val s3Bucket = Properties.envOrElse(DestinationS3Bucket, "")
      val s3Prefix = Properties.envOrElse(DestinationS3Prefix, "")
      val statement = "ALTER TABLE " + covidTable.get + " ADD PARTITION (year = '" + year + ", month = '" + month + "', day ='" + day +  "') LOCATION 's3://" + s3Bucket + "/" + s3Prefix + "/" + path + "'"
      logger.info("Athena statement: " + statement)
    }
  }
}
