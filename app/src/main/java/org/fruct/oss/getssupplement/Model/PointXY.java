package org.fruct.oss.getssupplement.Model;

/**
 * Created by Andrey on 30.03.2016.
 */
public class PointXY {

    public double x;
    public double y;

    public PointXY(double x, double y) {
        this.x = x;
        this.y = y;
    }
    public String toString() {
        return this.x + " " + this.y;
    }
}
