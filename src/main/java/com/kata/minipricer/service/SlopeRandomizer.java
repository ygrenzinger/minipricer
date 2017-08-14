package com.kata.minipricer.service;

import com.kata.minipricer.domain.Slope;

import java.util.Random;

public class SlopeRandomizer {

    private static final Random RANDOM = new Random();

    public Slope randomVolatilityForDay() {
         RANDOM.setSeed(System.nanoTime());
        int value = RANDOM.nextInt(3);
        switch (value) {
            case 0: return Slope.DOWN;
            case 1: return Slope.FLAT;
            default: return Slope.UP;
         }
    }

}
