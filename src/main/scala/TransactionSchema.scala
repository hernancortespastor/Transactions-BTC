import org.apache.spark.sql.types.{ArrayType, IntegerType, LongType, StringType, StructField, StructType}

object TransactionSchema {
  val schema = StructType(
    Array(
      StructField("op", StringType, true),
      StructField("x", StructType(Array(
        StructField("lock_time", IntegerType, true),
        StructField("ver", IntegerType, true) ,
        StructField("size", IntegerType, true) ,
        StructField("inputs", ArrayType(StructType(Array(
          StructField("sequence", LongType, true),
          StructField("prev_out", StructType(Array(
            StructField("spent", StringType, true),
            StructField("tx_index", IntegerType, true),
            StructField("type", IntegerType, true),
            StructField("addr", StringType, true),
            StructField("value", IntegerType, true),
            StructField("n", IntegerType, true),
            StructField("script", StringType, true)


          ))),
          StructField("script", StringType, true)
        )))),
        StructField("time", LongType, true),
        StructField("tx_index", IntegerType, true),
        StructField("vin_sz", IntegerType, true),
        StructField("hash", StringType, true),
        StructField("vout_sz", IntegerType, true),
        StructField("relayed_by", StringType, true),

        StructField("out", ArrayType(StructType(Array(
          StructField("spent", StringType, true) ,
          StructField("tx_index", IntegerType, true),
          StructField("type", IntegerType, true),
          StructField("addr", StringType, true),
          StructField("value", IntegerType, true),
          StructField("n", IntegerType, true),
          StructField("script", StringType, true)
        ))))

      )))
    )
  )
}
