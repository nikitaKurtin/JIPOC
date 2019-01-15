package kurtin.nikita.jipoc.utils;

import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Nikita Kurtin on 1/13/19.
 */
public class Crypto {

    public static String sha256(String str){
        if(str != null){
            try {
                MessageDigest md = MessageDigest.getInstance("SHA-256");
                byte[] hash = md.digest(str.getBytes(StandardCharsets.UTF_8));
                return Base64.encodeToString(hash, Base64.NO_WRAP).replace('/','0');
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    //Encrypt with AES & encode to Base64
    public static String encryptAes(String key, String iv, String msg){
        try {
            Cipher cipher = getCipher();
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(key), getIV(iv));
            return Base64.encodeToString(cipher.doFinal(msg.getBytes()), Base64.NO_WRAP);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //Decrypt from AES & decode from Base64
    public static String decryptAes(String key, String iv, String msg){
        try {
            Cipher cipher = getCipher();
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(key), getIV(iv));
            return new String(cipher.doFinal(Base64.decode(msg.getBytes(), Base64.NO_WRAP)));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static IvParameterSpec getIV(String iv){
        return new IvParameterSpec(iv.getBytes());
    }

    private static SecretKeySpec getSecretKey(String key){
        return new SecretKeySpec(key.getBytes(), "AES");
    }

    //Advance Encryption Standard with Cypher Block Chaining algorithm padded by PKCS#7
    //If you aren't sure what that means - ask me directly
    private static Cipher getCipher() throws NoSuchPaddingException, NoSuchAlgorithmException {
        return Cipher.getInstance("AES/CBC/PKCS7PADDING");
    }

}
