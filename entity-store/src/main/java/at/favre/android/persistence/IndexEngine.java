package at.favre.android.persistence;

import java.util.Collection;

/**
 * Created by PatrickF on 20.10.2015.
 */
public interface IndexEngine<Key> {
	void setup();

	Collection<Key> getIndex();

	void add(Key index);

	void remove(Key index);

	void clear();
}
