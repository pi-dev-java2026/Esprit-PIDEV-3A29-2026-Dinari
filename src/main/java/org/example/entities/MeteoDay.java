package org.example.entities;

import java.time.LocalDate;

public class MeteoDay {
    private final LocalDate date;
    private final double tempMax;
    private final double rainMm;

    public MeteoDay(LocalDate date, double tempMax, double rainMm) {
        this.date = date;
        this.tempMax = tempMax;
        this.rainMm = rainMm;
    }

    public LocalDate getDate() { return date; }
    public double getTempMax() { return tempMax; }
    public double getRainMm() { return rainMm; }
}