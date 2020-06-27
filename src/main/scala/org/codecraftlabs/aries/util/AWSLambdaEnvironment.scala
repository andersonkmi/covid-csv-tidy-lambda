package org.codecraftlabs.aries.util

object AWSLambdaEnvironment {
  val FieldSeparator = "FIELD_SEPARATOR"
  val FieldSeparatorDefaultValue = ","

  val S3DestinationBucket = "S3_DESTINATION_BUCKET"
  val S3DestinationBucketDefaultValue = "FakeBucket"

  val RecordSQSUrl = "RECORD_SQS_URL"
  val RecordSQSUrlDefaultValue = "https://sqs.us-east-1.amazonaws.com/970221509170/csv-data-processor"
}
