package net.teralytics.geohex

import com.vividsolutions.jts.geom.{GeometryFactory, PrecisionModel}
import com.vividsolutions.jts.io.WKTReader
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class ZoneSpec extends FlatSpec with PropertyChecks with Matchers {

  it should "create a WellKnownText in which the starting point is equal to the ending point" in {
    val zones = GeoHex.getZonesWithin(((51d, 21d),(56d, 22d)), 2)
    val defaultSRID = 4326
    val factory = new GeometryFactory(new PrecisionModel(1e7), defaultSRID)
    val zoneCoords = new WKTReader(factory).read(zones.head.toWellKnownText).getCoordinates
    zoneCoords.head should equal(zoneCoords.last)
  }
}
