package net.teralytics.terahex

/**
  * Zone represents a geographic area of a hexagonal geometry. Zone is specified by the size of the root hexagon and
  * a series of tessellation steps for that root hexagon.
  */
case class Zone(rootSize: Double, cells: Seq[Cell]) {

  private[this] val grid = Grid(rootSize)

  val level: Int = cells.length

  /**
    * Size of the side of the hexagon, the same as the hexagon outer radius
    */
  val size: Double = grid.size(level)

  /**
    * Geographic location of the hexagon centroid
    */
  lazy val location: LatLon = LatLon(grid.inverse(cells).toPoint)

  /**
    * The hexagon inner radius in decimal lat/lon degrees.
    */
  def innerRadius: Double = grid.innerRadius(level)

  /**
    * `LatLon` coordinates of the hexagon corners.
    */
  def geometry: Seq[LatLon] = {

    val center = location.toPoint
    val east = Point(size, 0d)
    Iterator.iterate(east)(_.rotate(60d.toRadians))
      .take(6)
      .map(_ + center)
      .map(LatLon.apply)
      .toSeq
  }

  def leftLon = location.lon.lon - size

  def bottomLeftLon = location.lon.lon - size / 2

  def rightLon = location.lon.lon + size

  def bottomLat = geometry.map(_.lat).minBy(_.lat)

  def topLat = geometry.map(_.lat).maxBy(_.lat)

  def moveN: Zone = move(_.moveN)

  def moveS: Zone = move(_.moveS)

  def moveNE: Zone = move(_.moveNE)

  def moveSE: Zone = move(_.moveSE)

  def moveNW: Zone = move(_.moveNW)

  def moveSW: Zone = move(_.moveSW)

  private[this] def move(cellMove: Cell => Cell): Zone =
    if (cells.isEmpty) this
    else {
      val cs = cells.init :+ cellMove(cells.last)
      val loc = copy(cells = cs).location
      Zone(loc, level)(grid)
    }
}

object Zone {

  def apply(location: LatLon, level: Int)(implicit grid: Grid): Zone = {
    val cells = grid.tessellate(location.toPoint.toHex, level)
    Zone(grid.rootSize, cells)
  }

  def apply[Code](code: Code)(implicit encoding: Encoding[Code]): Zone = encoding.decode(code)

  def zonesWithin(boundingBox: (LatLon, LatLon), level: Int)(implicit grid: Grid): Stream[Zone] = {

    val (from, to) = LatLon.mercatorBoundingBox(boundingBox)
    val start = {
      val fromZone = Zone(from, level)
      if (fromZone.bottomLeftLon > from.lon.lon)
        Zone(from.copy(lon = Lon(from.lon.lon - fromZone.size)), level)
      else fromZone
    }

    def moveEast(z: Zone) = Zone(LatLon(Lon(z.location.lon.lon + 1.5 * z.size), from.lat), level)
    val towardsEast = start #:: Stream.iterate(start)(moveEast)
      .tail.takeWhile(z => z.leftLon < to.lon.lon && z.rightLon + z.size < LatLon.maxLon)

    def towardsNorth(z0: Zone) = z0 #:: Stream.iterate(z0)(_.moveN)
      .tail.takeWhile(z => z.bottomLat.lat < to.lat.lat && z.topLat.lat < LatLon.maxMercatorLat)

    for {
      we <- towardsEast
      sn <- towardsNorth(we)
    } yield sn
  }

  def zonesBetween(line: (LatLon, LatLon), level: Int)(implicit grid: Grid): Seq[Zone] = {
    val (from, to) = line
    val size = grid.size(level)
    val a = from.toPoint.toHex.toCell(size)
    val b = to.toPoint.toHex.toCell(size)

    a.pathTo(b)
      .map(_.toHex(size))
      .map(_.toPoint)
      .map(LatLon(_))
      .map(Zone(_, level))
  }
}
