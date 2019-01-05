package netty.push.codec;

import java.util.function.Supplier;

/**
 * @author sunding
 */
@FunctionalInterface
public interface Factory<T> extends Supplier<T> {
}
