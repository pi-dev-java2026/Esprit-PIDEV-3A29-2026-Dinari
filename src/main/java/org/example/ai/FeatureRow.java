package org.example.ai;

public class FeatureRow {
    public final double[] x; // features
    public final int y;      // target 0/1

    public FeatureRow(double[] x, int y) {
        this.x = x;
        this.y = y;
    }
}