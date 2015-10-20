package at.favre.android.persistence.impl;

import at.favre.android.persistence.IndexEngine;
import at.favre.android.persistence.Memory;
import at.favre.android.persistence.Persistence;
import at.favre.android.persistence.Serializer;
import at.favre.android.persistence.Store;

/**
 * Created by PatrickF on 20.10.2015.
 */
public class BasicStore<Key, T> implements Store<Key, T> {

	private IndexEngine<Key> indexEngine;
	private Memory<Key, T> memory;
	private Persistence<Key> storePersistence;
	private Serializer<T> serializer;

	public BasicStore(IndexEngine<Key> indexEngine, Memory<Key, T> memory, Persistence<Key> storePersistence, Serializer<T> serializer) {
		this.indexEngine = indexEngine;
		this.memory = memory;
		this.storePersistence = storePersistence;
		this.serializer = serializer;

		indexEngine.setup();
		storePersistence.setup();
	}

	@Override
	public T get(Key id) {
		T memObj = memory.get(id);

		if (memObj == null) {
			memObj = serializer.deserialie(storePersistence.readFromPersistence(id), null);
			memory.put(id, memObj);
		}

		return memObj;
	}

	@Override
	public void insertOrUpdate(Key id, T obj) {
		storePersistence.writeToPersistence(id, serializer.serialize(obj));
		memory.put(id, obj);
		indexEngine.add(id);
	}

	@Override
	public int getSize() {
		return indexEngine.getIndex().size();
	}

	@Override
	public void delete(Key id) {
		memory.remove(id);
		storePersistence.remove(id);
		indexEngine.remove(id);
	}

	@Override
	public void clear() {
		memory.clear();
		storePersistence.clear();
		indexEngine.clear();
	}
}
