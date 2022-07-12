package com.cloudimpl.outstack.repo.core.geo;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public class Polygons implements GeoMetry{

    private double[][][] polygons;

}
