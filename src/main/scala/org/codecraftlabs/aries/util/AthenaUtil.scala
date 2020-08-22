package org.codecraftlabs.aries.util

import com.amazonaws.services.athena.AmazonAthenaClientBuilder
import com.amazonaws.services.athena.model.{QueryExecutionContext, ResultConfiguration, StartQueryExecutionRequest}
import org.apache.logging.log4j.LogManager
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{AthenaResultS3Bucket, AthenaResultS3Prefix, CovidAthenaDatabase}

import scala.util.Properties

object AthenaUtil {
  private val logger = LogManager.getLogger(getClass)
  private val athenaClient = AmazonAthenaClientBuilder.standard.build

  def executeQuery(statement: String): Unit = {
    logger.info("Statement: " + statement)
    val databaseName = Properties.envOrElse(CovidAthenaDatabase, "")
    val executionId = submitQuery(databaseName, statement)
    waitForQueryExecution(executionId)
  }

  private def submitQuery(database: String, statement: String): String = {
    val resultS3Bucket = Properties.envOrElse(AthenaResultS3Bucket, "")
    val resultS3Prefix = Properties.envOrElse(AthenaResultS3Prefix, "")
    val resultingBucketPrefix = "s3://" + resultS3Bucket + "/" + resultS3Prefix + "/"

    val queryExecutionContext = new QueryExecutionContext()
    queryExecutionContext.setDatabase(database)
    val resultConfiguration = new ResultConfiguration()
    resultConfiguration.setOutputLocation(resultingBucketPrefix)

    val startQueryExecutionRequest = new StartQueryExecutionRequest()
    startQueryExecutionRequest.setQueryString(statement)
    startQueryExecutionRequest.setQueryExecutionContext(queryExecutionContext)
    startQueryExecutionRequest.setResultConfiguration(resultConfiguration)

    val startQueryExecutionResponse = athenaClient.startQueryExecution(startQueryExecutionRequest)
    startQueryExecutionResponse.getQueryExecutionId
  }

  private def waitForQueryExecution(executionId: String): Unit = {
    import com.amazonaws.services.athena.model.GetQueryExecutionRequest
    import com.amazonaws.services.athena.model.QueryExecutionState
    val getQueryExecutionRequest = new GetQueryExecutionRequest()
    getQueryExecutionRequest.setQueryExecutionId(executionId)

    var isQueryStillRunning = true
    while (isQueryStillRunning) {
      val getQueryExecutionResponse = athenaClient.getQueryExecution(getQueryExecutionRequest)
      val queryState = getQueryExecutionResponse.getQueryExecution.getStatus.getState

      if (QueryExecutionState.FAILED.toString.equals(queryState)) {
        logger.error("Query execution failed: " + getQueryExecutionResponse.getQueryExecution.getStatus.getStateChangeReason)
        isQueryStillRunning = false
      } else if (QueryExecutionState.CANCELLED.toString.equals(queryState)) {
        logger.warn("Query execution cancelled")
        isQueryStillRunning = false
      } else if (QueryExecutionState.SUCCEEDED.toString.equals(queryState)) {
        logger.info("Query execution finished successfully")
        isQueryStillRunning = false
      } else {
        logger.info("Still running - waiting a little bit")
        val sleepTimeInMs = Properties.envOrElse(AWSLambdaEnvironment.SleepTimeInMs, "5000").toInt
        Thread.sleep(sleepTimeInMs)
      }
    }
  }
}
