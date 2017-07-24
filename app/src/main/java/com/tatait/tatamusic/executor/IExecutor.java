package com.tatait.tatamusic.executor;

/**
 * Created by Lynn on 2017/1/20.
 */
public interface IExecutor<T> {
    void execute();

    void onPrepare();

    void onExecuteSuccess(T t);

    void onExecuteFail(Exception e);
}