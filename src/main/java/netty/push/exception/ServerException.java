package netty.push.exception;

/**
 * @author sunding
 * 服务端异常信息
 */
public class ServerException extends RuntimeException{

	public ServerException(String msg) {
		super(msg);
	}

	public ServerException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
