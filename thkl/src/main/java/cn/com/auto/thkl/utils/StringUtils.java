package cn.com.auto.thkl.utils;

import android.text.TextUtils;

import com.stardust.autojs.runtime.api.AppUtils;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;

public class StringUtils {

    public static String hex2Binary(String hexStr) {
        StringBuilder binaryStr = new StringBuilder();
        for (int i = 0; i < hexStr.length(); i++) {
            binaryStr.append(hex2Binary(hexStr.charAt(i)));
        }
        return binaryStr.toString();
    }

    public String convertStringToHex(String str) {

        char[] chars = str.toCharArray();

        StringBuffer hex = new StringBuffer();
        for (int i = 0; i < chars.length; i++) {
            hex.append(Integer.toHexString((int) chars[i]));
        }

        return hex.toString();
    }
    public static String replaceAutoJsValue(String str){
        return str.replace("\"","");
    }
    public static String ofString(String str){
        return "\""+str+"\"";
    }

    public static String convertHexToString(String hex) {

        StringBuilder sb = new StringBuilder();
        StringBuilder temp = new StringBuilder();

        //49204c6f7665204a617661 split into two characters 49, 20, 4c...
        for (int i = 0; i < hex.length() - 1; i += 2) {

            //grab the hex in pairs
            String output = hex.substring(i, (i + 2));
            //convert hex to decimal
            int decimal = Integer.parseInt(output, 16);
            //convert the decimal to character
            sb.append((char) decimal);

            temp.append(decimal);
        }

        return sb.toString();
    }


    public static byte[] toByteArray(String hexString) {
        hexString = hexString.toLowerCase();
        final byte[] byteArray = new byte[hexString.length() / 2];
        int k = 0;
        for (int i = 0; i < byteArray.length; i++) {//因为是16进制，最多只会占用4位，转换成字节需要两个16进制的字符，高位在先
            byte high = (byte) (Character.digit(hexString.charAt(k), 16) & 0xff);
            byte low = (byte) (Character.digit(hexString.charAt(k + 1), 16) & 0xff);
            byteArray[i] = (byte) (high << 4 | low);
            k += 2;
        }
        return byteArray;
    }

    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(bytes.length() / 2);
        //将每2位16进制整数组装成一个字节
        String hexString = "0123456789abcdef";
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString.indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    private static String hex2Binary(char c) {
        switch (c) {
            case '0':
                return "0000";
            case '1':
                return "0001";
            case '2':
                return "0010";
            case '3':
                return "0011";
            case '4':
                return "0100";
            case '5':
                return "0101";
            case '6':
                return "0110";
            case '7':
                return "0111";
            case '8':
                return "1000";
            case '9':
                return "1001";
            case 'a':
            case 'A':
                return "1010";
            case 'b':
            case 'B':
                return "1011";
            case 'c':
            case 'C':
                return "1100";
            case 'd':
            case 'D':
                return "1101";
            case 'e':
            case 'E':
                return "1110";
            case 'f':
            case 'F':
                return "1111";
            default:
                return null;
        }
    }

