package org.codecraftlabs.aries.util

import com.amazonaws.services.athena.AmazonAthenaClientBuilder
import org.apache.logging.log4j.LogManager

object AthenaUtil {
  private val logger = LogManager.getLogger(getClass)
  private val athenaClient = AmazonAthenaClientBuilder.standard.build

  def executeQuery(statement: String): Unit = {
    logger.info("Statement: " + statement)
  }

  private def submitQuery(statement: String): String = ???

  private def waitForQueryExecution(executionId: String): Unit = ???
}
