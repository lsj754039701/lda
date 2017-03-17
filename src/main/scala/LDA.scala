/**
  * Created by time on 17/3/14.
  */
import org.apache.spark.mllib.clustering.LDA
import org.apache.spark.mllib.clustering.DistributedLDAModel
import org.apache.spark.mllib.linalg.Vectors

import scala.collection.mutable.{ArrayBuffer, Map, PriorityQueue}
import trainData._
import SparkCommon._
import scala.collection.mutable

import java.io._

object LDA {
  def main(args: Array[String]): Unit = {
    // 输入的文件每行用词频向量表示一篇文档
    val Array(topic_doc_output, topic_word_output) = args
    val ims = getTrainData()
    val K = 80
    val key_word_cnt = 6
    val ims_map = ims.map { case (id, uid, msgType, time, msg, msg_words, word_id, word_cnt) =>
      (id, msg)
    }.collect().toMap

    val train = ims.map { line =>
      (line._1, Vectors.sparse(word_cnt, line._7, line._8))
    }.cache()

    val ldaModel = new LDA().setK(K).run(train)
    val distLDAModel = ldaModel.asInstanceOf[DistributedLDAModel]
    // 划分类别
    val topicPerdoc = distLDAModel.topTopicsPerDocument(1).map { line =>
      (line._2(0), line._1)
    }.groupByKey().map { line =>
      (line._1, line._2.toArray.map{ ims_map(_) })
    }

    // 提取关键字
    val clu = topicPerdoc.map(_._1).collect()
    val topics = ldaModel.topicsMatrix;
    val topic_word = Map[Int, ArrayBuffer[Int]]()
    implicit val ord:Ordering[(Int, Double)] = Ordering.by(_._2)
    for (topic <- clu) {
      val que = mutable.PriorityQueue[(Int, Double)]()
      for (word <- Range(0, ldaModel.vocabSize)) {  // print(" " + topics(word, topic));
        que.enqueue((word, topics(word, topic)))
      }
      val min_sz = math.min(6, que.size)
      topic_word += (topic -> ArrayBuffer[Int]())
      for (i <- Range(0, min_sz)) {
        val elem = que.dequeue()
        topic_word(topic).append(elem._1)
      }
    }

    val topic_word_rdd = sc.parallelize(topic_word.toSeq).sortBy(_._1).map { case (topic, wordIDs) =>
      val words = wordIDs.map(word_id_map(_)._1)
      topic + " : " + words.mkString(" ")
    }.saveAsTextFile(topic_word_output)
    topicPerdoc.map{line =>
      "type: " + line._1 + " ,size= " + line._2.size  + "\n" + line._2.mkString("\n") + "\n"
    }.saveAsTextFile(topic_doc_output)

    val typeInfo = topicPerdoc.collect().foreach{line =>
      val info = "type: " + line._1 + " ,size= " + line._2.size  + "\n" + line._2.mkString("\n") + "\n"
      val writer = new PrintWriter(new File("/data/resys/lsj/work/lda/typeInfo" + "/type%d.txt".format(line._1) ))
      writer.write(info)
      writer.close()
    }
    topicPerdoc.collect().sortBy(_._1).foreach(line => println("type: " + line._1 + " , " + line._2.size))

    println("clust size: " + clu.size)
    println("topic_word: " + topic_word.size)
    /*
    val tmp_col = topicPerdoc.collect()
    for ((topic, im) <- tmp_col) {
      println("topic : " + topic)
      for (word <- topic_word(topic)) {
        print(word_id_map(word)._1 + " ")
        println("---")
      }
      im.foreach(println)
      println("**************************")
    }
    */
  }
}
