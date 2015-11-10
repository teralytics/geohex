/*
 * GeoHex by @sa2da (http://geogames.net) is licensed under Creative Commons BY-SA 2.1 Japan License.
 * GeoHex V3 for Java implemented by @chshii is licensed under Creative Commons BY-SA 2.1 Japan License.
 */

package org.geohex.geohex4j;

import java.util.ArrayList;
import java.util.List;

import net.teralytics.geohex.Zone;
import net.teralytics.geohex.Loc;
import net.teralytics.geohex.XY;
import net.teralytics.geohex.package$;

public class GeoHex {

    public static final String h_key = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final double h_base = 20037508.34;
    public static final double h_deg = Math.PI * (30.0 / 180.0);
    public static final double h_k = Math.tan(h_deg);

    public static Zone getZoneByLocation(double lat, double lon, int level) {
        if (lat < -90 || lat > 90)
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        if (lon < -180 || lon > 180)
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        if (level < 0 || level > 15)
            throw new IllegalArgumentException("level must be between 0 and 15");

        XY z_xy = package$.MODULE$.loc2xy(lon, lat);
        double lon_grid = z_xy.x();
        double lat_grid = z_xy.y();
        double h_size = package$.MODULE$.calcHexSize(level);
        double unit_x = 6 * h_size;
        double unit_y = 6 * h_size * h_k;
        double h_pos_x = (lon_grid + lat_grid / h_k) / unit_x;
        double h_pos_y = (lat_grid - h_k * lon_grid) / unit_y;
        long h_x_0 = (long) Math.floor(h_pos_x);
        long h_y_0 = (long) Math.floor(h_pos_y);
        double h_x_q = h_pos_x - h_x_0;
        double h_y_q = h_pos_y - h_y_0;
        long h_x = Math.round(h_pos_x);
        long h_y = Math.round(h_pos_y);

        if (h_y_q > -h_x_q + 1) {
            if ((h_y_q < 2 * h_x_q) && (h_y_q > 0.5 * h_x_q)) {
                h_x = h_x_0 + 1;
                h_y = h_y_0 + 1;
            }
        } else if (h_y_q < -h_x_q + 1) {
            if ((h_y_q > (2 * h_x_q) - 1) && (h_y_q < (0.5 * h_x_q) + 0.5)) {
                h_x = h_x_0;
                h_y = h_y_0;
            }
        }

        double h_lat = (h_k * h_x * unit_x + h_y * unit_y) / 2;
        double h_lon = (h_lat - h_y * unit_y) / h_k;

        Loc z_loc = package$.MODULE$.xy2loc(h_lon, h_lat);
        double z_loc_x = z_loc.lon();
        double z_loc_y = z_loc.lat();
        if (h_base - h_lon < h_size) {
            z_loc_x = 180;
            long h_xy = h_x;
            h_x = h_y;
            h_y = h_xy;
        }

        String h_code = encodeToNumeric(level, h_x, h_y);

        String code = topCodeNumToAlpha(h_code.substring(0, 3)) + h_code.substring(3);

        return new Zone(z_loc_y, z_loc_x, h_x, h_y, code);
    }

    private static String encodeToNumeric(int level, long x, long y) {
        List<Integer> code3_x = new ArrayList<>();
        List<Integer> code3_y = new ArrayList<>();
        long mod_x = x;
        long mod_y = y;


        int length = level + 2;
        for (int i = 0; i <= length; i++) {
            double h_pow = Math.pow(3, length - i);
            if (mod_x >= Math.ceil(h_pow / 2)) {
                code3_x.add(2);
                mod_x -= h_pow;
            } else if (mod_x <= -Math.ceil(h_pow / 2)) {
                code3_x.add(0);
                mod_x += h_pow;
            } else {
                code3_x.add(1);
            }
            if (mod_y >= Math.ceil(h_pow / 2)) {
                code3_y.add(2);
                mod_y -= h_pow;
            } else if (mod_y <= -Math.ceil(h_pow / 2)) {
                code3_y.add(0);
                mod_y += h_pow;
            } else {
                code3_y.add(1);
            }
        }

        StringBuilder h_code = new StringBuilder();
        for (int i = 0; i < code3_x.size(); i++) {
            String code3 = code3_x.get(i).toString() + code3_y.get(i).toString();
            Integer code9 = Integer.parseInt(code3, 3);
            h_code.append(code9);
        }
        return h_code.toString();
    }

    private static String topCodeNumToAlpha(String topCodeNum) {
        int h_1 = Integer.parseInt(topCodeNum);
        int h_a1 = (int) Math.floor(h_1 / 30);
        int h_a2 = h_1 % 30;
        return String.valueOf(h_key.charAt(h_a1)) + h_key.charAt(h_a2);
    }
}
