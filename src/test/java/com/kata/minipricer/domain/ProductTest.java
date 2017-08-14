package com.kata.minipricer.domain;

import com.kata.minipricer.service.DaysOff;
import com.kata.minipricer.service.PriceForecaster;
import com.kata.minipricer.service.OpenDays;
import com.kata.minipricer.service.SlopeRandomizer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ProductTest {

    private DaysOff daysOff;
    private SlopeRandomizer slopeRandomizer;
    private PriceForecaster priceForecaster;

    @Before
    public void before() {
        daysOff = mock(DaysOff.class);
        slopeRandomizer = mock(SlopeRandomizer.class);
        Mockito.when(slopeRandomizer.randomVolatilityForDay()).thenReturn(Slope.UP);
        priceForecaster = new PriceForecaster(new OpenDays(daysOff), slopeRandomizer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_create_a_product_with_volatility_less_than_minus_100() {
        Product.of(LocalDate.of(2017,5,24), BigDecimal.ONE, new BigDecimal("-101"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void should_not_create_a_product_with_volatility_more_than_100() {
        Product.of(LocalDate.of(2017,5,24), BigDecimal.ONE, new BigDecimal("101"));
    }

    @Test
    public void should_compute_forecast_after_one_day(){
        LocalDate currentDay = LocalDate.of(2017,5,24);
        Product product = Product.of(currentDay, BigDecimal.ONE, BigDecimal.ONE);
        assertThat(priceForecaster.forecast(product,currentDay.plusDays(1))).isEqualByComparingTo("1.01");
        verify(slopeRandomizer).randomVolatilityForDay();
    }

    @Test
    public void should_compute_forecast_for_multiple_days() {
        LocalDate currentDay = LocalDate.of(2017,5,2);
        Product product = Product.of(currentDay, BigDecimal.ONE, BigDecimal.ONE);
        assertThat(priceForecaster.forecast(product,currentDay.plusDays(3))).isEqualByComparingTo("1.030301");
    }

    @Test
    public void should_compute_forecast_whitout_weekend() {
        LocalDate currentDay = LocalDate.of(2017,5,19);
        LocalDate forecastDay = LocalDate.of(2017,5,24);
        Product product = Product.of(currentDay, BigDecimal.ONE, BigDecimal.ONE);
        assertThat(priceForecaster.forecast(product,forecastDay)).isEqualByComparingTo("1.030301");
    }

    @Test
    public void should_compute_forecast_whitout_weekend_and_daysoff() {
        when(daysOff.isDaysOff(LocalDate.of(2017,5,18))).thenReturn(true);
        when(daysOff.isDaysOff(LocalDate.of(2017,5,22))).thenReturn(true);
        LocalDate currentDay = LocalDate.of(2017,5,17);
        LocalDate forecastDay = LocalDate.of(2017,5,24);
        Product product = Product.of(currentDay, BigDecimal.ONE, BigDecimal.ONE);
        assertThat(priceForecaster.forecast(product,forecastDay)).isEqualByComparingTo("1.030301");
    }

    @Test
    public void should_not_change_price_when_slope_is_flat() {
        Mockito.when(slopeRandomizer.randomVolatilityForDay()).thenReturn(Slope.FLAT);
        LocalDate currentDay = LocalDate.of(2017,5,24);
        Product product = Product.of(currentDay, BigDecimal.ONE, BigDecimal.ONE);
        BigDecimal forecastPrice = priceForecaster.forecast(product,currentDay.plusDays(1));
        assertThat(forecastPrice).isEqualByComparingTo("1");
        verify(slopeRandomizer).randomVolatilityForDay();
    }

    @Test
    public void should_reduce_price_when_slope_is_down() {
        Mockito.when(slopeRandomizer.randomVolatilityForDay()).thenReturn(Slope.DOWN);
        LocalDate currentDay = LocalDate.of(2017,5,24);
        Product product = Product.of(currentDay, BigDecimal.ONE, BigDecimal.ONE);
        BigDecimal forecastPrice = priceForecaster.forecast(product,currentDay.plusDays(1));
        assertThat(forecastPrice).isEqualByComparingTo("0.99");
        verify(slopeRandomizer).randomVolatilityForDay();
    }

    @Test
    public void should_have_correct_monte_carlo() {
        slopeRandomizer = new SlopeRandomizer();
        priceForecaster = new PriceForecaster(new OpenDays(daysOff), slopeRandomizer);
        BigDecimal price = new BigDecimal("100");
        Product product = Product.of(LocalDate.of(2017,6,4), price, BigDecimal.ONE);
        BigDecimal minPrice = new BigDecimal("0.99").pow(5).multiply(price);
        BigDecimal maxPrice = new BigDecimal("1.01").pow(5).multiply(price);

        LocalDate forecastDay = LocalDate.of(2017, 6, 9);
        BigDecimal forecastPrice = priceForecaster.forecastWithMonteCarlo(product, forecastDay, 10000);
        assertThat(forecastPrice).isBetween(minPrice, maxPrice);
    }
}