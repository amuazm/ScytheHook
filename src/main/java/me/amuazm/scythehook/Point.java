package me.amuazm.scythehook;

import org.bukkit.Location;
import org.bukkit.util.Vector;

public class Point {
    private Location current;
    private Location old;

    public Point(Location current, Location old) {
        this.current = current;
        this.old = old;
    }

    public Location getCurrent() {
        return current;
    }

    public void setCurrent(Location current) {
        this.current = current;
    }

    public double getCurrentX() {
        return current.getX();
    }

    public void setCurrentX(double d) {
        current.setX(d);
    }

    public double getCurrentY() {
        return current.getY();
    }

    public void setCurrentY(double d) {
        current.setY(d);
    }

    public double getCurrentZ() {
        return current.getZ();
    }

    public void setCurrentZ(double d) {
        current.setZ(d);
    }

    public Location getOld() {
        return old;
    }

    public void setOld(Location old) {
        this.old = old;
    }

    public double getOldX() {
        return old.getX();
    }

    public void setOldX(double d) {
        old.setX(d);
    }

    public double getOldY() {
        return old.getY();
    }

    public void setOldY(double d) {
        old.setY(d);
    }

    public double getOldZ() {
        return old.getZ();
    }

    public void setOldZ(double d) {
        old.setZ(d);
    }

    /*
    Returns the vector going from Old to Current
     */
    public Vector getVector() {
        return current.clone().subtract(old.clone()).toVector();
    }

    public String toString() {
        String s = "";
        s += "Old: " + dp2(old.getX()) + " " + dp2(old.getY()) + " " + dp2(old.getZ());
        s += "\nCurrent: " + dp2(current.getX()) + " " + dp2(current.getY()) + " " + dp2(current.getZ());
        return s;
    }

    public double dp2(double d) {
        return Math.round(d*100)/100.0;
    }
}
