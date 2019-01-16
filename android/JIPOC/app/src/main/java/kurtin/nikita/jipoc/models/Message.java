package kurtin.nikita.jipoc.models;

/**
 * Created by Nikita Kurtin on 1/13/19.
 */
public final class Message {

    final boolean thisUser;
    final String text;
    final String uid;

    public Message(String uid, String text, boolean thisUser) {
        this.thisUser = thisUser;
        this.text = text;
        this.uid = uid;
    }

    public boolean isThisUser() {
        return thisUser;
    }

    public String getText() {
        return text;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public String toString() {
        return uid+":"+text;
    }
}
