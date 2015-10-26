package at.favre.android.persistence.impl.persistence;

/**
 * Created by PatrickF on 26.10.2015.
 */
public class AndroidFilePresistence<Key> extends FileSystemPersistence<Key> {
	enum StoragePolicy {
		USE_INTERNAL,
		USE_EXTERNAL,
		PREFER_EXTERNAL
	}

	enum StorageType {
		PERSISTENT_PRIVATE,
		PERSISTENT_PUBLIC,
		CACHE
	}

	public AndroidFilePresistence(boolean compress, String directoryStringPath) {
		super(compress, directoryStringPath);
	}
}
