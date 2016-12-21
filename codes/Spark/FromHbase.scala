package com.zoyi.spark.job.temp

import com.zoyi.spark.Rdd.SessionRdd
import com.zoyi.spark.models.ShopResolver
import org.apache.spark.{SparkConf, SparkContext}

/**
  * Created by huy on 2016. 12. 22..
  */
object FromHbase {
  def main(args: Array[String]): Unit = {
    val sparkConf = new SparkConf().setAppName("DeviceVisit Spark TEST")

    val sc = new SparkContext(sparkConf)

    val devices = DeviceVisitRdd.getRdd(sc).count()
    println(devices)
  }
}
