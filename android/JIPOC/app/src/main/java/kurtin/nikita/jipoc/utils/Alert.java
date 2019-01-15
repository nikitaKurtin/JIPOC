package kurtin.nikita.jipoc.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.widget.Toast;

import kurtin.nikita.jipoc.R;

/**
 * Created by Nikita Kurtin on 1/12/19.
 */
public class Alert {

    public static void toast(Context context, String txt){
        Toast.makeText(context, txt, Toast.LENGTH_LONG).show();
    }

    public static void dialog(Context context, String ttl){
        new AlertDialog.Builder(context).setTitle(ttl).setPositiveButton(R.string.ok, null).show();
    }

    public static void dialog(Context context, String ttl, String msg){
        new AlertDialog.Builder(context).setTitle(ttl).setMessage(msg).setPositiveButton(R.string.ok, null).show();
    }

}
