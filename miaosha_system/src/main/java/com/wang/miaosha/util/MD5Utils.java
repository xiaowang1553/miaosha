package com.wang.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * 两次MD5
 * 1.用户端：pass=MD5(明文+固定Salt)
 * 2.服务端：pass=MD5(用户输入+随机Salt)
 */
public class MD5Utils {
    private static final String salt="1a2b3c4d";
    public static String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    public static String inputPassToFormPass(String inputPass){
         String str=""+salt.charAt(0)+salt.charAt(2)+inputPass+salt.charAt(5)+salt.charAt(4);
         System.out.println(str);
         return md5(str);
    }

    public static String inputPassToDbPass(String input,String saltDB){
        String formPass=inputPassToFormPass(input);
        String dbPass=formPassToDBPass(formPass,saltDB);
        return dbPass;
    }

    public static String formPassToDBPass(String formPass,String salt){
        String str=""+salt.charAt(0)+salt.charAt(2)+formPass+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }

    public static void main(String[] args) {
        System.out.println(inputPassToFormPass("123456")); //d3b1294a61a07da9b49b6e22b2cbd7f9
        System.out.println(inputPassToDbPass("123456","1a2b3c4d"));//b7797cce01b4b131b433b6acf4add449
    }
}
