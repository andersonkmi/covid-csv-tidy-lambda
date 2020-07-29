# COVID CSV files tidy and export lambda

This is another project my mine to practice some AWS services (AWS Lambda, S3, Serverless framework, SQS and Athena)

AWS Lambda for processing and tidying CSV files with COVID data to standardize date/time format and extract the desired 
columns to play with.

The source code developed was based on the CSV format provided by the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University. See References section for more information.

## How to build and deploy/remove the CSV tidy lambda function

In order to build this lambda function, run the following command:

```
$ sbt clean assembly
```

In order to deploy the tidy lambda function, run the following command:

```
$ sls deploy --stage dev --config serverless-tidy-lambda.yml
```

In order to remove the tidy lambda function, run the following command:

```
$ sls remove --stage dev --config serverless-tidy-lambda.yml
```

## How to build and deploy/remove the record export lambda function

In order to deploy the export lambda function, run the following command:

```
$ sls deploy --stage dev --config serverless-export-lambda.yml
```

In order to remove the export lambda function, run the following command:

```
$ sls remove --stage dev --config serverless-export-lambda.yml
```

## References

- COVID-19 Data Repository by the Center for Systems Science and Engineering (CSSE) at Johns Hopkins University - https://github.com/CSSEGISandData/COVID-19