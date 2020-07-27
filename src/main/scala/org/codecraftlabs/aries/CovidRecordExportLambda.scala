package org.codecraftlabs.aries

import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import org.apache.logging.log4j.{LogManager, Logger}

class CovidRecordExportLambda {
  private val logger: Logger = LogManager.getLogger(getClass)

  def processRecords(scheduleEvent: ScheduledEvent): String = {
    logger.info(s"Starting processor lambda - ${scheduleEvent.getTime.toDate}")
    "Ok"
  }
}
