package org.codecraftlabs.aries.util

object AWSLambdaEnvironment {
  val FieldSeparator = "FIELD_SEPARATOR"
  val FieldSeparatorDefaultValue = ","
  val RecordSQSUrl = "RECORD_SQS_URL"
  val BatchSize = "BATCH_SIZE"
  val NumberIterations = "NUMBER_ITERATIONS"
  val DestinationS3Bucket = "DESTINATION_S3_BUCKET"
  val DestinationS3Prefix = "DESTINATION_S3_PREFIX"
  val SecondarySQSUrl = "SECONDARY_SQS_URL"
  val SendToSecondarySQS = "SEND_TO_SECONDARY_SQS"
}
