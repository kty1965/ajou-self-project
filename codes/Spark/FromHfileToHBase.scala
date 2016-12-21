package com.zoyi.spark.job.temp

import com.zoyi.spark.helper.HbaseRddHelper
import org.apache.hadoop.hbase.client.Put
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.TableOutputFormat
import org.apache.hadoop.hbase.util.Bytes
import org.apache.hadoop.mapreduce.Job
import org.apache.spark.{SparkConf, SparkContext}
import org.joda.time.format.DateTimeFormat
import org.joda.time.{DateTime, DateTimeZone}

import scala.util.Try

/**
  * Created by huy on 2016. 11. 29..
  */
object FromHfileToHBase {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("FromHfileToHBase").setMaster("yarn-cluster")

    val job = Job.getInstance(HbaseRddHelper.getBaseConfiguration)
    job.getConfiguration.set(TableOutputFormat.OUTPUT_TABLE, "from_hfile_to_hbase_spark")
    job.setOutputFormatClass(classOf[TableOutputFormat[Array[Bytes]]])

    val sc = new SparkContext(sparkConf)
    sc.textFile("hdfs://master.kona.zoyi.co:8020/user/huy/ajou/*.csv")
        .filter(x => x.split(",").length >= 3)
        .flatMap(x => {
          val tokens = x.split(",")
          val dateTime = new DateTime(tokens(0).toLong / 1000, DateTimeZone.forID("Asia/Seoul"))
          val deviceId = tokens(1)

          List(
            (dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd")), deviceId),
            (dateTime.toString(DateTimeFormat.forPattern("yyyy-MM-dd'T'HH")), deviceId)
          )
        }).groupBy(_._1).map(x => (x._1, x._2.groupBy(_._2).size))
        .map(x => {
          val put = new Put(x._1.getBytes)
          put.addColumn("value".getBytes, null, String.valueOf(x._2).getBytes)
          (new ImmutableBytesWritable, put)
        }).saveAsNewAPIHadoopDataset(job.getConfiguration)
  }
}
