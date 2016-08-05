package net.teralytics.geohex

import org.scalatest.{WordSpec, Matchers}

class BoundingCircleSpec extends WordSpec with Matchers {


  "GeoHex" should {
    "return zones for a bounding circle" in {

      val bc = BoundingCircle(Loc(52.183603, 21.001936), radiusDeg = 0.0007)
      val zones = GeoHex.getZonesWithin(bc, 9)
      zones.map(_.code) should contain theSameElementsAs List("QD722308776", "QD722332110", "QD722332111")
    }

    "return less zones for bounding circle than box in given example" in {

      val bc = BoundingCircle(Loc(52.183603, 21.001936), radiusDeg = 0.0007)

      val zonesInCircle = GeoHex.getZonesWithin(bc, 9).toSet
      val zonesInBox = GeoHex.getZonesWithin(((bc.minLatitude, bc.minLongitude), (bc.maxLatitude, bc.maxLongitude)), 9).toSet

      zonesInCircle.forall(zonesInBox.contains) shouldBe true
      zonesInCircle.size should be < zonesInBox.size
    }
  }
}
