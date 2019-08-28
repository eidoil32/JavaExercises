package utils;

public class WarpInteger {
    public int number;

    public WarpInteger() {
        this.number = 1;
    }

    public WarpInteger(int number) {
        this.number = number;
    }

    public void inc() {
        this.number++;
    }

    public int getValue() {
        return number;
    }

    public void add(int value) {
        this.number += value;
    }

    @Override
    public String toString() {
        return Integer.toString(number);
    }
}
