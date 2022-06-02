package com.cloudimpl.outstack.repo.core.geo;

import java.util.Arrays;
import java.util.stream.Collectors;

import com.cloudimpl.outstack.repo.RepoException;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKBWriter;

public class GeoUtil {
    public static final WKBWriter WKB_WRITER = new WKBWriter();
    public static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(), 4326);

    public static byte[] convertToGeo(GeoMetry geo) {
        if (geo == null)
            return null;
        if (geo instanceof Point) {
            Point point = (Point) geo;
            return GeoUtil.WKB_WRITER.write(getGeometryForPoint(io.r2dbc.postgresql.codec.Point.of(point.getLat(), point.getLon())));
        } else if (geo instanceof Polygon) {
            Polygon polygon = (Polygon) geo;
            return WKB_WRITER.write(getGeometryForPolygon(getPolygon(convertCoordinates(polygon.getPoints()))));
        }
        throw new RepoException("uknown geometry " + geo.getClass().getName());
    }

    public static Geometry getGeometryForPoint(io.r2dbc.postgresql.codec.Point point) {
        return GeoUtil.GEOMETRY_FACTORY.createPoint(new Coordinate(point.getX(), point.getY()));
    }

    public static Geometry getGeometryForPolygon(io.r2dbc.postgresql.codec.Polygon polygon) {
        return GeoUtil.GEOMETRY_FACTORY
                .createPolygon(polygon.getPoints().stream()
                        .map(point -> new Coordinate(point.getX(), point.getY()))
                        .toArray(Coordinate[]::new));
    }

    public static io.r2dbc.postgresql.codec.Polygon getPolygon(Coordinate[] coordinates) {
        return io.r2dbc.postgresql.codec.Polygon.of(Arrays.stream(coordinates)
                .map(coordinate -> io.r2dbc.postgresql.codec.Point.of(coordinate.x, coordinate.y))
                .collect(Collectors.toList()));
    }

    public static Coordinate[] convertCoordinates(double[][] geometry) {
        return Arrays.stream(geometry)
                .map(points -> new Coordinate(points[0], points[1]))
                .toArray(Coordinate[]::new);
    }
}
