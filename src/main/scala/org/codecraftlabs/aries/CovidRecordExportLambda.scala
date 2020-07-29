package org.codecraftlabs.aries

import java.text.SimpleDateFormat

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{DestinationS3Bucket, DestinationS3Prefix, NumberIterations}
import org.codecraftlabs.aries.util.{CovidJsonRecord, CovidRecord, S3ObjectProcessor}
import org.codecraftlabs.aries.util.SQSUtil.{deleteMessages, getRecords}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import scala.util.Properties.envOrElse

class CovidRecordExportLambda {
  private val dateTimeFormatYYYY_MM_DD = new SimpleDateFormat("yyyy/MM/dd")
  private val logger: Logger = LogManager.getLogger(getClass)

  def processRecords(scheduleEvent: ScheduledEvent): String = {
    val numberIterations = envOrElse(NumberIterations, "10").toInt
    logger.info(s"Number of iterations - $numberIterations")
    for (_ <- 0 until numberIterations) {
      val items = getRecords
      val messageHandles = items.map(item => processRecord(item))
      deleteMessages(messageHandles)
    }
    "Ok"
  }

  private def processRecord(entry: CovidJsonRecord): String = {
    implicit lazy val jsonFormats: Formats = DefaultFormats
    val bucket = envOrElse(DestinationS3Bucket, "")
    val prefix = envOrElse(DestinationS3Prefix, "")
    val convertedJson = read[CovidRecord](entry.contents)
    val convertedDateTime = dateTimeFormatYYYY_MM_DD.format(convertedJson.lastUpdate)
    val keyName = prefix + "/" + convertedDateTime + "/" + entry.messageId + ".json"
    S3ObjectProcessor.writeObject(bucket, keyName, entry.contents)
    entry.receiptHandle
  }
}
