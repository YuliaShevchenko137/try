package ua.sumdu.java.lab2.messenger.handler.test;

import static ua.sumdu.java.lab2.messenger.handler.entities.ResponseType.*;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;
import com.tngtech.java.junit.dataprovider.UseDataProvider;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.time.LocalDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ua.sumdu.java.lab2.messenger.entities.*;
import ua.sumdu.java.lab2.messenger.handler.processing.ResponseParsingImpl;
import ua.sumdu.java.lab2.messenger.parsers.XmlParser;
import ua.sumdu.java.lab2.messenger.processing.GroupMapParserImpl;

@RunWith(DataProviderRunner.class)
public class ResponseParsingImplTest {

    ResponseParsingImpl responseParsing;

    @Before
    public void init() {
        responseParsing = new ResponseParsingImpl();
    }

    @DataProvider
    public static Object[][] groupForTest() throws UnknownHostException {
        return RequestParsingImplTest.groupForTest();
    }

    @DataProvider
    public static Object[][] messages() throws UnknownHostException {
        String username = "this_user";
        String testUser = "testUser";
        Message[] messages =    {new Message(testUser, username, "text1", LocalDateTime.now()),
                new Message(testUser, username, "text2", LocalDateTime.now().minusDays(1)),
                new Message(testUser, username, "text3", LocalDateTime.now().minusDays(2)),
                new Message(testUser, username, "text4", LocalDateTime.now().minusDays(3))};
        return new Object[][]{{messages}};
    }

    @Test
    @UseDataProvider("groupForTest")
    public void updatedGroupList(String chatName, UserMapImpl userMap) {
        GroupMapParserImpl groupMapParser = GroupMapParserImpl.getInstance();
        GroupMapImpl groupMap = (GroupMapImpl) groupMapParser.getGroupMap();
        GroupMapImpl groupForRequest = new GroupMapImpl();
        for (User user : userMap.getMap().values()) {
            groupForRequest.addUser(chatName, user);
        }
        String request = UPDATED_GROUP_LIST.getResponseNumber() + "="
            + groupMapParser.groupMapToJSonString(groupForRequest);
        responseParsing.responseParsing(request);
        GroupMapImpl newGroups = (GroupMapImpl) groupMapParser.getGroupMap();
        for (User user : userMap.getMap().values()) {
            groupMap.addUser(chatName, user);
        }
        Assert.assertEquals(RequestParsingImplTest.getMessage(newGroups.toString(),
            groupMap.toString()), newGroups, groupMap);
        groupMap.getMap().remove(chatName);
        groupMapParser.writeGroupMapToFile(groupMapParser.groupMapToJSonString(groupMap));
    }

    @Test
    @UseDataProvider("messages")
    public void requestedMessages(Message[] messages) throws IOException {
        String sender = messages[0].getSender();
        File senderMessages = new File(User.getUrlMessageDirectory() + "/" + sender + ".xml");
        MessageMapImpl messageMap = (MessageMapImpl) XmlParser.INSTANCE.read(senderMessages);
        MessageMapImpl messagesForResponse = new MessageMapImpl();
        for (Message message : messages) {
            messageMap.addMessage(message);
            messagesForResponse.addMessage(message);
        }
        File temp = File.createTempFile("test", "messagesForResponse",
            new File(User.getUrlMessageDirectory()));
        XmlParser.INSTANCE.write(messageMap, temp);
        String response = REQUESTED_MESSAGES.getResponseNumber() + "="
            + XmlParser.INSTANCE.toXml(XmlParser.INSTANCE.getDocument(temp));
        temp.delete();
        responseParsing.responseParsing(response);
        MessageMapImpl newMessageMap = (MessageMapImpl) XmlParser.INSTANCE.read(senderMessages);
        Assert.assertEquals(RequestParsingImplTest.getMessage(newMessageMap.toString(),
            messageMap.toString()), newMessageMap, messageMap);
        senderMessages.delete();
    }

    @Test
    public void addedToGroup() {
        User testUser = User.getCurrentUser();
        GroupMapImpl currentGroups = (GroupMapImpl) GroupMapParserImpl.getInstance().getGroupMap();
        GroupMapImpl newGroup = new GroupMapImpl();
        String chatName = "testGroup";
        currentGroups.addUser(chatName, testUser);
        newGroup.addUser(chatName, testUser);
        String response = ADDED_TO_GROUP.getResponseNumber() + "=" + GroupMapParserImpl.getInstance()
            .groupMapToJSonString(newGroup);
        responseParsing.responseParsing(response);
        GroupMapImpl updatedGroups = (GroupMapImpl) GroupMapParserImpl.getInstance().getGroupMap();
        Assert.assertEquals(RequestParsingImplTest.getMessage(updatedGroups.toString(),    currentGroups.toString()),
            updatedGroups, currentGroups);
        currentGroups.getMap().remove(chatName);
        GroupMapParserImpl.getInstance().writeGroupMapToFile(GroupMapParserImpl.getInstance()
            .groupMapToJSonString(currentGroups));
    }
}