    public static int byte2float(byte[] b, int index) {
        int l;
        l = b[index + 0];
        l &= 0xff;
        l |= ((long) b[index + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[index + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[index + 3] << 24);
        return l;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if ((hexString == null) || (hexString.equals(""))) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; ++i) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[(pos + 1)]));
        }
        return d;
    }

    // 字节数组转换成16进制串
    public static String byte2HexStr(byte[] b, int length) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; n < length; ++n) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1) {
                hs.append("0");
            }
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }

    public static String byte2HexStr(byte[] b) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; n < b.length; ++n) {
            stmp = Integer.toHexString(b[n] & 0xFF);
            if (stmp.length() == 1) {
                hs.append("0");
            }
            hs.append(stmp);
        }
        return hs.toString().toUpperCase();
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


    public static String logArray(int[] array) {
        StringBuilder builder = new StringBuilder();
        for (int i : array) {
            builder.append(String.format("%02X ", (byte) i));
        }
        builder.append("\n");
        return builder.toString();
    }

    public static String logChar(char[] array) {
        StringBuilder builder = new StringBuilder();
        for (char i : array) {
            builder.append(String.format("%02X ", (byte) i));
        }
        builder.append("\n");
        return builder.toString();
    }

    public static Double formatDouble(double d) {
        DecimalFormat df = new DecimalFormat("#.00");
        return Double.valueOf(df.format(d));
    }

    /**
     * 校验手机号
     *
     * @return
     */

    public static String hexString2binaryString(String hexString) {
        if (TextUtils.isEmpty(hexString)) {
            return null;
        }
        String binaryString = "";
        for (int i = 0; i < hexString.length(); i++) {
            //截取hexStr的一位
            String hex = hexString.substring(i, i + 1);
            //通过toBinaryString将十六进制转为二进制
            String binary = Integer.toBinaryString(Integer.parseInt(hex, 16));
            //因为高位0会被舍弃，先补上4个0
            String tmp = "0000" + binary;
            //取最后4位，将多补的0去掉
            binaryString += tmp.substring(tmp.length() - 4);
        }
        return binaryString;
    }

    public static byte[] intToByte4(int i) {
        byte[] targets = new byte[4];
        targets[3] = (byte) (i & 0xFF);
        targets[2] = (byte) (i >> 8 & 0xFF);
        targets[1] = (byte) (i >> 16 & 0xFF);
        targets[0] = (byte) (i >> 24 & 0xFF);
        return targets;
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {

        int len = 0;

        for (byte[] srcArray : srcArrays) {

            len += srcArray.length;

        }

        byte[] destArray = new byte[len];

        int destLen = 0;

        for (byte[] srcArray : srcArrays) {

            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);

            destLen += srcArray.length;

        }

        return destArray;

    }

    public static int getInt1(byte[] arr, int index) {
        return (0xff000000 & (arr[index + 0] << 24)) |
                (0x00ff0000 & (arr[index + 1] << 16)) |
                (0x0000ff00 & (arr[index + 2] << 8)) |
                (0x000000ff & arr[index + 3]);
    }

    public static float getFloat(byte[] arr, int index) {
        return Float.intBitsToFloat(getInt1(arr, index));
    }

    public static String toStringHex(String s) {
        byte[] baKeyword = new byte[s.length() / 2];
        for (int i = 0; i < baKeyword.length; i++) {
            try {
                baKeyword[i] = (byte) (0xff & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        try {
            s = new String(baKeyword, StandardCharsets.UTF_8);//UTF-16le:Not
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return s;
    }

    public static boolean isNumeric(String str) {
        int temp = 0;
        for (int i = 0; i < str.length(); i++) {
            //判断字符串的最后一位不能为小数点
            if (str.charAt(str.length() - 1) == '.') {
                return false;
            }
//判断不能含有多个小数点
            if (str.charAt(i) == '.') {
                temp++;
                if (temp >= 2) {
                    return false;
                }
                continue;
            }
//判断字符串的每一个字符不能为数字以外的字符
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static String bytesToString2(byte[] bytes) {
        final char[] hexArray = "0123456789ABCDEF".toCharArray();
        char[] hexChars = new char[bytes.length * 2];
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < bytes.length; i++) {
            int v = bytes[i] & 0xFF;
            hexChars[i * 2] = hexArray[v >>> 4];
            hexChars[i * 2 + 1] = hexArray[v & 0x0F];

            sb.append(hexChars[i * 2]);
            sb.append(hexChars[i * 2 + 1]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public static byte[][] splitBytes(byte[] bytes, int size) {
        double splitLength = Double.parseDouble(size + "");
        int arrayLength = (int) Math.ceil(bytes.length / splitLength);
        byte[][] result = new byte[arrayLength][];
        int from, to;
        for (int i = 0; i < arrayLength; i++) {

            from = (int) (i * splitLength);
            to = (int) (from + splitLength);
            if (to > bytes.length)
                to = bytes.length;
            result[i] = Arrays.copyOfRange(bytes, from, to);
        }
        return result;
    }

    public static int binaryToDecimal(String str) {
        BigInteger bi = new BigInteger(str, 2);
        return Integer.parseInt(bi.toString());
    }

}
