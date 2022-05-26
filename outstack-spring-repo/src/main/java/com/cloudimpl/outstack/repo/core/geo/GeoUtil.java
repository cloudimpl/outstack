package com.cloudimpl.outstack.repo.core.geo;

import com.cloudimpl.outstack.repo.RepoException;

import java.util.Arrays;
import java.util.stream.Collectors;

public class GeoUtil {
    public static String convertToGeo(GeoMetry geo) {
        if (geo == null)
            return null;
        if (geo instanceof Point) {
            Point point = (Point) geo;
            return "'POINT(".concat(String.valueOf(point.getLat())).concat(" ").concat(String.valueOf(point.getLon()))
                    .concat(")'");
        } else if (geo instanceof Polygon) {
            Polygon polygon = (Polygon) geo;
            return "'POLYGON(".concat(Arrays.stream(((Polygon) geo).getPoints())
                            .map(arr -> (String.valueOf(arr[0])).concat(" ")
                                    .concat(String.valueOf(arr[1]))).collect(Collectors.joining(",")))
                    .concat(")'");
        }
        throw new RepoException("uknown geometry " + geo.getClass().getName());
    }
}
