package com.trinus.repositori.api.dto;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Model class to match with the Google Directions API Response and use GSON to fill it.
 *
 * @author hetorres
 */
public class DirectionsDTO {
    List<Route> routes;

    public List<Route> getRoutes() {
        return routes;
    }

    public class Route {
        @SerializedName("overview_polyline")
        OverviewPolyline polylines;
        List<Leg> legs;

        public OverviewPolyline getPolylines() {
            return polylines;
        }

        public List<Leg> getLegs() {
            return legs;
        }
    }

    public class OverviewPolyline {
        String points;

        public String getPoints() {
            return points;
        }
    }

    public class Leg {
        List<Step> steps;

        public List<Step> getSteps() {
            return steps;
        }
    }

    public class Step {
        @SerializedName("start_location")
        StepLocation start;
        @SerializedName("end_location")
        StepLocation end;
        OverviewPolyline polyline;

        public StepLocation getStartLocation() {
            return start;
        }

        public StepLocation getEndLocation() {
            return end;
        }

        public OverviewPolyline getPolyline() {
            return polyline;
        }
    }

    public class StepLocation {
        double lat;
        double lng;

        public double getLat() {
            return lat;
        }

        public double getLng() {
            return lng;
        }
    }

}
