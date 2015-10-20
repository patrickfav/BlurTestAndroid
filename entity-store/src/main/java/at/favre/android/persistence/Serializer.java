package at.favre.android.persistence;

/**
 * Created by PatrickF on 20.10.2015.
 */
public interface Serializer<T> {

	byte[] serialize(Object obj);

	T deserialie(byte[] content,TypeInformation type);

}
