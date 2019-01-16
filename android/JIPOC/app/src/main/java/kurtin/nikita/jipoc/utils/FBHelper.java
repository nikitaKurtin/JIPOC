package kurtin.nikita.jipoc.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import kurtin.nikita.jipoc.models.Message;

/**
 * Created by Nikita Kurtin on 1/15/19.
 */
public class FBHelper {

    public static final class Keys{
        public static final String BASE_URL  = "https://jipoc-1dc20.firebaseio.com/";
        public static final String PATH      = "l0";
        public static final String CHATS_URL = BASE_URL+"/"+ PATH +".json";
        public static final String IV        = "l0";
        public static final String MSGS      = "l0l";

        private static final String inner    = ",";
        private static final String outer    = ";";
    }

    public static List<Message> parseMessages(String uid, String str){
        String [] msgs = str.split(Keys.outer);
        List<Message> messages = new ArrayList<>();
        for(String msg : msgs){
            if(msg.contains(Keys.inner)){
                String [] msgData = msg.split(Keys.inner);
                messages.add(new Message(msgData[0], msgData[1], msgData[0].equals(uid)));
            }
        }
        return messages;
    }

    //All messages are stored as "[sender1]:[text1];[sender2]:[text2]..."
    public static String stringify(List<Message> messages){
        StringBuilder sb = new StringBuilder();
        for(Message msg : messages){
            sb.append(stringify(msg.getUid(), msg.getText())).append(Keys.outer);
        }
        return sb.toString();
    }

    //Each message is stored as "[uid]:[text]"
    public static String stringify(String uid, String text){
        return uid+Keys.inner+text+Keys.outer;
    }

}