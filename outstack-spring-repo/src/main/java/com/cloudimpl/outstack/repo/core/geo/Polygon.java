package com.cloudimpl.outstack.repo.core.geo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class Polygon implements GeoMetry{

    private double[][] points;


}
