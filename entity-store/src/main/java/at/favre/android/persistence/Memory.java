package at.favre.android.persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public interface Memory<Key,Value> {

	Value get(Key id);
	void put(Key id, Value value);
	void remove(Key id);
	void clear();


}
