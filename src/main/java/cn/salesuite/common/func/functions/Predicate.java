package cn.salesuite.common.func.functions;

/**
 * Created by Tony Shen on 16/9/9.
 */
public interface Predicate<T> {

    boolean accept(T t);
}
