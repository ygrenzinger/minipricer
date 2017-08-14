package com.kata.minipricer.service;

import com.kata.minipricer.domain.Product;
import com.kata.minipricer.domain.Slope;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkArgument;

public class PriceForecaster {
    private final OpenDays openDays;
    private final SlopeRandomizer slopeRandomizer;


    public PriceForecaster(OpenDays openDays, SlopeRandomizer slopeRandomizer) {
        this.openDays = openDays;
        this.slopeRandomizer = slopeRandomizer;
    }


    public BigDecimal forecast(Product product, LocalDate forecastDay) {
        checkArgument(forecastDay.isAfter(product.getDate()), "Forecast day must be after product price", forecastDay);

        return openDays.openDays(product.getDate(), forecastDay)
                .map(date -> slopeRandomizer.randomVolatilityForDay())
                .map(slope -> computePriceVariability(product.getVolatility(), slope))
                .reduce(product.getPrice(), BigDecimal::multiply);
    }

    private BigDecimal computePriceVariability(BigDecimal volatility, Slope slope) {
        switch (slope) {
            case UP:
                return BigDecimal.ONE.add(volatility);
            case DOWN:
                return BigDecimal.ONE.subtract(volatility);
            default:
                return BigDecimal.ONE;
        }
    }

    public BigDecimal forecastWithMonteCarlo(Product product, LocalDate forecastDay, int numberOfTrajectory) {
        BigDecimal total = IntStream.range(0, numberOfTrajectory)
                .parallel()
                .mapToObj(i -> forecast(product, forecastDay))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return total.divide(BigDecimal.valueOf(numberOfTrajectory), BigDecimal.ROUND_HALF_UP);
    }
}
