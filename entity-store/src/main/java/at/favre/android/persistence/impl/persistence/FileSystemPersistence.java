package at.favre.android.persistence.impl.persistence;

import android.os.StrictMode;
import android.util.Log;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.Formatter;
import java.util.zip.GZIPOutputStream;

import at.favre.android.persistence.Persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public class FileSystemPersistence<Key> implements Persistence<Key> {
	private static final String TAG = FileSystemPersistence.class.getSimpleName();
	private static MessageDigest MESSAGE_DIGEST;

	private File directory;
	private final boolean compress;
	private final String directoryStringPath;

	public FileSystemPersistence(boolean compress, String directoryStringPath) {
		this.compress = compress;
		this.directoryStringPath = directoryStringPath;
	}

	@Override
	public void setup() {
		directory = new File(directoryStringPath);
		if(!directory.exists()) {
			Log.d(TAG,"create dir "+ directoryStringPath);
			if(!directory.mkdirs() || !directory.isDirectory()) {
				throw new IllegalStateException("could not create storage dir "+ directoryStringPath);
			}
		}
	}

	@Override
	public void writeToPersistence(Key k, byte[] content) throws IOException {
		StrictMode.noteSlowCall("write to disk storage");
		File targetFile = getStorageAddress(k);

		try {
			if(targetFile.exists()) {
				targetFile.delete();
			}

			if(!targetFile.createNewFile()) {
				throw new IllegalStateException("Trying to write to disk, could not create file " + targetFile);
			}

			OutputStream out = null;

			try {
				out = new FileOutputStream(targetFile);
				if(compress) {
					out = new GZIPOutputStream(out);
				}
				out.write(content);
			} finally {
				if(out != null) out.close();
			}
		} catch (Exception e) {
			throw new IOException("Could not write to disk, using file "+targetFile,e);
		}
	}

	private File getStorageAddress(Key key) {
		return new File(directory,createSha1HashHex(key.toString()));
	}

	@Override
	public byte[] readFromPersistence(Key k) {
		ByteArrayOutputStream outputStream = null;
		InputStream inputStream = null;
		try {
			File targetFile = getStorageAddress(k);
			if (targetFile.exists()) {
				inputStream = new FileInputStream(targetFile);
				byte[] buffer = new byte[4096];
				outputStream = new ByteArrayOutputStream();

				int read = 0;
				while ((read = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, read);
				}
				return outputStream.toByteArray();
			}
		} catch (Exception e) {
			Log.w(TAG,"Could not read from disk, try to remove file",e);
			remove(k);
		} finally {
			try {if(inputStream != null) inputStream.close();} catch (Exception e) {Log.w(TAG,"could not close inputstream",e);}
			try {if(outputStream != null) outputStream.close();} catch (Exception e) {Log.w(TAG,"could not close outputstream",e);}
		}
		return null;
	}

	@Override
	public boolean remove(Key k) {
		File targetFile = getStorageAddress(k);

		if (targetFile.exists()) {
			if(!targetFile.delete()) {
				Log.w(TAG,"could not delete "+targetFile+" from disk");
			} else {
				return true;
			}
		}
		return false;
	}

	@Override
	public void clear() {
		for(File file: directory.listFiles()) {
			if(!file.delete()) {
				Log.w(TAG,"could not delete "+file+" from disk");
			}
		}
	}

	@Override
	public StringBuilder dump() {
		StringBuilder sb = new StringBuilder();
		sb.append("\nDUMP CONTENT OF ").append(getClass().getSimpleName()).append(" ").append(toString());
		if(directory != null && directory.exists()) {
			int count = 0;
			for (File file : directory.listFiles()) {
				BufferedReader reader=null;
				try {
					sb.append("\n").append(file);
					reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8")));
					String line;
					while ((line = reader.readLine()) != null) {
						sb.append("\n\t").append(line);
					}
				} catch (Exception e) {
					sb.append("\ncould not read ").append(file).append(": ").append(e.getMessage());
				} finally {
					try {if(reader != null) reader.close();} catch (Exception e) {Log.w(TAG,"could not close reader",e);}
				}
				count++;
			}
			if(count == 0) {
				sb.append("\n<no files found>");
			}
		}
		sb.append("\nEND DUMP OF ").append(getClass().getSimpleName());
		return sb;
	}


	protected static String createSha1HashHex(String unhashed) {
		try {
			if (MESSAGE_DIGEST == null) {
				MESSAGE_DIGEST = MessageDigest.getInstance("SHA-1");
			}
			MESSAGE_DIGEST.update(unhashed.getBytes("UTF-8"));
			return byteArrayToHexString(MESSAGE_DIGEST.digest());
		} catch (Exception e) {
			throw new IllegalStateException("Could not hash with SHA1", e);
		}
	}

	private static String byteArrayToHexString(final byte[] hash) {
		Formatter formatter = new Formatter();
		for (byte b : hash) {
			formatter.format("%02x", b);
		}
		return formatter.toString();
	}

	@Override
	public String toString() {
		return "FileSystemPersistence{" +
				"directory=" + directory +
				", compress=" + compress +
				", directoryStringPath='" + directoryStringPath + '\'' +
				'}';
	}
}
