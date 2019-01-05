package netty.push.codec;

/**
 * Created by sundingding on 2019/1/2.
 */
public interface Json {

	Json JSON = JsonFactory.create();

	<T> T fromJson(String json, Class<T> clazz);

	String toJson(Object json);
}
