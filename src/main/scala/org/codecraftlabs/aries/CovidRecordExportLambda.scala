package org.codecraftlabs.aries

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.NumberIterations
import org.codecraftlabs.aries.util.SQSUtil.{deleteMessages, getRecords}

import scala.util.Properties.envOrElse

class CovidRecordExportLambda {
  private val logger: Logger = LogManager.getLogger(getClass)

  def processRecords(scheduleEvent: ScheduledEvent): String = {
    logger.info(s"Starting processor lambda - ${scheduleEvent.getTime.toDate}")
    val numberIterations = envOrElse(NumberIterations, "10").toInt

    logger.info(s"Number of iterations - $numberIterations")
    for (_ <- 0 until numberIterations) {
      val items = getRecords
      // process the messages
      val messageHandles = items.keySet
      deleteMessages(messageHandles)
    }
    "Ok"
  }
}
