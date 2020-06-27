package org.codecraftlabs.aries.util

import java.io.{BufferedReader, InputStreamReader}

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{FieldSeparator, FieldSeparatorDefaultValue}

import scala.util.Properties

object S3ObjectReader {
  private val logger: Logger = LogManager.getLogger(getClass)
  private val s3Service = AmazonS3ClientBuilder.standard.build
  private val CountryColumnNames = List("Country/Region", "Country_Region")
  private val LastUpdateColumnNames = List("Last Update", "Last_Update")
  private val ConfirmedColumnNames = List("Confirmed")
  private val DeathsColumnNames = List("Deaths")
  private val RecoveredColumnNames = List("Recovered")

  def readObject(bucket: String, key: String): Unit = {
    logger.info("Starting S3 object processing")

    val s3object = s3Service.getObject(new GetObjectRequest(bucket, key))
    val s3ObjectInputStream = s3object.getObjectContent
    val reader = new BufferedReader(new InputStreamReader(s3ObjectInputStream))
    var line: String = null

    var lineNumber: Int = 0;
    var countryColPosition: Int = 0
    var lastUpdateColPosition: Int = 0
    var confirmedColPosition: Int = 0
    var deathsColPosition: Int = 0
    var recoveredColPosition: Int = 0

    var clear: Boolean = true;
    while ({line = reader.readLine; line != null && clear}) {
      val tokens = line.split(Properties.envOrElse(FieldSeparator, FieldSeparatorDefaultValue))
      if (lineNumber == 0) {
        val positions = getColumnPositions(tokens)

        countryColPosition = positions(CountryColumnNames)
        lastUpdateColPosition = positions(LastUpdateColumnNames)
        confirmedColPosition = positions(ConfirmedColumnNames)
        deathsColPosition = positions(DeathsColumnNames)
        recoveredColPosition = positions(RecoveredColumnNames)

        if (countryColPosition <= 0 ||
          lastUpdateColPosition <= 0 ||
          confirmedColPosition <= 0 ||
          deathsColPosition <= 0 ||
          recoveredColPosition <= 0) {
          logger.warn("Some of the columns were not found - aborting process")
          clear = false
          s3ObjectInputStream.abort()
        }
      } else {
        // Regular value processing
        val countryName = tokens(countryColPosition)
        logger.info(s"Country name: $countryName")
      }
      lineNumber += 1
    }
    reader.close()
    logger.info("Finished S3 object processing")
  }

  private def getColumnPositions(tokens: Array[String]): Map[List[String], Integer] = {
    val countryColPosition = tokens.indexWhere(CountryColumnNames.contains(_))
    val lastUpdateColPosition = tokens.indexWhere(LastUpdateColumnNames.contains(_))
    val confirmedColPosition = tokens.indexWhere(ConfirmedColumnNames.contains(_))
    val deathsColPosition = tokens.indexWhere(DeathsColumnNames.contains(_))
    val recoveredColPosition = tokens.indexWhere(RecoveredColumnNames.contains(_))
    logger.info(s"Country column position: $countryColPosition")
    logger.info(s"Last update column position: $lastUpdateColPosition")
    logger.info(s"Confirmed column position: $confirmedColPosition")
    logger.info(s"Deaths column position: $deathsColPosition")
    logger.info(s"Recovered column position: $recoveredColPosition")

    Map(CountryColumnNames -> countryColPosition, LastUpdateColumnNames -> lastUpdateColPosition, ConfirmedColumnNames -> confirmedColPosition, DeathsColumnNames -> deathsColPosition, RecoveredColumnNames -> recoveredColPosition)
  }
}
