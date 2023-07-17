package site.doramusic.app.http;

import dora.cache.data.adapter.Result;

public class ApiResult<T> implements Result<T> {

	private String errorCode;

	private String errorDetail;

	private T data;

	private long timestamp = System.currentTimeMillis();


	public String getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public String getErrorDetail() {
		return errorDetail;
	}

	public void setErrorDetail(String errorDetail) {
		this.errorDetail = errorDetail;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	@Override
	public T getRealModel() {
		return data;
	}
}
