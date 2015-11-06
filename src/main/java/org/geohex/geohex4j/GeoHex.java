/*
 * GeoHex by @sa2da (http://geogames.net) is licensed under Creative Commons BY-SA 2.1 Japan License.
 * GeoHex V3 for Java implemented by @chshii is licensed under Creative Commons BY-SA 2.1 Japan License.
 */

package org.geohex.geohex4j;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static net.teralytics.geohex.GeoHex.*;

public class GeoHex {

    // *** Share with all instances ***
    public static final String h_key = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    public static final double h_base = 20037508.34;
    public static final double h_deg = Math.PI * (30.0 / 180.0);
    public static final double h_k = Math.tan(h_deg);

    // private class

    // public static
    public static Zone getZoneByLocation(double lat, double lon, int level) {
        if (lat < -90 || lat > 90)
            throw new IllegalArgumentException("latitude must be between -90 and 90");
        if (lon < -180 || lon > 180)
            throw new IllegalArgumentException("longitude must be between -180 and 180");
        if (level < 0 || level > 15)
            throw new IllegalArgumentException("level must be between 0 and 15");

        level += 2;
        double h_size = calcHexSize(level);

        XY z_xy = loc2xy(lon, lat);
        double lon_grid = z_xy.x();
        double lat_grid = z_xy.y();
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

        Loc z_loc = xy2loc(h_lon, h_lat);
        double z_loc_x = z_loc.lon();
        double z_loc_y = z_loc.lat();
        if (h_base - h_lon < h_size) {
            z_loc_x = 180;
            long h_xy = h_x;
            h_x = h_y;
            h_y = h_xy;
        }

        StringBuilder h_code = new StringBuilder();
        List<Integer> code3_x = new ArrayList<>();
        List<Integer> code3_y = new ArrayList<>();
        StringBuilder code3 = new StringBuilder();
        StringBuilder code9 = new StringBuilder();
        long mod_x = h_x;
        long mod_y = h_y;


        for (int i = 0; i <= level; i++) {
            double h_pow = Math.pow(3, level - i);
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

        for (int i = 0; i < code3_x.size(); i++) {
            code3.append(code3_x.get(i)).append(code3_y.get(i));
            code9.append(Integer.parseInt(code3.toString(), 3));
            h_code.append(code9);
            code9.setLength(0);
            code3.setLength(0);
        }
        String h_2 = h_code.substring(3);
        int h_1 = Integer.parseInt(h_code.substring(0, 3));
        int h_a1 = (int) Math.floor(h_1 / 30);
        int h_a2 = h_1 % 30;

        return new Zone(z_loc_y, z_loc_x, h_x, h_y, String.valueOf(h_key.charAt(h_a1)) + h_key.charAt(h_a2) + h_2);
    }

    public static Zone getZoneByCode(String code) {
        int level = code.length();
        double h_size = calcHexSize(level);
        double unit_x = 6 * h_size;
        double unit_y = 6 * h_size * h_k;
        long h_x = 0;
        long h_y = 0;
        String h_dec9 = "" + (h_key.indexOf(code.charAt(0)) * 30 + h_key.indexOf(code.charAt(1))) + code.substring(2);
        if (regMatch(h_dec9.charAt(0), INC15) && regMatch(h_dec9.charAt(1), EXC125) && regMatch(h_dec9.charAt(2), EXC125)) {
            if (h_dec9.charAt(0) == '5') {
                h_dec9 = "7" + h_dec9.substring(1, h_dec9.length());
            } else if (h_dec9.charAt(0) == '1') {
                h_dec9 = "3" + h_dec9.substring(1, h_dec9.length());
            }
        }
        int d9xlen = h_dec9.length();
        for (int i = 0; i < level + 1 - d9xlen; i++) {
            h_dec9 = "0" + h_dec9;
            d9xlen++;
        }
        StringBuilder h_dec3 = new StringBuilder();
        for (int i = 0; i < d9xlen; i++) {
            int dec9i = Integer.parseInt("" + h_dec9.charAt(i));
            String h_dec0 = Integer.toString(dec9i, 3);
            if (h_dec0.length() == 1) {
                h_dec3.append("0");
            }
            h_dec3.append(h_dec0);
        }

        List<Character> h_decx = new ArrayList<>();
        List<Character> h_decy = new ArrayList<>();

        for (int i = 0; i < h_dec3.length() / 2; i++) {
            h_decx.add(h_dec3.charAt(i * 2));
            h_decy.add(h_dec3.charAt(i * 2 + 1));
        }

        for (int i = 0; i <= level; i++) {
            double h_pow = Math.pow(3, level - i);
            if (h_decx.get(i) == '0') {
                h_x -= h_pow;
            } else if (h_decx.get(i) == '2') {
                h_x += h_pow;
            }
            if (h_decy.get(i) == '0') {
                h_y -= h_pow;
            } else if (h_decy.get(i) == '2') {
                h_y += h_pow;
            }
        }

        double h_lat_y = (h_k * h_x * unit_x + h_y * unit_y) / 2;
        double h_lon_x = (h_lat_y - h_y * unit_y) / h_k;

        Loc h_loc = xy2loc(h_lon_x, h_lat_y).normalize();
        return new Zone(h_loc.lat(), h_loc.lon(), h_x, h_y, code);
    }


    // private static

    public static String encode(double lat, double lon, int level) {
        return getZoneByLocation(lat, lon, level).code();
    }

    public static Zone decode(String code) {
        return getZoneByCode(code);
    }

    private static final Pattern INC15 = Pattern.compile("[15]");
    private static final Pattern EXC125 = Pattern.compile("[^125]");

    private static boolean regMatch(CharSequence cs, Pattern pat) {
        return pat.matcher(cs).matches();
    }

    private static boolean regMatch(char ch, Pattern pat) {
        return regMatch("" + ch, pat);
    }
}
