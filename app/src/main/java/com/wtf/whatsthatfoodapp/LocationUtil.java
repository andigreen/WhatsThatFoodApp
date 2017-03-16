package com.wtf.whatsthatfoodapp;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class LocationUtil {

    private static final int[] PLACE_TYPES = {
            Place.TYPE_BAKERY,
            Place.TYPE_BAR,
            Place.TYPE_CAFE,
            Place.TYPE_CONVENIENCE_STORE,
            Place.TYPE_FOOD,
            Place.TYPE_GROCERY_OR_SUPERMARKET,
            Place.TYPE_LIQUOR_STORE,
            Place.TYPE_MEAL_TAKEAWAY,
            Place.TYPE_RESTAURANT,
    };

    private static boolean isRestaurant(Place place) {
        List<Integer> types = place.getPlaceTypes();
        for (int type : PLACE_TYPES) {
            if (types.contains(type)) return true;
        }
        return false;
    }

    /**
     * Returns a list of up to n Place names which are a restaurant type.
     */
    public static List<String> getRestaurants(
            PlaceLikelihoodBuffer likelyPlaces, int n) {
        List<String> names = new ArrayList<>(n);

        Iterator<PlaceLikelihood> iter = likelyPlaces.iterator();
        Place place;
        while (names.size() < n && iter.hasNext()) {
            place = iter.next().getPlace();
            if (isRestaurant(place)) names.add(place.getName().toString());
        }

        return names;
    }

}
