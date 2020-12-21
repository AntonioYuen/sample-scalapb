package com.sample

case class Scaffold[T](
  _partition_id: Int,
  event: T
)
