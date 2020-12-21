package com.sample

import org.apache.spark.SparkContext
import org.apache.spark.sql.functions.col
import org.apache.spark.sql.{Dataset, SparkSession}
import org.scalatest.{Assertion, MustMatchers}

trait SparkSupport {

  val SPARK_LOG_LEVEL: String = "ERROR"

  implicit lazy val spark: SparkSession = {
    val spark = SparkSession.builder
      .master("local[*]")
      .appName("SparkTestApp")
      .config("spark.driver.host", "localhost")
      .config("spark.sql.shuffle.partitions", "5")
      .config("spark.databricks.delta.snapshotPartitions", "2")
      .config("spark.ui.enabled", "false")
      .config("spark.sql.extensions", "io.delta.sql.DeltaSparkSessionExtension")
      .config("spark.sql.catalog.spark_catalog", "org.apache.spark.sql.delta.catalog.DeltaCatalog")
      .getOrCreate()
    spark.sparkContext.setLogLevel(SPARK_LOG_LEVEL)
    spark
  }

  implicit lazy val sparkContext: SparkContext = {
    spark.sparkContext
  }

  def display[T](ds: Dataset[T]): Unit = {
    println(s"Size: ${ds.count()}")
    println("Schema:")
    ds.printSchema()
    println(s"Showing: ")
    ds.show()
  }
}

//noinspection ScalaStyle
trait SparkSupportMustCompare extends SparkSupport with MustMatchers {

  def compare[T](
                  expected: Dataset[T],
                  actual: Dataset[T],
                  compareNullable: Boolean = true,
                  compareSchema: Boolean = true
                ): Assertion = {
    actual must not be null
    expected must not be null
    if(compareSchema) getSchema(actual, compareNullable) must contain theSameElementsAs getSchema(expected, compareNullable)
    actual.count must be(expected.count)
    actual.collect must contain theSameElementsAs expected.collect
  }

  def compareUnordered[T](
                           expected: Dataset[T],
                           actual: Dataset[T],
                           compareNullable: Boolean = true,
                           compareSchema: Boolean = true)
  : Assertion = {
    actual.columns must contain theSameElementsAs expected.columns
    val cols = expected.columns.map(col)
    compare(expected.select(cols:_*), actual.select(cols:_*), compareNullable, compareSchema)
  }

  private def getSchema[T](ds: Dataset[T], checkNullable: Boolean = true): Seq[Product with Serializable] = {
    ds.schema.map(x => checkNullable match {
      case true => x
      case _ => (x.name, x.dataType)
    })
  }
}
