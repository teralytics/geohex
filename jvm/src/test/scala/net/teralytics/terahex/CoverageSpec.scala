package net.teralytics.terahex

import net.teralytics.terahex.Generators._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{FlatSpec, Matchers}

class CoverageSpec extends FlatSpec with PropertyChecks with Matchers with GeometryMatchers {

  implicit val geoGrid = Grid(300)

  "Hexagon coverage of a bounding box" should "gracefully handle locations outside of domain" in
    forAll(locationsOutsideOfDomain, locationsOutsideOfDomain) { (from, to) =>
      val locations = Zone.zonesWithin(from -> to, 3).map(_.location)
      all(locations.map(_.lat.lat)) should fitIntoLatRange
      all(locations.map(_.lon.lon)) should fitIntoLonRange
    }

  it should "cover antarctic" in {
    implicit val enc = Encoding.numeric
    val bbox = LatLon(Lon(-5D), Lat(80D)) -> LatLon(Lon(5D), Lat(89D))
    val zones = Zone.zonesWithin(bbox, 3)
    zones.map(_.code) should contain theSameElementsAs Seq(BigInt(1300808), BigInt(1300840))
  }
}
