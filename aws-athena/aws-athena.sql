CREATE EXTERNAL TABLE `covid`(
  `country` string COMMENT 'from deserializer',
  `stateprovince` string COMMENT 'from deserializer',
  `lastupdate` string COMMENT 'from deserializer',
  `confirmed` bigint COMMENT 'from deserializer',
  `deaths` bigint COMMENT 'from deserializer',
  `recovered` bigint COMMENT 'from deserializer')
PARTITIONED BY (
  `year` string,
  `month` string,
  `day` string)
ROW FORMAT SERDE
  'org.openx.data.jsonserde.JsonSerDe'
WITH SERDEPROPERTIES (
  'paths'='confirmed,country,deaths,lastUpdate,recovered,stateProvince')
STORED AS INPUTFORMAT
  'org.apache.hadoop.mapred.TextInputFormat'
OUTPUTFORMAT
  'org.apache.hadoop.hive.ql.io.HiveIgnoreKeyTextOutputFormat'
LOCATION
  's3://org.codecraftlabs.test/stage001/'
TBLPROPERTIES (
  'classification'='json',
  'compressionType'='none',
  'typeOfData'='file')