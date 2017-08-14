package com.kata.minipricer.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class OpenDays {

    private final DaysOff daysOff;

    public OpenDays(DaysOff daysOff) {
        this.daysOff = daysOff;
    }

    public Stream<LocalDate> openDays(LocalDate startDate, LocalDate forecastDate) {
        int numberOfDays = (int) ChronoUnit.DAYS.between(startDate, forecastDate);
        return IntStream.range(1, numberOfDays + 1)
                .mapToObj(startDate::plusDays)
                .filter(day -> !isWeekend(day) && !daysOff.isDaysOff(day));
    }

    private boolean isWeekend(LocalDate day) {
        return day.getDayOfWeek() == DayOfWeek.SATURDAY || day.getDayOfWeek() == DayOfWeek.SUNDAY;
    }
}
