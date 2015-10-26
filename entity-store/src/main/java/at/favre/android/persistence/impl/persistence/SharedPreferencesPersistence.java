package at.favre.android.persistence.impl.persistence;

import android.content.SharedPreferences;

import java.io.UnsupportedEncodingException;

import at.favre.android.persistence.Persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public class SharedPreferencesPersistence<Key> implements Persistence<Key> {

	private SharedPreferences sharedPreferences;

	public SharedPreferencesPersistence(SharedPreferences sharedPreferences) {
		this.sharedPreferences = sharedPreferences;
	}

	@Override
	public void setup() {
	}

	@Override
	public void writeToPersistence(Key key, byte[] content) {
		try {
			sharedPreferences.edit().putString(key.toString(),new String(content,"UTF-8")).commit();
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException("utf-8 not supported",e);
		}
	}

	@Override
	public byte[] readFromPersistence(Key key) {
		String stored = sharedPreferences.getString(key.toString(),null);

		if(stored != null) {
			try {
				return stored.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new IllegalStateException("utf-8 not supported",e);
			}
		}

		return new byte[0];
	}

	@Override
	public boolean remove(Key key) {
		if(sharedPreferences.contains(key.toString())) {
			return sharedPreferences.edit().remove(key.toString()).commit();
		}
		return false;
	}

	@Override
	public void clear() {
		sharedPreferences.edit().clear().commit();
	}

	@Override
	public StringBuilder dump() {
		return new StringBuilder(sharedPreferences.getAll().toString());
	}
}
