import org.apache.spark.sql.DataFrame
import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{asc, avg, col, count, current_timestamp, dense_rank, desc, explode, max, min, sum, window, year}
import org.apache.spark.sql.types.TimestampType

object TransactionsFuntions {

  // Proccesing raw data
  def TxPreProcess (df: DataFrame): DataFrame = {
    df.filter(col("data.op") === "utx")
      .withColumn("timestamp",(col("data.x.time").cast(TimestampType)))
      .withColumn("size",(col("data.x.size")))
      .withColumn("hash",(col("data.x.hash")))
      .withColumn("inputs", col("data.x.inputs.prev_out"))
      .withColumn("outputs", col("data.x.out"))


  }

  //  Select and process columns
  def TxColumns (df: DataFrame): DataFrame = {
    df.select(col("timestamp"),col("hash"),col("size"))

  }

  //  Select and process columns
  def TxColumnsInputs(df: DataFrame): DataFrame = {
    df.withColumn("explode_inputs", explode(col("inputs")))
      .withColumn("value", (col("explode_inputs.value")/100000000))
      .withColumn("address", (col("explode_inputs.addr")))
      .select(col("timestamp"),col("hash"),col("address"),col("value"))

  }

  //  Select and process columns
  def TxColumnsOutputs(df: DataFrame): DataFrame = {
    df.withColumn("explode_inputs", explode(col("outputs")))
      .withColumn("value", (col("explode_inputs.value")/100000000))
      .withColumn("address", (col("explode_inputs.addr")))
      .select(col("timestamp"),col("hash"),col("address"),col("value"),col("current_timestamp"))

  }

// Group Transactions by windows
  def GroupTxByMetrics (df: DataFrame, windowduration: String): DataFrame = {
    df.groupBy(window(col("timestamp"),windowduration))
      .agg(
        sum(col("size")).alias("total"),
        count(col("hash")).alias("count")
      )
      .sort(col(("window")))

  }
  // Group Transactions by windows
  def GroupTxByMetrics (df: DataFrame, windowduration: String,slideduration :String): DataFrame = {
    df.groupBy(window(col("timestamp"),windowduration,slideduration))
      .agg(
        sum(col("size")).alias("total"),
        count(col("hash")).alias("count")
      )
      .sort(col(("window")))

  }
  // Group Inputs-Outputs by windows

  def GroupByMetrics (df: DataFrame, windowduration: String): DataFrame = {
    df.groupBy(window(col("timestamp"),windowduration))
      .agg(
        sum(col("value")).alias("total")
      )
      .sort(col(("window")))

  }
  // Group Inputs-Outputs by windows

  def GroupByMetrics (df: DataFrame, windowduration: String,slideduration :String): DataFrame = {
    df.groupBy(window(col("timestamp"),windowduration,slideduration))
      .agg(
        sum(col("value")).alias("total")
      )
      .sort(col(("window")))

  }

  // Get Top Address
  def TopAddressAll (df: DataFrame): DataFrame = {
      df.groupBy(current_timestamp().as("current_time"),col("address"))
      .agg(
        sum(col("value")).alias("total"))
      .orderBy(desc("total"))
      .limit(10)

  }

  // Get Top Transaction

  def TopTransactionOutValue (df: DataFrame): DataFrame = {
      df.groupBy(current_timestamp(),col("hash"))
      .agg(
        sum(col("value")).alias("total"))
      .orderBy(desc("total"))
      .limit(10)

  }

  // Get Top Addres by window

  def TopAddress (df: DataFrame, windowduration: String): DataFrame = {
    val w1 = Window.partitionBy(col("window"))
      .orderBy(desc("total"))

    df.groupBy(window(col("timestamp"),windowduration),col("address"))
      .agg(
        sum(col("value")).alias("total"))
      .withColumn("rank", dense_rank().over(w1))
     .filter(col("rank")<11)
      .orderBy(desc("window"),asc("rank"))

  }


  def WriteBatchToKafka (df: DataFrame, kafkaserver: String, topic:String) = {
    df.toJSON.alias("value")
      .write
      .option("checkpointLocation", "/tmp/spark/checkpointLocation/"+topic)
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaserver)
      .option("topic", topic)
      .save()
  }


  def WriteStreamingToKafka (df: DataFrame, kafkaserver: String, topic:String) = {
    df.toJSON.alias("value")
      .writeStream
      .option("checkpointLocation", "/tmp/spark/checkpointLocation/"+topic)
      .format("kafka")
      .option("kafka.bootstrap.servers", kafkaserver)
      .option("topic", topic)
      .start()

  }

}
