package com.bsd.say.util;

import java.security.Key;
import java.security.Security;
 
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
 
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Base64;
 
public class AESWithJCEUtils { 
           
    /**
     * 第三方提供Token的URL： http://XXX
     * @param args
     */ 
    public static void main(String[] args) {         
        String pubkey = "1234567890123456";
        String iv = "WJi7HTZQoh8eHjup";
        String content ="pnHNKu5KCZibpf2iKU7YqYp4TXtDjaV/cvD1E1YlgLjXU9eDzClRa70AgEIvEuPo+A4F8ZwkWeWLjKhFVReywxNdGwXWZy9Hj7CVhTnmkyQ2wo0dLTY+IiC/HPdxPrpCeGuPRABpZjQ+S33VQSP1vywIoKEmTPGU2JSbu0tGiOsZYk3Lhq7vJ3TMljhq0k8R5j5yBYNMgd3Az9+7+LfIZw==";

//         String encode = aesEncode(content, pubkey, iv);
//         System.out.println(encode);
        
         String decode = aesDecode(content, pubkey, iv);
        System.out.println(decode);

    } 
    
    /**
     * 加密方法
     * @param content - 需要加密的串
     * @param pubKey - 秘钥，即API接口中的参数key(随机的16位数字)
     * @param iv - IV向量，固定的16字节串，加密方要告诉解密方该值
     * @return
     */
    public static String aesEncode(String content, String pubKey, String iv){
         try{
                  Security.addProvider(new BouncyCastleProvider()); 
                 Key key = new SecretKeySpec(pubKey.getBytes(), "AES");
                 Cipher in = Cipher.getInstance("AES/CBC/PKCS5Padding"); 
                 in.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(iv.getBytes())); 
                 byte[] enc = in.doFinal(content.getBytes());
                 String result = (new String(Base64.encode(enc))).replaceAll("\n", "");
                
                 return result;
         }catch(Exception ex){
                   ex.printStackTrace();
                   return null;
         }
    }
    /**
     * 解密方法
     * @param mima - 需要解密的串
     * @param pubKey - 秘钥，即API接口中的参数key
     * @param iv - IV向量，固定的16字节串，接口提供方提供该值
     * @return
     */
    public static String aesDecode(String mima, String pubKey, String iv){
         try{
                  Security.addProvider(new BouncyCastleProvider()); 
                 Key key = new SecretKeySpec(pubKey.getBytes(), "AES");
                            Cipher out = Cipher.getInstance("AES/CBC/PKCS5Padding"); 
                            out.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(iv.getBytes())); 
                            byte[] dec = out.doFinal(Base64.decode(mima)); 
                            String result = new String(dec);
                                            
                 return result;
         }catch(Exception ex){
                   ex.printStackTrace();
                   return null;
         }
    }
        
}