package com.mycompany.dreamhouse.util;
import java.security.*; import java.util.Base64; import javax.crypto.*; import javax.crypto.spec.PBEKeySpec;
public final class PasswordUtil {
 private static final int ITERATIONS=210000, BITS=256, SALT_BYTES=16; private static final SecureRandom RANDOM=new SecureRandom(); private PasswordUtil(){}
 public static String hash(String password){byte[] salt=new byte[SALT_BYTES]; RANDOM.nextBytes(salt); return "pbkdf2-sha256$"+ITERATIONS+"$"+Base64.getEncoder().encodeToString(salt)+"$"+Base64.getEncoder().encodeToString(derive(password.toCharArray(),salt,ITERATIONS));}
 public static boolean verify(String password,String stored){try{String[] p=stored.split("\\$",-1); if(p.length!=4||!p[0].equals("pbkdf2-sha256"))return false; int rounds=Integer.parseInt(p[1]); if(rounds<100000||rounds>1000000)return false; byte[] salt=Base64.getDecoder().decode(p[2]), expected=Base64.getDecoder().decode(p[3]), actual=derive(password.toCharArray(),salt,rounds); if(actual.length!=expected.length)return false; int diff=0; for(int i=0;i<actual.length;i++)diff|=actual[i]^expected[i]; return diff==0;}catch(IllegalArgumentException e){return false;}}
 private static byte[] derive(char[] pass,byte[] salt,int rounds){PBEKeySpec spec=new PBEKeySpec(pass,salt,rounds,BITS); try{return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();}catch(GeneralSecurityException e){throw new IllegalStateException("Password hashing unavailable",e);}finally{spec.clearPassword();}}
}
