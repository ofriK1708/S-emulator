package utils;

import com.google.gson.Gson;
import dto.server.UpdateUserInfoBody;
import jakarta.xml.bind.JAXBException;

import java.io.FileNotFoundException;

public class test {
    public static void main(String[] args) throws JAXBException, FileNotFoundException {
        Gson gson = new Gson();
        UpdateUserInfoBody info = new UpdateUserInfoBody("ofrik", "credits", "1000");
        System.out.println(gson.toJson(info));
    }
}
