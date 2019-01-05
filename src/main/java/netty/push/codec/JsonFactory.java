package netty.push.codec;

/**
 * Created by sundingding on 2019/1/2.
 */
public interface JsonFactory extends Factory<Json> {

	static Json create() {
		return SpiLoader.load(JsonFactory.class).get();
	}
}
