import org.apache.spark.sql.expressions.Window
import org.apache.spark.sql.functions.{asc, avg, col, count, current_timestamp, dense_rank, desc, explode, max, min, minute, rank, sum, window, year}
import org.apache.spark.sql.types.TimestampType
import org.apache.spark.sql.{DataFrame, Dataset, SparkSession}
import org.apache.log4j.Logger
import org.apache.log4j.Level


object SparkBatchTransactionsMain {

  def main(args:Array[String]):Unit= {


    val master = args(0)
    val kafkaserver = args(1)
    print(kafkaserver)
  //  Logger.getLogger("org").setLevel(Level.OFF)
   // Logger.getLogger("akka").setLevel(Level.OFF)

    val spark: SparkSession = SparkSession.builder()
      .master(master)
      .appName("SparkByExamples.com")
      .getOrCreate()
    // Replace Key with your AWS account key (You can find this on IAM
    spark.sparkContext
      .hadoopConfiguration.set("fs.s3a.access.key", "AKIAIVSS3BUYTCZKSVJQ")
    // Replace Key with your AWS secret key (You can find this on IAM
    spark.sparkContext
      .hadoopConfiguration.set("fs.s3a.secret.key", "zRgVxz0mJ/9R9G94pMuPKSaqFipf4CiVfvKOVE00")
    spark.sparkContext
      .hadoopConfiguration.set("fs.s3a.endpoint", "s3.amazonaws.com")

    //returns DataFrame
    val df:DataFrame = spark.read.schema(TransactionSchema.schema).json("s3a://transactionsproject2020/topics/transactions-raw/").alias("data").cache()


    // Pre process Txs
    val dfTx = TransactionsFuntions.TxPreProcess(df)

    // Pre process inputs
    val dfInputs = TransactionsFuntions.TxColumnsInputs(dfTx)

    // Pre process outputs
    val dfOutputs = TransactionsFuntions.TxColumnsOutputs(dfTx)



    // Txs Metrics by hour
    val groupTxByHour= TransactionsFuntions.GroupTxByMetrics(dfTx,"1 hour")

    // Inputs Metrics by hour
    val groupByHourInputs = TransactionsFuntions.GroupByMetrics(dfInputs,"1 hour")

    // Inputs Metrics by hour
    val groupByHourOutputs = TransactionsFuntions.GroupByMetrics(dfOutputs,"1 hour")

   // Top addrees INPUTS by day
    val topAddressInputsDay = TransactionsFuntions.TopAddress(dfInputs,"1 day")

    // Top addrees OUTPUTS by day
    val topAddressOutputsDay = TransactionsFuntions.TopAddress(dfInputs,"1 day")

    // Top addrees INPUTS all time
    val topAddresOutAll = TransactionsFuntions.TopAddressAll(dfInputs)

    // Top addrees OUT all time
    val topAddresInAll =  TransactionsFuntions.TopAddressAll(dfOutputs)


    // Top Transaction Value
    val topTxValueAll =  TransactionsFuntions.TopTransactionOutValue(dfOutputs)

// Write to Kafka
    TransactionsFuntions.WriteBatchToKafka(groupTxByHour,kafkaserver,"groupTxByHour")
    TransactionsFuntions.WriteBatchToKafka(groupByHourInputs,kafkaserver,"groupByHourInputs")
    TransactionsFuntions.WriteBatchToKafka(groupByHourOutputs,kafkaserver,"groupByHourOutputs")
    TransactionsFuntions.WriteBatchToKafka(topAddressInputsDay,kafkaserver,"topAddressInputsDay")
    TransactionsFuntions.WriteBatchToKafka(topAddressOutputsDay,kafkaserver,"topAddressOutputsDay")
    TransactionsFuntions.WriteBatchToKafka(topAddresOutAll,kafkaserver,"topAddresOutAll")
    TransactionsFuntions.WriteBatchToKafka(topAddresInAll,kafkaserver,"topAddresInAll")
    TransactionsFuntions.WriteBatchToKafka(topTxValueAll,kafkaserver,"topTxValueAll")


    spark.stop()
  }


}
