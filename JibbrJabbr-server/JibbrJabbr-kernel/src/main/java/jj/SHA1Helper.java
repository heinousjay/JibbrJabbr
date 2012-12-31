package jj;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** 
 * simple thread-safe way to get a pool of resources.  saw this somewhere on github,
 * very good way to handle certain situations.
 * @author jason
 *
 */
public class SHA1Helper {

	private static final ThreadLocal<MessageDigest> sha1 = new ThreadLocal<MessageDigest>() {
		
		@Override
		protected MessageDigest initialValue() {
			try {
				return MessageDigest.getInstance("SHA-1");
			} catch (NoSuchAlgorithmException e) {
				// can't happen
				return null;
			}
		}
	};
	
	private static String toHex(final byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < bytes.length; i++) {
          sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
 
        return sb.toString();
	}
	
	public static String keyFor(final byte[] bytes) {
		return toHex(sha1.get().digest(bytes));
	}
	
	/**
	 * treats input as UTF-8
	 * @param strings
	 * @return
	 */
	public static String keyFor(final String...strings) {
		for (String string : strings) {
			if (string != null) {
				sha1.get().update(string.getBytes(UTF_8));
			}
		}
		return toHex(sha1.get().digest());
	}
}
