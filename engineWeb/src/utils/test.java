package utils;

import jakarta.xml.bind.JAXBException;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class test {
    public static void main(String[] args) throws JAXBException, FileNotFoundException {
        Set<Integer> set = new LinkedHashSet<>();
        set.add(1);
        set.add(3);
        set.add(2);
        set.add(4);

        System.out.println(set);

        // preserves insertion order
        List<Integer> list1 = new ArrayList<>(set);
        System.out.println(list1); // [one, two, three]

        // also preserves order when using streams
        List<Integer> list2 = set.stream().toList(); // Java 16+
        System.out.println(list2); // [one, two, three]
    }
}
