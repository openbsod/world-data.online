name := "live_data"

version := "0.1"

scalaVersion := "2.12.15"

libraryDependencies += "org.apache.spark" % "spark-sql_2.12" % "3.2.1"
libraryDependencies += "org.apache.spark" % "spark-core_2.12" % "3.2.1"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.2.1" % Test
libraryDependencies += "org.apache.spark" % "spark-streaming_2.12" % "3.2.1"
libraryDependencies += "org.apache.spark" % "spark-kubernetes_2.12" % "3.2.1"
libraryDependencies += "org.apache.spark" % "spark-streaming-kafka-0-10_2.12" % "3.2.1"
libraryDependencies += ("com.datastax.spark" %% "spark-cassandra-connector" % "3.2.0").exclude("io.netty", "netty-handler")
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.1.0"
