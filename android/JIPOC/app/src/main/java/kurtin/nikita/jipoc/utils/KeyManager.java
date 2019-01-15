package kurtin.nikita.jipoc.utils;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Created by Nikita Kurtin on 1/12/19.
 */
public class KeyManager {

    //Full private key is 128bit (16bytes), therefor half of the length is 8bytes
    private static final int HALF_KEY_LENGTH = 8;

    //Built in randomization(pseudo) mechanism
    private static final SecureRandom SR = new SecureRandom();

    //Not all chars (ASCII or Unicode) are string representable, therefor I use my own chars collection
    //If you aren't sure why I chose only: '<', '>', '?', '!' '+' '@', '*', '%' ,'$' - ask me directly
    private static final String CHARS = "<>?!+@*%$0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    //Generate random String key for given length
    public static String generateHalfKey(){
        return randomChars(HALF_KEY_LENGTH);
    }

    public static String randomChars(int length){
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < length; i++ ){
            sb.append(CHARS.charAt(SR.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

}
