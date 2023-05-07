package cn.com.auto.thkl.utils;


import android.annotation.SuppressLint;
import android.os.Build;
import android.security.keystore.KeyProperties;

import androidx.annotation.RequiresApi;

import org.spongycastle.jcajce.provider.asymmetric.RSA;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

import kotlin.text.Charsets;

public class CipherUtils {
    public static String publicKeyHexString = "30819f300d06092a864886f70d010101050003818d00308189028181008c85b721d956bb978cb4f23eeb4e08fd266b7b72f5429e67c96736bc09d4280aad2901018b3269a2c84e64054651ade864f1c996aa9977a8d9ab126eb1b63d776d35f7a96277f83abb66a55d70" +
            "c8c5ffee8bcde595f01dc1e4beae5d5297af7e3331dc07bb70f128b41982d060ce84c2a67cc334f565227b9f3ce712c531cc390203010001";


    /**
     * 获取AES密匙
     */
    public static byte[] decryptionRSA(String aesContent) {
        try {
            X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(StringUtils.hexStringToBytes(publicKeyHexString));
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);
            Cipher cipher = Cipher.getInstance("RSA/None/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, publicKey);
            return cipher.doFinal(StringUtils.hexStringToBytes(aesContent));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressLint("GetInstance")
    @RequiresApi(api = Build.VERSION_CODES.O)
    public static byte[] decodeBase64(String content, String aesContent) {
        Cipher cipher;
        try {
            cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            String s = new String(decryptionRSA(aesContent), StandardCharsets.UTF_8);
            Key key = new SecretKeySpec( StringUtils.hexStringToBytes(s),"AES");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return cipher.doFinal(StringUtils.hexStringToBytes(content));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


}
