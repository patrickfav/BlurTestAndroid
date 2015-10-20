package at.favre.android.persistence.impl.persistence;

import at.favre.android.persistence.Persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public class FileSystemPersistence<Key> implements Persistence<Key> {
	@Override
	public void setup() {
		
	}

	@Override
	public void writeToPersistence(Key k, byte[] content) {

	}

	@Override
	public byte[] readFromPersistence(Key k) {
		return new byte[0];
	}

	@Override
	public void remove(Key k) {

	}

	@Override
	public void clear() {

	}

	@Override
	public StringBuilder dump() {
		return null;
	}
}
