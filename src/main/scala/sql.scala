/**
  * Created by time on 17/2/9.
  */

import java.sql.{Connection, DriverManager, ResultSet}

import conf._

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

object sql {
  //var connection: Connection = null
  //注册Driver
  Class.forName(driver)
  //得到连接
  val connection = DriverManager.getConnection(url, username, password)
  val statement = connection.createStatement


  def update(str: String) = {
    try {
      val num = statement.executeUpdate(str)
    }catch {
      case e: Exception => e.printStackTrace
    }
  }

  def select(str: String) = {
    var ans:ResultSet = null
    val buffer = new ArrayBuffer[mutable.HashMap[String, Any]]()
    try {
      val res = statement.executeQuery(str)
      val meta = res.getMetaData
      val size = meta.getColumnCount
      while(res.next()) {
        val tmp = mutable.HashMap[String, Any]()
        for (i <- 1 to size) {
          tmp += (meta.getColumnLabel(i) -> res.getString(i))
        }
        buffer += tmp
      }
    }catch {
      case e: Exception => println("sql.select error: " + e.printStackTrace)
    }
    buffer
  }


  def close() = {
    //关闭连接，释放资源
    connection.close
  }
}
