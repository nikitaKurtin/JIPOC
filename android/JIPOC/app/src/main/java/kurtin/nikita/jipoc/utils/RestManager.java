package kurtin.nikita.jipoc.utils;


import android.os.AsyncTask;
import android.support.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by Nikita Kurtin on 1/13/19.
 */
public class RestManager {



    public interface ResponseHandler{
        void handle(JSONObject json);
    }

    public static class HttpTask extends AsyncTask<String, Void, JSONObject>{

        private final String url;
        private final HttpRequest.Method method;
        private final ResponseHandler handler;

        public HttpTask(@NonNull String url, HttpRequest.Method method, @NonNull ResponseHandler handler) {
            this.url = url;
            this.method = method;
            this.handler = handler;
        }

        @Override
        protected JSONObject doInBackground(String... data) {
            boolean hasData = (data != null && data.length > 0);
            try{//If everything is fine -> return response as JSON
                return new HttpRequest(url).prepare(method).withData(hasData ? data[0] : "").sendAndReadJSON();
            }catch (IOException | JSONException e){
                //Debug the exception OR rethrow runtime exception -> depends on the goal
                e.printStackTrace();
            }

            return null;//In case of any failure -> return null
        }

        @Override
        protected void onPostExecute(JSONObject json) {
            if(json != null){
                handler.handle(json);
            }
        }
    }



}