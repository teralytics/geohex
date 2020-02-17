package net.teralytics.terahex

import org.apache.spark.sql.api.java.UDF3

object TeraHex {

  implicit val encoding = Encoding.numeric

  implicit val grid: Grid = Grid(300)

  def zoneByLocation(loc: LatLon, level: Int): Zone = Zone(loc, level)

  def encode(loc: LatLon, level: Int): Long = Zone(loc, level).code.toLong

  def decode(code: Long): Zone = encoding.decode(code)

  def size(level: Int): Double = grid.size(level)
}