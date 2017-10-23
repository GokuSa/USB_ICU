package cn.shine.icumaster.net;

public interface ResponsListener<T> {
	public void onSuccess(T t);

	public void onFailure(Throwable e);
}
