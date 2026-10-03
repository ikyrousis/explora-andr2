package com.example.andr2project;

import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

import com.google.common.collect.Maps;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    public final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;


    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    public Boolean calculateDistanceInKilometer(double currentUserLat, double currentUserLng,
                                                double otherUserLat, double otherUserLng) {

        double latDistance = Math.toRadians(currentUserLat - otherUserLat);
        double lngDistance = Math.toRadians(currentUserLng - otherUserLng);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(currentUserLat)) * Math.cos(Math.toRadians(otherUserLat))
                * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = (double) (AVERAGE_RADIUS_OF_EARTH_KM * c);
        //if distance is less than 600 meters return true otherwise false.
        System.out.println(distance);
        if(distance <= 0.6){
            return true;
        }
        return false;
    }


    @Test
    public void CalculateDistance_Within600M_ReturnsTrue(){
        assertTrue(calculateDistanceInKilometer(51.427790, 5.457276, 51.428121, 5.457886));
    }

    @Test
    public void CalculateDistance_Within600M_ReturnsFalse(){
        assertFalse(calculateDistanceInKilometer(51.427790, 5.457276, 51.437917, 5.468981));
    }

}