package org.codecraftlabs.aries.util

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.SendMessageRequest
import org.apache.logging.log4j.LogManager
import org.json4s.{DefaultFormats, Formats}
import org.json4s.jackson.Serialization.write
import org.codecraftlabs.aries.util.AWSLambdaEnvironment._
import scala.util.Properties._

object SQSUtil {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  private val logger = LogManager.getLogger(getClass)

  private val sqsService = AmazonSQSClientBuilder.standard.build

  def enqueue(record: CovidRecord): Unit = {
    val json = write(record)
    //logger.info(s"Sending to queue: '$json'")
    val sqsMessageRequest = new SendMessageRequest().withQueueUrl(envOrElse(RecordSQSUrl, RecordSQSUrlDefaultValue)).withMessageBody(json).withDelaySeconds(5)
    val result = sqsService.sendMessage(sqsMessageRequest)
    logger.info(s"Information sent to queue: message_id = '${result.getMessageId}'; md5_body = '${result.getMD5OfMessageBody}'")
  }
}
