package jj.util;

import static java.nio.charset.StandardCharsets.UTF_8;
import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import jj.resource.ResourceThread;

/** 
 * 
 * @author jason
 *
 */
public enum SHA1Helper {
	
	; // no instances
	
	private static final byte[] DELIMITER = "|".getBytes(UTF_8);

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
		StringBuilder sb = new StringBuilder(bytes.length);
		for (int i = 0; i < bytes.length; i++) {
          sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
 
        return sb.toString();
	}
	
	public static String keyFor(final byte[] bytes) {
		return toHex(sha1.get().digest(bytes));
	}
	
	public static String keyFor(final ByteBuf byteBuf) {
		return toHex(sha1.get().digest(byteBuf.array()));
	}
	
	public static String keyFor(final ByteBuffer bytes) {
		bytes.rewind();
		sha1.get().update(bytes);
		bytes.rewind();
		return toHex(sha1.get().digest());
	}
	
	/**
	 * treats input as UTF-8, adds a delimiter between inputs
	 * @param strings
	 * @return
	 */
	public static String keyFor(final String...strings) {
		for (String string : strings) {
			if (string != null) {
				sha1.get().update(string.getBytes(UTF_8));
				sha1.get().update(DELIMITER);
			}
		}
		return toHex(sha1.get().digest());
	}
	
	@ResourceThread
	public static String keyFor(final Path path) throws IOException {
		try (SeekableByteChannel channel = Files.newByteChannel(path)) {
			if (channel instanceof FileChannel) { 
				map(path, (FileChannel)channel);
				return toHex(sha1.get().digest());
			} else {
				return keyFor(Files.readAllBytes(path));
			}
		}
	}
	
	private static void map(final Path path, final FileChannel channel) throws IOException {
		MappedByteBuffer buffer = channel.map(MapMode.READ_ONLY, 0, Files.size(path));
		sha1.get().update(buffer);
	}
}
