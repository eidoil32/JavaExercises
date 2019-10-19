package utils;

import java.util.LinkedList;
import java.util.List;

public class Utilities {
    public static List<String> createSingleItemList(String item) {
        List<String> temp = new LinkedList<>();
        temp.add(item);
        return temp;
    }
}
