
import org.apache.log4j.{Level, LogManager}
import org.apache.spark.sql.{Column, Encoders, SparkSession}
import org.apache.spark.sql.types.{ArrayType, DoubleType, IntegerType, LongType, StringType, StructField, StructType, TimestampType}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.expressions.scalalang.typed
import java.sql.Timestamp

import org.apache.spark.sql.streaming.Trigger

/**
 * Stream from Kafka
 */
  object SparkTransactionsMain {

  def main(args: Array[String]) {

    val master = args(0)
    val kafkaserver = args(1)

    val spark = SparkSession
      .builder()
      .appName("SparkTransactionsMain")
      .master(master)
      .getOrCreate()

    import spark.implicits._

    LogManager.getRootLogger.setLevel(Level.WARN)

    val inputDf = spark.readStream.format("kafka")
       .option("kafka.bootstrap.servers", kafkaserver)
      .option("subscribe", "transactions-raw")
      .option("startingOffsets", "earliest")
      .option("failOnDataLoss","false")
      .load()

    val df = inputDf
      .selectExpr("CAST(key AS STRING)", "CAST(value AS STRING)")
      .select(from_json($"value", TransactionSchema.schema).alias("data"))

    // Pre Process Transactions

    val dfTx = TransactionsFuntions.TxPreProcess(df)

    val dfTxTotal = TransactionsFuntions.TxColumns(dfTx)

    // Process Transactions Inputs
    val dfTxInputs = TransactionsFuntions.TxColumnsInputs(dfTx)

    // Process Transactions Outputs
    val dfTxOutputs = TransactionsFuntions.TxColumnsOutputs(dfTx)


    // Write
    val Txs = TransactionsFuntions.WriteStreamingToKafka(dfTxTotal,kafkaserver,"Txs")
    val TxsInputs = TransactionsFuntions.WriteStreamingToKafka(dfTxInputs,kafkaserver,"TxsInputs")
    val TxsOutputs = TransactionsFuntions.WriteStreamingToKafka(dfTxOutputs,kafkaserver,"TxsOutputs")

    spark.streams.awaitAnyTermination()
  }

}