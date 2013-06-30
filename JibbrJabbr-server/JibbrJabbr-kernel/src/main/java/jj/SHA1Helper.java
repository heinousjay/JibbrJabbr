package jj;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** 
 * 
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
				throw new AssertionError("couldn't make a MessageDigest", e);
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
	
	public static String keyFor(final ByteBuffer bytes) {
		bytes.rewind();
		sha1.get().update(bytes);
		bytes.rewind();
		return toHex(sha1.get().digest());
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
	
	@IOThread
	public static String keyFor(final Path path) throws IOException {
		try (FileChannel channel = (FileChannel)Files.newByteChannel(path)) {
			MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, Files.size(path));
			sha1.get().update(buffer);
		}
		
		return toHex(sha1.get().digest());
	}
}
