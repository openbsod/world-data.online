import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.from_json
import org.apache.spark.sql.types.{ArrayType, DataTypes, DoubleType, IntegerType, StringType, StructField, StructType, TimestampType}
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010.KafkaUtils
import org.apache.spark.util.LongAccumulator

object Main {

  def main(args: Array[String]): Unit = {

    val r = scala.util.Random
    val groupId = s"stream-checker-v${r.nextInt.toString}"

    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> "ip:9092",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> groupId,
      "auto.offset.reset" -> "earliest",
      "enable.auto.commit" -> (true: java.lang.Boolean),
      "failOnDataLoss" -> (false: java.lang.Boolean)
    )

    val topics = Array("state-events")
    val batchInterval = Seconds(3)
    val batchesToRun = 3

    val schema = new StructType()
      .add("vid", DataTypes.IntegerType)
      .add("tb", DataTypes.IntegerType)
      .add("evt", DataTypes.StringType)
      .add("ts", DataTypes.TimestampType)
      .add("rid", DataTypes.StringType)
      .add("ev", DataTypes.StringType)

    object FinishedBatchesCounter {
      @volatile private var instance: LongAccumulator = _

      def getInstance(sc: SparkContext): LongAccumulator = {
        if (instance == null) {
          synchronized {
            if (instance == null) {
              instance = sc.longAccumulator("FinishedBatchesCounter")
            }
          }
        }
        instance
      }
    }

    val session = SparkSession.builder()
      .appName("live_data")
      .master("local[*]")
      .config("spark.serializer", "org.apache.spark.serializer.KryoSerializer")
      .config("spark.driver.allowMultipleContexts", "true")
      .config("spark.executor.memory", "64g")
      .config("spark.executor.cores", "32")
      .config("spark.default.parallelism", "12")
      .getOrCreate()
    session.sparkContext.setLogLevel("ERROR")

    val ssc = new StreamingContext(session.sparkContext, batchInterval)
    val stream = KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
    val messages = stream.map(record => record.value)
    //messages.print()

    messages.foreachRDD { rdd =>

      val spark = SparkSession.builder.config(rdd.sparkContext.getConf).getOrCreate()
      import spark.implicits._
      val rawDF = rdd.toDF("msg")
      val df = rawDF.select(from_json($"msg", schema) as "data").select("data.*")
      df.cache()

      val finishedBatchesCounter = FinishedBatchesCounter.getInstance(session.sparkContext)

      df.createOrReplaceTempView("msgs")
      spark.sql("select * from msgs").show(10, false)

      if (finishedBatchesCounter.count >= batchesToRun - 1) {
        ssc.stop()
      } else {
        finishedBatchesCounter.add(1)
      }
    }

    ssc.start()
    ssc.awaitTermination()

  }
}
