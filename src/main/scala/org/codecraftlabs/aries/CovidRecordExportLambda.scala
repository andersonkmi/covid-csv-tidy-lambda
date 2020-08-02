package org.codecraftlabs.aries

import java.text.SimpleDateFormat

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{DestinationS3Bucket, DestinationS3Prefix, NumberIterations}
import org.codecraftlabs.aries.util.DateTimeFormatters.{YYYY_MM_DD, YYYY_MM_DD_T_HH_MM_SS}
import org.codecraftlabs.aries.util.{CovidJsonRecord, CovidRecord, DateTimeFormatters, S3ObjectProcessor}
import org.codecraftlabs.aries.util.SQSUtil.{deleteMessages, getRecords}
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.read

import scala.util.Properties.envOrElse

class CovidRecordExportLambda {
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
    val convertedDateTime = YYYY_MM_DD.format(convertedJson.lastUpdate)
    //val keyName = prefix + "/" + convertedDateTime + "/" + entry.messageId + ".json"
    val keyName = prefix + "/" + convertedDateTime + "/" + generateKey(convertedJson)
    S3ObjectProcessor.writeObject(bucket, keyName, entry.contents)
    entry.receiptHandle
  }

  private def generateKey(record: CovidRecord): String = {
    val buffer = new StringBuilder
    buffer.append(record.country.replaceAll(" ", ""))
    buffer.append("_")
    buffer.append(record.stateProvince.replaceAll(" ", ""))
    buffer.append("_")
    buffer.append(YYYY_MM_DD_T_HH_MM_SS.format(record.lastUpdate))
    buffer.append("_")
    buffer.append(record.confirmed)
    buffer.append("_")
    buffer.append(record.deaths)
    buffer.append("_")
    buffer.append(record.recovered)
    buffer.append(".json")
    buffer.toString()
  }
}
