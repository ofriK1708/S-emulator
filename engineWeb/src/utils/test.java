package utils;

import com.google.gson.Gson;
import dto.server.UpdateUserInfoBody;

public class test {
    public static void main(String[] args) {
        Gson gson = new Gson();
        UpdateUserInfoBody info = new UpdateUserInfoBody("ofrik", "credits", "1000");
        System.out.println(gson.toJson(info));
    }
}
