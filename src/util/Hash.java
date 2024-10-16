package util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Hash {
	static MessageDigest md;

	static {
		try {
			md = MessageDigest.getInstance("SHA256");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	synchronized public static byte[] digest(byte[] data) {
		md.reset();
		md.update(data);
		return md.digest();
	};

	public static String of(byte[] data) {
		return String.format("%016X", new BigInteger(1,digest(data)));
	};

	public static String of(String data) {
		return of( data.getBytes() );
	};
		
	synchronized public static String of(Object ...values) {
		md.reset();
		for( var o : values )
			md.update( o.toString().getBytes() );

		return String.format("%016X", new BigInteger(1, md.digest()));
	};
	
}
