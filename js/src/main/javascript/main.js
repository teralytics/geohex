function findRightLevel(map) {
    var bb = map.getBounds();
    var totalLon = Math.abs(bb.getNorth() - bb.getSouth());
    var level = 1;
    while (terahex.size(level) * 5 > totalLon) { level++; }
    return level;
}

function showZone(map, loc, level) {
    var zone = terahex.zoneByLocation(loc.lng, loc.lat, level);
    var hex = omnivore.wkt
        .parse(zone.wellKnownText)
        .addTo(map);
    var text = 'Code: ' + zone.code;
    text += '<br />Level: ' + zone.level;
    text += '<br />Location: ' + zone.location.lon + ',' + zone.location.lat;
    L.popup()
        .setLatLng(zone.location)
        .setContent(text)
        .openOn(map);
}

function coverBoundingBox(bbox, level) {
    var map = window.map;
    console.log(bbox);
    var [from, to] = bbox;
    var zones = terahex.zonesWithin(from.lng, from.lat, to.lng, to.lat, level);
    zones.forEach(function (z) {
        omnivore.wkt
            .parse(z.wellKnownText)
            .addTo(map);
        console.log(z.code)
    });
    L.rectangle(bbox, {color: "#ff7800", weight: 1}).addTo(map);
    map.fitBounds(bbox, { padding: [20, 20] });
}

function rectangleCoverage(map) {
    var drawnItems = new L.FeatureGroup();
    map.addLayer(drawnItems);

    var drawControl = new L.Control.Draw({
        draw: {
            circle: false,
            rectangle: true,
            marker: false,
            polyline: false,
            polygon: false
        },
        edit: {
            featureGroup: drawnItems
        }
    });
    map.addControl(drawControl);

    map.on('draw:created', function (e) {
        if (e.layerType === 'rectangle') {
            map.addLayer(e.layer);
            coverBoundingBox([
                e.layer.getBounds().getSouthWest(),
                e.layer.getBounds().getNorthEast()],
                findRightLevel(map));
        }
    });
}

window.onload = function() {

    var map = L.map('map');
    window.map = map;
    map.setView([40.7127, -74.0059], 15);

    L.tileLayer('http://{s}.tile.osm.org/{z}/{x}/{y}.png', {
        attribution: '&copy; <a href="http://osm.org/copyright">OpenStreetMap</a> contributors'
    }).addTo(map);

    rectangleCoverage(map);

    function onMapClick(e) {
        var level = findRightLevel(map);
        showZone(map, e.latlng, level);
    }

    map.on('click', onMapClick);

//    coverBoundingBox([{lat: 40.71916022743469, lng: -73.8332748413086},
//                      {lat: 40.74478596665598, lng: -73.76083374023438}], 9);
//
//    coverBoundingBox([{lat: 40.72332345541449, lng: -73.61801147460938},
//                      {lat: 40.81276986422275, lng: -73.35090637207031}], 8);
}