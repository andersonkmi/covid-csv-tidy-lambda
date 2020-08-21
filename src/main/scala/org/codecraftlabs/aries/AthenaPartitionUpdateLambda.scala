package org.codecraftlabs.aries

import com.amazonaws.services.lambda.runtime.{Context, RequestHandler}
import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.s3.event.S3EventNotification.S3EventNotificationRecord
import org.apache.logging.log4j.{LogManager, Logger}

import scala.jdk.CollectionConverters._

class AthenaPartitionUpdateLambda extends RequestHandler [S3Event, String] {
  private val logger: Logger = LogManager.getLogger(getClass)

  override def handleRequest(input: S3Event, context: Context): String = {
    logger.info("Starting Athena partition update lambda")
    input.getRecords.asScala.foreach(processItem)
    "Done"
  }

  private def processItem(event: S3EventNotificationRecord): Unit = {
    val key = event.getS3.getObject.getKey
    logger.info("Key: " + key)
    val fields = key.split("/")
    logger.info("Year: " + fields(1).split("%3D")(1))
    logger.info("Month: " + fields(2).split("%3D")(1))
    logger.info("Day: " + fields(3).split("%3D")(1))
  }
}
