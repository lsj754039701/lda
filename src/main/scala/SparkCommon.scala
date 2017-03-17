/**
  * Created by time on 17/1/8.
  */

import org.apache.spark
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf



object SparkCommon {
  val conf = new SparkConf().setAppName("spark").setMaster("local")
  val sc = new SparkContext(conf)
}
