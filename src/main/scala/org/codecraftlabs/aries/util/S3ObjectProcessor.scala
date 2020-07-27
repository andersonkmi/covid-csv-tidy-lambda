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

    val firstLine = reader.readLine()
    val positions = getColumnPositions(splitFormatLine(firstLine))

    val countryColPosition: Int = positions(CountryColumnNames)
    val stateProvinceColPosition: Int = positions(StateProvinceColumnNames)
    val lastUpdateColPosition: Int = positions(LastUpdateColumnNames)
    val confirmedColPosition: Int = positions(ConfirmedColumnNames)
    val deathsColPosition: Int = positions(DeathsColumnNames)
    val recoveredColPosition: Int = positions(RecoveredColumnNames)

    if (countryColPosition <= 0 || stateProvinceColPosition <= 0 || lastUpdateColPosition <= 0 ||
    confirmedColPosition <= 0 || deathsColPosition <= 0 || recoveredColPosition <= 0) {
      logger.warn("Some of the columns were not found - aborting process")
      s3ObjectInputStream.abort()
      List()
    } else {
      var line: String = null
      val processedLines: ListBuffer[CovidRecord] = ListBuffer()

      while ({line = reader.readLine; line != null}) {
        val formattedLine = splitFormatLine(line)
        val countryName = formattedLine(countryColPosition)
        val stateProvince = formattedLine(stateProvinceColPosition)
        val lastUpdate = formattedLine(lastUpdateColPosition)
        val confirmed = formattedLine(confirmedColPosition)
        val deaths = formattedLine(deathsColPosition)
        val recovered = formattedLine(recoveredColPosition)
        val record = CovidRecord(countryName, stateProvince, dateTimeFormatter.parse(lastUpdate), confirmed, deaths, recovered)
        // Join the fields
        processedLines.addOne(record)
      }
      reader.close()
      logger.info("Finished S3 object processing")
      processedLines.toList
    }
  }

  private def splitFormatLine(line: String): List[String] = {
    val tokens = line.split(Properties.envOrElse(FieldSeparator, FieldSeparatorDefaultValue))

    // the code here is required because there fields that have commas as part of the string
    // and it needs to be handled otherwise the split fails.
    val elements = ListBuffer[String]()
    val buffer = new StringBuilder()
    var separatorAlreadyFound = false
    tokens.foreach(item => {
      if (item.contains("\"") && !separatorAlreadyFound) {
        separatorAlreadyFound = true
        buffer.append(item)
      } else if (item.contains("\"") && separatorAlreadyFound) {
        buffer.append(item)
        elements.addOne(buffer.toString())
        separatorAlreadyFound = false
        buffer.clear()
      } else {
        elements.addOne(item)
      }
    })

    elements.toList
  }

  private def getColumnPositions(tokens: List[String]): Map[List[String], Integer] = {
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
