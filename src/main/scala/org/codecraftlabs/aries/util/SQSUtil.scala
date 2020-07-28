package org.codecraftlabs.aries.util

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.{AmazonSQSException, SendMessageRequest}
import org.apache.logging.log4j.LogManager
import org.codecraftlabs.aries.util.AWSLambdaEnvironment._
import org.json4s.jackson.Serialization.{read, write}
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable
import scala.util.Properties.envOrElse

object SQSUtil {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  private val logger = LogManager.getLogger(getClass)

  private val sqsService = AmazonSQSClientBuilder.standard.build

  def enqueue(record: CovidRecord): Unit = {
    try {
      val json = write(record)
      val sqsMessageRequest = new SendMessageRequest().withQueueUrl(envOrElse(RecordSQSUrl, "")).withMessageBody(json).withDelaySeconds(5)
      val result = sqsService.sendMessage(sqsMessageRequest)
      logger.debug(s"Information sent to queue: message_id = '${result.getMessageId}'; md5_body = '${result.getMD5OfMessageBody}'")
    } catch {
      case exception: AmazonSQSException =>
        logger.warn(s"Failed to send message to SQS: '${exception.getMessage}'", exception)
    }
  }

  def getRecords: Map[String, String] = {
    val messages = sqsService.receiveMessage(envOrElse(RecordSQSUrl, "")).getMessages
    val results = mutable.Map[String, String]()
    messages.forEach(item => results(item.getReceiptHandle) = item.getBody)
    results.toMap
  }

  def deleteMessages(messageHandles: Set[String]): Unit = {
    messageHandles.foreach(sqsService.deleteMessage(envOrElse(RecordSQSUrl, ""), _))
  }
}
