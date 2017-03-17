name := "knn"

version := "1.0"

scalaVersion := "2.11.1"

resolvers ++= Seq(
  "Weidian Maven" at "https://nexus.vdian.net"
)

resolvers ++= Seq(
  "Nexus" at "http://nexus.vdian.net/repository/maven-all/"
)

libraryDependencies += "org.apache.spark" %% "spark-core" % "2.0.2" % "provided"

libraryDependencies += "org.apache.spark" %% "spark-mllib" % "2.0.2" % "provided"

libraryDependencies += "com.huaban" % "jieba-analysis" % "1.0.2"

libraryDependencies += "com.huaban" % "jieba-analysis" % "1.0.2"
