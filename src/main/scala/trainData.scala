/**
  * Created by time on 17/3/3.
  */
import java.sql.ResultSet
import scala.io.Source
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable
import sql._
import SparkCommon._
import com.huaban.analysis.jieba._


object trainData {
  val segmenter = new JiebaSegmenter()
  var word_cnt = 0
  var word_id_map = Map[Int,(String, Int)]()

  def getAimAns(res: ArrayBuffer[mutable.HashMap[String, Any]]) = {
    val sz = res.size
    val ans = ArrayBuffer[mutable.HashMap[String, Any]]()
    for (i <- 0 until sz if (res(i)("type") == "0") ) {
      if (i+1 >= sz || (res(i+1)("type") == "0")) {
        ans += res(i)
      }
    }
    ans
  }

  def cut(line: String, stop_words: Set[String]): Array[String] = {
    val token_list = segmenter.process(line, JiebaSegmenter.SegMode.SEARCH)
    token_list.toArray.map(_.asInstanceOf[SegToken].word).filter(word => !stop_words.contains(word) && word.trim.nonEmpty && word.size != 1)
  }


  def getTrainData() = {
    val stop_words = Source.fromFile("stop_words2").getLines().toSet
    val str =
      """select * from IM where uid in
        |(select uid from
        |   (select uid, sum(type) as typeSum, count(type) as typeCnt from IM group by uid) as tmp
        |   where typeSum*2 != typeCnt)
        |order by uid asc,time asc""".stripMargin
    val res = sql.select(str)
    if (res.nonEmpty) {
      val ans = getAimAns(res)
      val ims = sc.parallelize(ans).map { line =>
        (line("id"), line("uid"), line("type"), line("time"), line("msg"), cut(line("msg").toString, stop_words))
      }.filter(_._6.nonEmpty) //.collect().foreach(println)

      val words_cnt_map = ims.flatMap { line =>
        line._6.map(word => (word, 1))
      }.reduceByKey(_ + _).collect().zipWithIndex.map { case ((word, cnt), idx) =>
        (word, (idx, cnt))
      }.toMap
      word_cnt = words_cnt_map.size
      word_id_map = words_cnt_map.map { case (word, (idx, cnt)) =>
        (idx, (word, cnt))
      }

      val data = ims.map { case (id, uid, msgType, time, msg, msg_words) =>
          val word_id = msg_words.map(word=> words_cnt_map(word)._1)
          val word_cnt =  msg_words.map(word=> words_cnt_map(word)._2.toDouble)
          (id.toString.toLong, uid, msgType, time, msg, msg_words, word_id, word_cnt)
      }
      data
    }
    else {
      println("sql error")
      sc.parallelize(List())
    }
  }
}
