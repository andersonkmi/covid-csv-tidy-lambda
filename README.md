# COVID csv files tidy lambda
AWS Lambda for processing and tidying CSV files with COVID data

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