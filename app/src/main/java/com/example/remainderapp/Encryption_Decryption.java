package com.example.remainderapp;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Encryption_Decryption {
    static String key = "2222222222222222";
    static String iv = "2222222222222222";

    public static String decrypt(String data){
        String decryptedString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                Base64.Decoder decoder = Base64.getDecoder();
                byte[] encrypted1 = decoder.decode(data);

                Cipher cipher = Cipher.getInstance("AES/CBC/Pkcs7Padding");
                SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
                IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());

                cipher.init(Cipher.DECRYPT_MODE, keyspec, ivspec);

                byte[] original = cipher.doFinal(encrypted1);
                String originalString = new String(original);
                decryptedString = originalString.trim();
            } catch (Exception e) {
                e.printStackTrace();
                decryptedString = e.toString();
            }
        }
        return  decryptedString;
    }

    public static String encrypt(String data){
        String encryptedString = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            try {
                byte[] encval = null;
                Cipher cipher = Cipher.getInstance("AES/CBC/Pkcs7Padding");
                SecretKeySpec keyspec = new SecretKeySpec(key.getBytes(), "AES");
                IvParameterSpec ivspec = new IvParameterSpec(iv.getBytes());
                cipher.init(Cipher.ENCRYPT_MODE, keyspec, ivspec);
                encval = cipher.doFinal(data.getBytes());
                String encryptedValue = Base64.getEncoder().encodeToString(encval);

                encryptedString = encryptedValue;
            } catch (Exception e) {
                encryptedString = e.toString();
            }
        }
        return encryptedString;
    }
}
