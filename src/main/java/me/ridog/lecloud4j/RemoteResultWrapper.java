package me.ridog.lecloud4j;

/**
 * @author: Tate
 * @date: 2016/6/17 10:45
 */
public class RemoteResultWrapper<T> {

    private RemoteResultVo remoteResult;
    private Object obj; //存储转换的对象

    public T getObj() {
        return (T) obj;
    }

    public void setObj(T obj) {
        this.obj = obj;
    }

    public RemoteResultVo getRemoteResult() {
        return remoteResult;
    }

    public void setRemoteResult(RemoteResultVo remoteResult) {
        this.remoteResult = remoteResult;
    }
}
