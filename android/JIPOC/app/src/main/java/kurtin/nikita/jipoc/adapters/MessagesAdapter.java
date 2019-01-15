package kurtin.nikita.jipoc.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import kurtin.nikita.jipoc.models.Message;

/**
 * Created by Nikita Kurtin on 1/13/19.
 */
public class MessagesAdapter extends RecyclerView.Adapter<MessagesAdapter.MsgViewHolder> {

    private Context context;
    private List<Message> messages;

    public MessagesAdapter(Context context, List<Message> messages) {
        this.context  = context;
        this.messages = messages;
    }

    public Message getMessage(int i){
        return messages.get(i);
    }

    @Override
    public void onBindViewHolder(@NonNull MsgViewHolder msgViewHolder, int i) {
        Message msg = getMessage(i);
        View msgView = msgViewHolder.msgView;
        msgView.setBackgroundColor(msg.isThisUser() ? Color.GREEN: Color.GRAY);
        ((TextView)msgView).setText(msg.getText());
    }

    @NonNull
    @Override
    public MsgViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View msgView = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, viewGroup, false);

        return new MsgViewHolder(msgView);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public static class MsgViewHolder extends RecyclerView.ViewHolder{

        private final View msgView;

        public MsgViewHolder(View msgView) {
            super(msgView);
            this.msgView = msgView;
        }


    }
}
