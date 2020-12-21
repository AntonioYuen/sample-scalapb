package com.sample

import org.apache.spark.sql.Dataset
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder
import scalapb.spark.Implicits._

object Transform {


  // This automatically imports implicits because I believe the implicits only works on generated messages
  def case1(ds: Dataset[Person]): Dataset[Person] = {
    ds.map(x => x)
  }

  // This does not automatically import implicits because Scaffold is not a generated message
  def case2(ds: Dataset[Scaffold[Person]]): Dataset[Scaffold[Person]] = {
    ds.map(x => x)
  }

  // This does not automatically imports implicits
  def case3(ds: Dataset[Person]): Dataset[Scaffold[Person]] = {
    ds.map(x => Scaffold(1, x))
  }

  // Using an explicit encoder
  def case4(ds: Dataset[Scaffold[Person]]): Dataset[Scaffold[Person]] = {
    ds.map(x => x)(ExpressionEncoder[Scaffold[Person]])
  }

  // Using an explicit encoder
  def case5(ds: Dataset[Person]): Dataset[Scaffold[Person]] = {
    ds.map(x => Scaffold(1, x))(ExpressionEncoder[Scaffold[Person]])
  }
}
