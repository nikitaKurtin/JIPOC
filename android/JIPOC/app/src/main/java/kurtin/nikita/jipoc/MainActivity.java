package kurtin.nikita.jipoc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import kurtin.nikita.jipoc.adapters.MessagesAdapter;
import kurtin.nikita.jipoc.models.Message;
import kurtin.nikita.jipoc.utils.Crypto;
import kurtin.nikita.jipoc.utils.Alert;
import kurtin.nikita.jipoc.utils.FBHelper;
import kurtin.nikita.jipoc.utils.HttpRequest;
import kurtin.nikita.jipoc.utils.KeyManager;
import kurtin.nikita.jipoc.utils.RestManager;

public class MainActivity extends CoreActivity {

    private static String fullKey;
    private static String iv;
    private static String uid = "";

    private DatabaseReference db;

    private List<Message> messages = new ArrayList<>();
    private String msgEnc = "";

    private RecyclerView messagesList;
    private EditText inputMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputMsg = findViewById(R.id.inputMsg);
        messagesList = findViewById(R.id.messagesList);
        messagesList.setLayoutManager(new LinearLayoutManager(self));

        db = FirebaseDatabase.getInstance().getReference(FBHelper.Keys.PATH);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop listening when screen disappears
        db.removeEventListener(dataChanged);
    }

    public void sendMsg(View v){
        String msg = inputMsg.getText().toString();
        if(validMsg(msg)){
            //Update encrypted messages
            msgEnc = Crypto.encryptAes(fullKey, iv, FBHelper.stringify(messages)+FBHelper.stringify(uid, msg));
            initChat(fullKey, uid);
            inputMsg.setText("");
        }
    }

    public void openChat(View v){
        boolean isStart       = (v.getId() == R.id.startChatBtn);
        String halfKey        = KeyManager.generateHalfKey();
        final View dialogView = LayoutInflater.from(self).inflate(R.layout.dialog_key_exchange, null);

        ((TextView)dialogView.findViewById(R.id.randKey)).setText(halfKey);
        openChat(isStart, dialogView, halfKey);
        toggleChat(v);
    }

    public void toggleChat(View v){
        boolean closeChat = (v.getId() == R.id.closeChatBtn);

        if(closeChat)destroyChat(uid);

        findViewById(R.id.closeChatBtn).setVisibility(closeChat ? View.GONE : View.VISIBLE);
        findViewById(R.id.startChatBtn).setVisibility(closeChat ? View.VISIBLE : View.GONE);
        findViewById(R.id.joinChatBtn).setVisibility(closeChat ? View.VISIBLE : View.GONE);
    }

    public void updateScreen(){
        messagesList.setVisibility(View.VISIBLE);
        messagesList.post(new Runnable() {
            @Override
            public void run() {
                messagesList.setAdapter(new MessagesAdapter(self, messages));
            }
        });
    }

    private ValueEventListener dataChanged = new ValueEventListener(){
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            DataSnapshot chat = dataSnapshot.child(Crypto.sha256(fullKey));
            if(chat.hasChild(FBHelper.Keys.MSGS)){
                msgEnc = chat.child(FBHelper.Keys.MSGS).getValue(String.class);//Store encrypted copy
                messages = FBHelper.parseMessages(uid, Crypto.decryptAes(fullKey, iv, msgEnc));
                updateScreen();
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError e) {
            e.toException().printStackTrace();
        }
    };

    private void openChat(final boolean isStart, final View dialogView, final String selfKey){
        new AlertDialog.Builder(self)
                .setTitle(isStart ? R.string.start_chat : R.string.join_chat)
                .setView(dialogView).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                String givenKey = ((EditText)dialogView.findViewById(R.id.inputKey)).getText().toString();
                if(validKey(givenKey)){
                    if(isStart){//Start a new chat
                        initChat(selfKey + givenKey, "s"+KeyManager.randomChars(7));
                    }else{//Join existing chat
                        joinChat(givenKey + selfKey, "j"+KeyManager.randomChars(7));
                    }
                }else{
                    ((ViewGroup)dialogView.getParent()).removeView(dialogView);
                    openChat(isStart, dialogView, selfKey);
                }
            }
        }).show();
    }

    private void handleChat(String iv, String fullKey, String uid){
        MainActivity.iv      = iv;
        MainActivity.fullKey = fullKey;
        MainActivity.uid     = uid;
        //Start listening when chat is created
        db.addValueEventListener(dataChanged);
    }

    private void joinChat(final String fullKey, final String uid){
        final String chatUrl = FBHelper.Keys.BASE_URL+ FBHelper.Keys.PATH+"/"+Crypto.sha256(fullKey)+".json";

        new RestManager.HttpTask(chatUrl, HttpRequest.Method.GET, new RestManager.ResponseHandler(){
            @Override
            public void handle(JSONObject response) {
                handleChat(response.optString(FBHelper.Keys.IV), fullKey, uid);
            }
        }).execute();
    }

    private void initChat(final String fullKey, final String uid){
        try {
            final String auth    = Crypto.sha256(fullKey);
            final String newIv   = (iv == null ? KeyManager.randomChars(16) : iv);
            JSONObject request   = new JSONObject().put(auth, new JSONObject().put(FBHelper.Keys.IV, newIv).put(FBHelper.Keys.MSGS, msgEnc));

            new RestManager.HttpTask(FBHelper.Keys.CHATS_URL, HttpRequest.Method.PUT, new RestManager.ResponseHandler(){
                @Override
                public void handle(JSONObject response) {
                    handleChat(newIv, fullKey, uid);
                }
            }).execute(request.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void destroyChat(final String uid){
        if(uid.startsWith("s")){
           try {
               JSONObject request = new JSONObject().put(KeyManager.randomChars(4), KeyManager.randomChars(4));
               new RestManager.HttpTask(FBHelper.Keys.CHATS_URL, HttpRequest.Method.PUT, new RestManager.ResponseHandler() {
                   @Override
                   public void handle(JSONObject response) {
                       msgEnc = "";
                       messages.clear();
                       updateScreen();
                       Alert.toast(self, "Chat Deleted");
                   }
               }).execute(request.toString());
           }catch (JSONException e){}
        }
    }

    private boolean validKey(String givenKey){
        return givenKey != null && givenKey.trim().length() == 8;
    }

    private boolean validMsg(String msg){
        return msg.trim().length() > 0;
    }

}
