package com.sample

import frameless.TypedEncoder
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import org.apache.spark.sql.{Dataset, Encoder, Row}
import org.scalatest.{FunSpec, MustMatchers}
import scalapb.spark.ProtoSQL
import scalapb.spark.Implicits.{messageTypedEncoder, typedEncoderToEncoder}

class TransformSpec extends FunSpec with MustMatchers with SparkSupport {
  describe("Transform") {

    val persons: Seq[Person] = Seq(Person(
      name = "name",
      age = 1,
      unknownFields = _root_.scalapb.UnknownFieldSet.empty
    ))

    val scaffoldPersons: Seq[Scaffold[Person]] = persons.map(x => Scaffold(1, x))

    implicit val typedEncoder: TypedEncoder[Person] = messageTypedEncoder[Person]
    implicit val encoder: Encoder[Person] = typedEncoderToEncoder

    it("case 1") {

      val df: Dataset[Row] = ProtoSQL.createDataFrame(spark, persons)

      df.show(false)

      val actual: Dataset[Person] = Transform.case1(df.as[Person](encoder))

      actual.show(false)
    }
    it("case 2") {
      // Does not compile
    }
    it("case 3") {
      // Does not compile
    }
    it("case 4") {
      // Fails
      val df: Dataset[Person] = ProtoSQL.createDataFrame(spark, persons).as[Person]
      df.show(false)
      val ds: Dataset[Scaffold[Person]] = df.map(x => Scaffold(1, x))(ExpressionEncoder[Scaffold[Person]])
      ds.show(false)
      val actual: Dataset[Scaffold[Person]] = Transform.case4(ds)
      actual.show(false)
    }
    it("case 5") {
      // Fails
      val df: Dataset[Person] = ProtoSQL.createDataFrame(spark, persons).as[Person]
      df.show(false)
      val actual: Dataset[Scaffold[Person]] = Transform.case5(df)
      actual.show(false)
    }
  }
}
