package com.cloudimpl.outstack.repo.core.geo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Point implements GeoMetry{
    private double lat;
    private double lon;

    public String toString()
    {
        return "POINT("+lat+","+lon+")";
    }
}
