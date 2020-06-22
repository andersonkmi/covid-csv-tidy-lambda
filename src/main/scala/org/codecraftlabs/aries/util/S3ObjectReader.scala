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

  def readObject(bucket: String, key: String): Unit = {
    val s3object = s3Service.getObject(new GetObjectRequest(bucket, key))
    val reader = new BufferedReader(new InputStreamReader(s3object.getObjectContent))
    var line: String = null

    var lineNumber: Int = 0;
    while ({line = reader.readLine; line != null}) {
      val tokens = line.split(Properties.envOrElse(FieldSeparator, FieldSeparatorDefaultValue))
      if (lineNumber == 0) {
        // find the column index for considered values
      }
      lineNumber += 1
    }
    reader.close()
  }
}
