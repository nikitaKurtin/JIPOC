package kurtin.nikita.jipoc;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

public class MainActivity extends AppCompatActivity {

    private static String fullKey;
    private static String iv;
    private static String uid = "";

    private DatabaseReference db;

    private List<Message> messages = new ArrayList<>();
    private String msgEnc = "";
    private boolean chatOn;

    private RecyclerView messagesList;
    private EditText inputMsg;
    private Button sendBtn;

    private Context self = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendBtn      = findViewById(R.id.sendBtn);
        inputMsg     = findViewById(R.id.inputMsg);
        messagesList = findViewById(R.id.messagesList);


        LinearLayoutManager llm =  new LinearLayoutManager(self);
        llm.setReverseLayout(true);
        messagesList.setLayoutManager(llm);

        db = FirebaseDatabase.getInstance().getReference(FBHelper.Keys.PATH);


    }

    @Override
    protected void onStop() {
        super.onStop();

        //Stop listening when screen closed
        db.removeEventListener(dataChanged);
    }

    public void openChat(View v){
        boolean isStart = (R.id.startChatBtn == v.getId());
        String halfKey  = KeyManager.generateHalfKey();
        View dialogView = LayoutInflater.from(self).inflate(R.layout.dialog_key_exchange, null);

        ((TextView)dialogView.findViewById(R.id.randKey)).setText(halfKey);
        openChat(isStart, dialogView, halfKey);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.closeChat){
            if(chatOn)destroyChat(uid);
            else Alert.toast(self, "Nothing to close");
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_chat, menu);
        return true;
    }

    private View.OnClickListener sendMsg = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String msg = inputMsg.getText().toString();
            if(validMsg(msg)){
                //Update encrypted messages
                msgEnc = Crypto.encryptAes(fullKey, iv, FBHelper.stringify(uid, msg)+FBHelper.stringify(messages));
                initChat(fullKey, uid);
                inputMsg.setText("");
            }
        }
    };

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

    private void openChat(String iv, String fullKey, String uid){
        //Set data for chat
        MainActivity.iv      = iv;
        MainActivity.fullKey = fullKey;
        MainActivity.uid     = uid;

        //Start listening when chat is created
        db.addValueEventListener(dataChanged);
        sendBtn.setOnClickListener(sendMsg);

        //Show relevant views
        chatOn = true;
        toggleChatView();
    }

    private void joinChat(final String fullKey, final String uid){
        final String chatUrl = FBHelper.Keys.BASE_URL+ FBHelper.Keys.PATH+"/"+Crypto.sha256(fullKey)+".json";

        new RestManager.HttpTask(chatUrl, HttpRequest.Method.GET, new RestManager.ResponseHandler(){
            @Override
            public void handle(JSONObject response) {
                if(response == null){
                    Alert.dialog(self, "Failed to join", "Please try again");
                }else{
                    openChat(response.optString(FBHelper.Keys.IV), fullKey, uid);
                }
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
                    openChat(newIv, fullKey, uid);
                }
            }).execute(request.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void destroyChat(final String uid){
        chatOn = false;
        toggleChatView();
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

    private void toggleChatView(){
        sendBtn.setVisibility(chatOn ? View.VISIBLE : View.GONE);
        inputMsg.setVisibility(chatOn ? View.VISIBLE : View.GONE);
        messagesList.setVisibility(chatOn ? View.VISIBLE : View.GONE);
        findViewById(R.id.startChatBtn).setVisibility(chatOn ? View.GONE : View.VISIBLE);
        findViewById(R.id.joinChatBtn).setVisibility(chatOn ? View.GONE : View.VISIBLE);
    }

    private boolean validKey(String givenKey){
        return givenKey != null && givenKey.trim().length() == 8;
    }

    private boolean validMsg(String msg){
        return msg.trim().length() > 0;
    }

}
