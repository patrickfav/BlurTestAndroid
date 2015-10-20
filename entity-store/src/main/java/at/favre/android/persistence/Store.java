package at.favre.android.persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public interface Store<Key,Value> {

	Value get(Key id);

	void insertOrUpdate(Key id, Value value);

	int getSize();

	void delete(Key id);

	void clear();
}
