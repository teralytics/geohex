package net.teralytics.terahex


object TeraHex {

  implicit val encoding = Encoding.numeric

  implicit val grid: Grid = Grid(300)

  def zoneByLocation(loc: LatLon, level: Int): Zone = Zone(loc, level)

  def encode(loc: LatLon, level: Int): Long = Zone(loc, level).code.toLong

  def decode(code: Long): Zone = encoding.decode(code)

  def size(level: Int): Double = grid.size(level)
}

object SparkSql {
  import org.apache.spark.sql.expressions.UserDefinedFunction
  import org.apache.spark.sql.functions.udf

  def getUdf(level: Int): UserDefinedFunction = udf((lat: Double, lon: Double) => TeraHex.encode(LatLon(Lon(lon),Lat(lat)), level))
}
