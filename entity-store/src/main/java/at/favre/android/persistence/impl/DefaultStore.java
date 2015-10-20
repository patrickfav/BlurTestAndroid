package at.favre.android.persistence.impl;

import at.favre.android.persistence.IndexEngine;
import at.favre.android.persistence.Memory;
import at.favre.android.persistence.Persistence;
import at.favre.android.persistence.Serializer;

/**
 * Created by PatrickF on 20.10.2015.
 */
public class DefaultStore<Key,Value> extends BasicStore<Key,Value>{
	public DefaultStore(IndexEngine<Key> indexEngine, Memory<Key, Value> memory, Persistence<Key> storePersistence, Serializer<Value> serializer) {
		super(indexEngine, memory, storePersistence, serializer);
	}
}
