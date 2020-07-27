package org.codecraftlabs.aries.util

import java.io.{BufferedReader, InputStreamReader}
import java.text.SimpleDateFormat

import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.GetObjectRequest
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{FieldSeparator, FieldSeparatorDefaultValue}

import scala.collection.mutable.ListBuffer
import scala.util.Properties

object S3ObjectProcessor {
  private val logger: Logger = LogManager.getLogger(getClass)
  private val s3Service = AmazonS3ClientBuilder.standard.build
  private val CountryColumnNames = List("Country/Region", "Country_Region")
  private val StateProvinceColumnNames = List("Province_State", "Province/State")
  private val LastUpdateColumnNames = List("Last Update", "Last_Update")
  private val ConfirmedColumnNames = List("Confirmed")
  private val DeathsColumnNames = List("Deaths")
  private val RecoveredColumnNames = List("Recovered")
  private val dateTimeFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

  def readObject(bucket: String, key: String): List[CovidRecord] = {
    logger.info("Starting S3 object processing")

    val s3object = s3Service.getObject(new GetObjectRequest(bucket, key))
    val s3ObjectInputStream = s3object.getObjectContent
    val reader = new BufferedReader(new InputStreamReader(s3ObjectInputStream))
    var line: String = null

    var lineNumber: Int = 0
    var countryColPosition: Int = 0
    var stateProvinceColPosition: Int = 0
    var lastUpdateColPosition: Int = 0
    var confirmedColPosition: Int = 0
    var deathsColPosition: Int = 0
    var recoveredColPosition: Int = 0

    var clear: Boolean = true

    val processedLines: ListBuffer[CovidRecord] = ListBuffer()

    while ({line = reader.readLine.trim; line != null && clear}) {
      val tokens = line.split(Properties.envOrElse(FieldSeparator, FieldSeparatorDefaultValue))

      // Handle file header
      if (lineNumber == 0) {
        val positions = getColumnPositions(tokens)
        countryColPosition = positions(CountryColumnNames)
        stateProvinceColPosition = positions(StateProvinceColumnNames)
        lastUpdateColPosition = positions(LastUpdateColumnNames)
        confirmedColPosition = positions(ConfirmedColumnNames)
        deathsColPosition = positions(DeathsColumnNames)
        recoveredColPosition = positions(RecoveredColumnNames)

        if (countryColPosition <= 0 || stateProvinceColPosition <= 0 || lastUpdateColPosition <= 0 ||
            confirmedColPosition <= 0 || deathsColPosition <= 0 || recoveredColPosition <= 0) {
          logger.warn("Some of the columns were not found - aborting process")
          clear = false
          s3ObjectInputStream.abort()
        }
      } else {
        // Regular value processing
        val countryName = tokens(countryColPosition)
        val stateProvince = tokens(stateProvinceColPosition)
        val lastUpdate = tokens(lastUpdateColPosition)
        val confirmed = tokens(confirmedColPosition)
        val deaths = tokens(deathsColPosition)
        val recovered = tokens(recoveredColPosition)
        val record = CovidRecord(countryName, stateProvince, dateTimeFormatter.parse(lastUpdate), confirmed, deaths, recovered)
        // Join the fields
        processedLines.addOne(record)
      }
      lineNumber += 1
    }
    reader.close()
    logger.info("Finished S3 object processing")
    processedLines.toList
  }

  private def getColumnPositions(tokens: Array[String]): Map[List[String], Integer] = {
    val countryColPosition = tokens.indexWhere(CountryColumnNames.contains(_))
    val stateProvinceColPosition = tokens.indexWhere(StateProvinceColumnNames.contains(_))
    val lastUpdateColPosition = tokens.indexWhere(LastUpdateColumnNames.contains(_))
    val confirmedColPosition = tokens.indexWhere(ConfirmedColumnNames.contains(_))
    val deathsColPosition = tokens.indexWhere(DeathsColumnNames.contains(_))
    val recoveredColPosition = tokens.indexWhere(RecoveredColumnNames.contains(_))
    logger.info(s"Country column position: $countryColPosition")
    logger.info(s"State/Province column position: $stateProvinceColPosition")
    logger.info(s"Last update column position: $lastUpdateColPosition")
    logger.info(s"Confirmed column position: $confirmedColPosition")
    logger.info(s"Deaths column position: $deathsColPosition")
    logger.info(s"Recovered column position: $recoveredColPosition")

    Map(CountryColumnNames -> countryColPosition,
      StateProvinceColumnNames -> stateProvinceColPosition,
      LastUpdateColumnNames -> lastUpdateColPosition,
      ConfirmedColumnNames -> confirmedColPosition,
      DeathsColumnNames -> deathsColPosition,
      RecoveredColumnNames -> recoveredColPosition)
  }
}
