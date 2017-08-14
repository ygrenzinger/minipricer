package com.kata.minipricer.domain;

import com.kata.minipricer.service.OpenDays;
import com.kata.minipricer.service.SlopeRandomizer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

import static com.google.common.base.Preconditions.checkArgument;

public class Product {
    private final static int ROUNDING_MODE = BigDecimal.ROUND_HALF_UP;

    private final LocalDate date;
    private final BigDecimal price;
    private final BigDecimal volatility;

    private Product(LocalDate date, BigDecimal price, BigDecimal volatilityInPercent) {
        checkArgument(isPercent(volatilityInPercent.abs()), "Volatility input is not a percent", volatilityInPercent);

        this.date = date;
        this.price = price.setScale(10, ROUNDING_MODE);
        this.volatility = volatilityInPercent.setScale(2, ROUNDING_MODE).divide(new BigDecimal("100"), ROUNDING_MODE);
    }

    public static Product of(LocalDate currentDay, BigDecimal price, BigDecimal volatility) {
        return new Product(currentDay, price, volatility);
    }

    private boolean isPercent(BigDecimal percent) {
        return BigDecimal.ZERO.compareTo(percent) < 0
                && new BigDecimal("100").compareTo(percent) > 0;
    }

    public LocalDate getDate() {
        return date;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getVolatility() {
        return volatility;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(date, product.date) &&
                Objects.equals(price, product.price) &&
                Objects.equals(volatility, product.volatility);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, price, volatility);
    }
}
