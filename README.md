# COVID csv files tidy lambda
AWS Lambda for processing and tidying CSV files with COVID data

## How to build and deploy/remove the lambda function

In order to build this lambda function, run the following command:

```
$ sbt clean assembly
```

In order to deploy the lambda function, run the following command:

```
$ sls deploy --stage dev
```

In order to remove the lambda function, run the following command:

```
$ sls remove --stage dev
```