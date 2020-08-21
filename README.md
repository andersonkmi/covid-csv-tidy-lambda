# COVID CSV files tidy and export lambda

This is another project my mine to practice some AWS services (AWS Lambda, S3, Serverless framework, SQS and Athena)

AWS Lambda for processing and tidying CSV files with COVID data to standardize date/time format and extract the desired 
columns to play with.

The source code developed was based on the CSV format provided by the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University. See References section for more information.

## How to build

In order to build this lambda function, run the following command:

```
$ sbt clean assembly
```

## How to configure the common config file for both lambdas

When deploying the lambda functions they require a config file with the values to be used. Such file needs to be named:

- serverless-config-dev.yaml (for dev stage)
- serverless-config-prod.yaml (for prod stage)

The following fields need to be configured:

- deploy-bucket
- lambda-role
- record-sqs-url
- field-separator
- number-iterations
- destination-s3-bucket
- destination-s3-prefix
- vpc-security-group-ids
- vpc-subnet-ids
- send-to-secondary-sqs
- secondary-sqs-url
- delay-seconds
- timeout-tidy-lambda
- timeout-export-lambda

## How to deploy/remove the CSV tidy lambda function

In order to deploy the tidy lambda function, run the following command:

```
$ sls deploy --stage dev --config serverless-tidy-lambda.yml
```

In order to remove the tidy lambda function, run the following command:

```
$ sls remove --stage dev --config serverless-tidy-lambda.yml
```

## How to deploy/remove the record export lambda function

In order to deploy the export lambda function, run the following command:

```
$ sls deploy --stage dev --config serverless-export-lambda.yml
```

In order to remove the export lambda function, run the following command:

```
$ sls remove --stage dev --config serverless-export-lambda.yml
```

## AWS Athena

In order to create the table inside AWS Athena, use the SQL script in the project. You need first to create a database that will hold such table.

```
aws-athena/aws-athena.sql
```

## References

- COVID-19 Data Repository by the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University - https://github.com/CSSEGISandData/COVID-19