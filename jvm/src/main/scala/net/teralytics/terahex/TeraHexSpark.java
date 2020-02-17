package net.teralytics.terahex;

import org.apache.spark.sql.api.java.UDF3;

public class TeraHexSpark implements UDF3<Double, Double, Integer, Long> {
    @Override
    public Long call(Double longitude, Double latitude, Integer level) throws Exception {
        return TeraHex.encode(new LatLon(longitude, latitude), level);
    }
}

