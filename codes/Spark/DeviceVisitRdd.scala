package com.zoyi.spark.job.temp

import com.zoyi.spark.helper.HbaseRddHelper
import com.zoyi.spark.models.ShopResolver
import org.apache.hadoop.hbase.client.{Result, Scan}
import org.apache.hadoop.hbase.io.ImmutableBytesWritable
import org.apache.hadoop.hbase.mapreduce.{MultiTableInputFormat, TableInputFormat}
import org.apache.spark.SparkContext

/**
  * Created by huy on 2016. 12. 22..
  */
object DeviceVisitRdd {
  def getScan: Scan = {
    val scan = new Scan
    scan.setCaching(1024)
    scan.setCacheBlocks(false)
    scan
  }

  def getRdd(sc: SparkContext) = {
    val conf = HbaseRddHelper.getBaseConfiguration()
    conf.set(TableInputFormat.INPUT_TABLE, "device_visit")
    conf.set(TableInputFormat.SCAN, HbaseRddHelper.convertScanToString(getScan))

    sc.newAPIHadoopRDD(conf, classOf[TableInputFormat],
      classOf[ImmutableBytesWritable],
      classOf[Result])
  }
}
