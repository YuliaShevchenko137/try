package ua.sumdu.java.lab2.instant_messenger.processing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ua.sumdu.java.lab2.instant_messenger.api.UserMap;
import ua.sumdu.java.lab2.instant_messenger.api.UserMapParser;
import ua.sumdu.java.lab2.instant_messenger.entities.UserMapImpl;

import java.io.File;
import java.io.IOException;

public final class UserMapParserImpl implements UserMapParser{

    private static final Logger LOGGER = LoggerFactory.getLogger(UserMapParserImpl.class);

    private static UserMapParserImpl instance;

    private UserMapParserImpl() {
    }

    public static UserMapParserImpl getInstance() {
        synchronized (UserMapParserImpl.class) {
            LOGGER.info("Create a new UserMap Parser");
            if (instance == null) {
                instance = new UserMapParserImpl();
            }
            return instance;
        }
    }

    @Override
    public String userMapToJSonString(UserMap userMap) {
        LOGGER.info("Converting a UserMap to a Json String");
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        UserMapImpl newUsers = (UserMapImpl) userMap;
        return gson.toJson(newUsers);
    }

    @Override
    public UserMap jsonStringToUserMap(String jsonString) {
        LOGGER.info("Converting a Json String to a UserMap");
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.setPrettyPrinting().create();
        return gson.fromJson(jsonString, UserMapImpl.class);
    }

    @Override
    public void writeUserMapToFile(String jsonString) {
        try {
            FileUtils.writeStringToFile(new File("src/main/java/resources/friends.json"), jsonString, "UTF-8");
        } catch (IOException e) {
            LOGGER.error("writeUserMapToFile: IOException");
        }
    }
}
