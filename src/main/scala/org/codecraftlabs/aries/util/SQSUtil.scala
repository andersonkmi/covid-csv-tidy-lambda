package org.codecraftlabs.aries.util

import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.{AmazonSQSException, SendMessageRequest}
import org.apache.logging.log4j.LogManager
import org.codecraftlabs.aries.util.AWSLambdaEnvironment.{RecordSQSUrl, SecondarySQSUrl, DelayInSeconds}
import org.json4s.jackson.Serialization.write
import org.json4s.{DefaultFormats, Formats}

import scala.collection.mutable.ListBuffer
import scala.util.Properties.envOrElse

object SQSUtil {
  protected implicit lazy val jsonFormats: Formats = DefaultFormats
  private val logger = LogManager.getLogger(getClass)

  private val sqsService = AmazonSQSClientBuilder.standard.build

  def enqueue(record: CovidRecord): Unit = {
    try {
      val delaySeconds = envOrElse(DelayInSeconds, "5").toInt
      val json = write(record)
      val sqsMessageRequest = new SendMessageRequest().withQueueUrl(envOrElse(RecordSQSUrl, "")).withMessageBody(json).withDelaySeconds(delaySeconds)
      val result = sqsService.sendMessage(sqsMessageRequest)
      logger.info(s"Information sent to queue: message_id = '${result.getMessageId}'; md5_body = '${result.getMD5OfMessageBody}'")

      // Sending the covid record to secondary queue
      val sendToSecondarySQS = envOrElse(SecondarySQSUrl, "")
      if (sendToSecondarySQS.equals("true")) {
        val sqsMessageSecondaryRequest = new SendMessageRequest().withQueueUrl(envOrElse(SecondarySQSUrl, "")).withMessageBody(json).withDelaySeconds(delaySeconds)
        val secondaryResult = sqsService.sendMessage(sqsMessageSecondaryRequest)
        logger.info(s"Information sent to secondary queue: message_id = '${secondaryResult.getMessageId}'; md5_body = '${secondaryResult.getMD5OfMessageBody}'")
      }
    } catch {
      case exception: AmazonSQSException =>
        logger.warn(s"Failed to send message to SQS: '${exception.getMessage}'", exception)
    }
  }

  def getRecords: List[CovidJsonRecord] = {
    val messages = sqsService.receiveMessage(envOrElse(RecordSQSUrl, "")).getMessages
    val results = ListBuffer[CovidJsonRecord]()
    messages.forEach(item => {
      results.addOne(CovidJsonRecord(item.getMessageId, item.getReceiptHandle, item.getBody))
    })
    results.toList
  }

  def deleteMessages(messageHandles: List[String]): Unit = {
    messageHandles.foreach(sqsService.deleteMessage(envOrElse(RecordSQSUrl, ""), _))
  }
}
