package system.test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import engine.generated_2.SProgram;
import jakarta.xml.bind.JAXBException;
import system.file.processing.XMLHandler;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Scanner;

public class testManager {
    public static void main(String[] args) throws JAXBException, IOException {
        XMLHandler xmlHandler = new XMLHandler();
        System.out.println("please insert file path: ");
        Scanner scanner = new Scanner(System.in);
        Path filePath = Path.of(scanner.nextLine());
        System.out.println("file path is: " + filePath);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        SProgram program = xmlHandler.unmarshallFile(filePath);
        String json = gson.toJson(program);
        System.out.println(json);
    }
}
