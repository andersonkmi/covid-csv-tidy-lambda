package org.codecraftlabs.aries.util

import java.io.{BufferedReader, ByteArrayInputStream, InputStreamReader}
import java.text.SimpleDateFormat

import com.amazonaws.AmazonServiceException
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.services.s3.model.{GetObjectRequest, ObjectMetadata, PutObjectRequest}
import org.apache.logging.log4j.{LogManager, Logger}
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{FieldSeparator, FieldSeparatorDefaultValue}

import scala.collection.mutable.ListBuffer
import scala.util.Properties

object S3ObjectProcessor {
  private val logger: Logger = LogManager.getLogger(getClass)
  private val s3Service = AmazonS3ClientBuilder.standard.build
  private val CountryColumn = "Country"
  private val StateProvinceColumn = "Province"
  private val LastUpdateColumn = "Update"
  private val ConfirmedColumn = "Confirmed"
  private val DeathsColumn = "Deaths"
  private val RecoveredColumn = "Recovered"
  private val dateTimeFormatYYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private val dateTimeFormatYYYY_MM_DD_T_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  private val dateTimeFormatMM_DD_YYYY_HHMM = new SimpleDateFormat("M/dd/yyyy HH:mm")

  def readObject(bucket: String, key: String): List[CovidRecord] = {
    logger.info("Starting S3 object processing")

    val s3object = s3Service.getObject(new GetObjectRequest(bucket, key))
    val s3ObjectInputStream = s3object.getObjectContent
    val reader = new BufferedReader(new InputStreamReader(s3ObjectInputStream))

    val firstLine = reader.readLine()
    val positions = getColumnPositions(splitFormatLine(firstLine))

    val countryColPosition: Int = positions(CountryColumn)
    val stateProvinceColPosition: Int = positions(StateProvinceColumn)
    val lastUpdateColPosition: Int = positions(LastUpdateColumn)
    val confirmedColPosition: Int = positions(ConfirmedColumn)
    val deathsColPosition: Int = positions(DeathsColumn)
    val recoveredColPosition: Int = positions(RecoveredColumn)

    if (countryColPosition < 0 || stateProvinceColPosition < 0 || lastUpdateColPosition < 0 ||
    confirmedColPosition < 0 || deathsColPosition < 0 || recoveredColPosition < 0) {
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
        val confirmed = if (confirmedColPosition >= formattedLine.size) 0 else formattedLine(confirmedColPosition).toLong
        val deaths = if (deathsColPosition >= formattedLine.size) 0 else formattedLine(deathsColPosition).toLong
        val recovered = if (recoveredColPosition >= formattedLine.size) 0 else formattedLine(recoveredColPosition).toLong

        if (lastUpdate.contains("/")) {
          val convertedDate = dateTimeFormatMM_DD_YYYY_HHMM.parse(lastUpdate)
          val record = CovidRecord(countryName, stateProvince, convertedDate, confirmed, deaths, recovered)
          processedLines.addOne(record)
        } else if (lastUpdate.contains("T")) {
          val convertedDate = dateTimeFormatYYYY_MM_DD_T_HH_MM_SS.parse(lastUpdate)
          val record = CovidRecord(countryName, stateProvince, convertedDate, confirmed, deaths, recovered)
          processedLines.addOne(record)
        } else {
          val convertedDate = dateTimeFormatYYYY_MM_DD_HH_MM_SS.parse(lastUpdate)
          val record = CovidRecord(countryName, stateProvince, convertedDate, confirmed, deaths, recovered)
          processedLines.addOne(record)
        }
      }

      reader.close()
      logger.info("Finished S3 object processing")
      processedLines.toList
    }
  }

  def writeObject(bucket: String, keyName: String, contents: String): Unit = {
    try {
      val metadata = new ObjectMetadata()
      metadata.setContentType("application/json")
      metadata.setContentLength(contents.getBytes.length)

      val inputStream = new ByteArrayInputStream(contents.getBytes)
      val putRequest = new PutObjectRequest(bucket, keyName, inputStream, metadata)
      s3Service.putObject(putRequest)
    } catch {
      case exception: AmazonServiceException => logger.warn("Error when creating a JSON file", exception)
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

  private def getColumnPositions(tokens: List[String]): Map[String, Integer] = {
    val countryColPosition = tokens.indexWhere(_.contains(CountryColumn))
    val stateProvinceColPosition = tokens.indexWhere(_.contains(StateProvinceColumn))
    val lastUpdateColPosition = tokens.indexWhere(_.contains(LastUpdateColumn))
    val confirmedColPosition = tokens.indexWhere(_.contains(ConfirmedColumn))
    val deathsColPosition = tokens.indexWhere(_.contains(DeathsColumn))
    val recoveredColPosition = tokens.indexWhere(_.contains(RecoveredColumn))
    logger.info(s"Country column position: $countryColPosition")
    logger.info(s"State/Province column position: $stateProvinceColPosition")
    logger.info(s"Last update column position: $lastUpdateColPosition")
    logger.info(s"Confirmed column position: $confirmedColPosition")
    logger.info(s"Deaths column position: $deathsColPosition")
    logger.info(s"Recovered column position: $recoveredColPosition")

    Map(CountryColumn -> countryColPosition,
      StateProvinceColumn -> stateProvinceColPosition,
      LastUpdateColumn -> lastUpdateColPosition,
      ConfirmedColumn -> confirmedColPosition,
      DeathsColumn -> deathsColPosition,
      RecoveredColumn -> recoveredColPosition)
  }
}
