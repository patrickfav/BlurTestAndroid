package at.favre.android.persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public interface Persistence<Key> {

	void setup();

	void writeToPersistence(Key k, byte[] content) throws Exception;

	byte[] readFromPersistence(Key k) throws Exception;

	boolean remove(Key k);

	void clear();

	StringBuilder dump();
}
