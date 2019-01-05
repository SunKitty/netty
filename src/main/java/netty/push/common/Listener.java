package netty.push.common;

/**
 * @author sunding
 */
public interface Listener {

	/**
	 * 成功
	 * @param args
	 */
	void onSuccess(Object... args);

	/**
	 * 失败
	 * @param cause
	 */
	void onFailure(Throwable cause);
}
