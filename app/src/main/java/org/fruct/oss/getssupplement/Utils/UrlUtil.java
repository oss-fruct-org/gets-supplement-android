package org.fruct.oss.getssupplement.Utils;


import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class UrlUtil {
	private static final int MAX_RECURSION = 5;

	public static HttpURLConnection getConnection(String urlStr) throws IOException {
		return getConnection(urlStr, MAX_RECURSION);
	}

	public static InputStream getInputStream(String urlStr) throws IOException {
		final HttpURLConnection conn = UrlUtil.getConnection(urlStr);
		final InputStream input = conn.getInputStream();

		return new FilterInputStream(input) {
			@Override
			public void close() throws IOException {
				super.close();
				conn.disconnect();
			}
		};
	}

	private static HttpURLConnection getConnection(String urlStr, final int recursionDepth) throws IOException {
		URL url = new URL(urlStr);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setReadTimeout(10000);
		conn.setConnectTimeout(10000);

		conn.setRequestMethod("GET");
		conn.setDoInput(true);

		conn.connect();
		int code = conn.getResponseCode();

		if (code == HttpURLConnection.HTTP_OK) {
			return conn;
		} else {
			throw new IOException(urlStr + " returned code " + code);
		}
	}

}
