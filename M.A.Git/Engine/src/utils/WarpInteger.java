package utils;

public class WarpInteger {
    public int number;

    public WarpInteger() {
        this.number = 1;
    }

    public void inc() {
        this.number++;
    }

    @Override
    public String toString() {
        return Integer.toString(number);
    }
}
