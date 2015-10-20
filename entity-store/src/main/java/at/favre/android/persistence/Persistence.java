package at.favre.android.persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public interface Persistence<Key> {

	void setup();

	void writeToPersistence(Key k, byte[] content);

	byte[] readFromPersistence(Key k);

	void remove(Key k);

	void clear();

	StringBuilder dump();
}
