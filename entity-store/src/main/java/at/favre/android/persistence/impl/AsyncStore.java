package at.favre.android.persistence.impl;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import at.favre.android.persistence.IndexEngine;
import at.favre.android.persistence.Memory;
import at.favre.android.persistence.Persistence;
import at.favre.android.persistence.Serializer;

/**
 * Created by PatrickF on 20.10.2015.
 */
public class AsyncStore<Key,Value> extends DefaultStore<Key,Value> {

	private ExecutorService executorService;
	private ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);

	public AsyncStore(ExecutorService executorService,
			IndexEngine<Key> indexEngine, Memory<Key, Value> memory,
			Persistence<Key> storePersistence, Serializer<Value> serializer) {
		super(indexEngine, memory, storePersistence, serializer);
		this.executorService = executorService;
	}


	@Override
	public void insertOrUpdate(final Key id, final Value obj) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					readWriteLock.writeLock().lock();
					AsyncStore.super.insertOrUpdate(id, obj);
				} finally {
					readWriteLock.writeLock().unlock();
				}
			}
		});
	}

	@Override
	public void delete(final Key id) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					readWriteLock.writeLock().lock();
					AsyncStore.super.delete(id);
				} finally {
					readWriteLock.writeLock().unlock();
				}
			}
		});
	}

	@Override
	public void clear() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				try {
					readWriteLock.writeLock().lock();
					AsyncStore.super.clear();
				} finally {
					readWriteLock.writeLock().unlock();
				}
			}
		});
	}
}
