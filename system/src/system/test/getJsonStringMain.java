package system.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.xml.bind.JAXBException;

import java.io.IOException;
import java.util.Map;

public class getJsonStringMain {
    public static void main(String[] args) throws JAXBException, IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Map<String, Integer> ProgArgs = Map.of("x1", 4, "x2", 2);
        String progArgsJson = gson.toJson(ProgArgs);
        System.out.println("Program arguments as JSON string:");
        System.out.println(progArgsJson);
    }
}
