package net.securesocial.client;

import java.io.InputStream;
import java.io.StringWriter;
import java.net.ConnectException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;

public abstract class BaseCommunicator {

	private String paymentToken;
	private String endpoint = "https://service.securesocial.net";
	private String additionalHeader;

	private InputStream hit(String privateKey, HttpRequestBase method, String password) throws Exception {
		return hit(privateKey, method, password, null);
	}

	protected void throwE(Exception ex) {

		if (ex instanceof ServiceException) {
			ServiceException se = (ServiceException) ex;
			throw se;
		}
		if (ex instanceof IllegalArgumentException) {
			throw new ServiceException("invalid input: " + ex.getMessage());
		}

		if (ex instanceof NullPointerException) {
			throw ((NullPointerException) ex);
		}

		if (ex.getMessage().contains("checksum mismatch")) {
			throw new AuthenticationException("invalid password");
		}

		if (ex instanceof UnknownHostException || ex instanceof ConnectException) {
			throw new NetworkException("Service or network connection is not available");
		}

		throw new RuntimeException(ex);
	}

	protected void simplePut(String url, InputStream from, long size) throws Exception {
		InputStreamEntity entity = new InputStreamEntity(from, size);
		hit(null, new HttpPut(url), null, entity, null, null);
	}

	public InputStream simpleGet(String url) throws Exception {

		return hit(null, new HttpGet(url), null, null, null, null);
	}

	private InputStream hit(String privateKey, HttpRequestBase method, String password, AbstractHttpEntity entity) throws Exception {
		return hit(privateKey, method, password, entity, "application/json", "application/json");
	}

	private InputStream hit(String privateKey, HttpRequestBase method, String password, AbstractHttpEntity entity, String accepttype, String contentType) throws Exception {

		boolean doSign = (privateKey != null) && (password != null);

		URL url = method.getURI().toURL();

		String uri = url.getPath();

		if (accepttype != null)
			method.setHeader("Accept", accepttype);
		if (!StringUtils.isEmpty(paymentToken)) {
			method.setHeader("X-SecureSocial-Token", paymentToken);
		}
		if (!StringUtils.isEmpty(additionalHeader)) {
			method.setHeader("X-SecureSocial-Xtra", additionalHeader);
		}

		method.setHeader("X-SecureSocial-Client", "public-from-boris");

		MessageDigest digest = null;

		if (doSign) {

			digest = MessageDigest.getInstance("SHA-256");
			digest.update(uri.getBytes());
		}
		if (entity != null) {
			HttpEntityEnclosingRequestBase request = (HttpEntityEnclosingRequestBase) method;
			if (contentType != null)
				entity.setContentType(contentType);
			request.setEntity(entity);
			if (doSign) {
				InputStream is = entity.getContent();
				byte[] buffer = new byte[8192];
				while (true) {
					int read = is.read(buffer);
					if (read == -1)
						break;
					digest.update(buffer, 0, read);
				}
			}
		}

		if (doSign) {

			byte[] digestBytes = digest.digest();
			String sig = CryptoWrapper.sign(privateKey, password, digestBytes);
			method.setHeader("X-SecureSocial-Signature", sig);

		}
		HttpResponse response = new DefaultHttpClient().execute(method);
		int responseCode = response.getStatusLine().getStatusCode();

		if (responseCode != 200) {

			StringWriter sw = new StringWriter();
			IOUtils.copy(response.getEntity().getContent(), sw);
			String responseBody = sw.toString();
			String msg = responseCode + " " + responseBody;

			if (responseCode == 404) {
				throw new NotFoundException(errorMessageFromResponse(responseBody));
			} else if (responseCode == 400) {
				throw new IllegalArgumentException(errorMessageFromResponse(responseBody));
			} else if (responseCode == 402) {
				throw new BillingException(errorMessageFromResponse(responseBody));
			} else if (responseCode == 403) {
				throw new AuthenticationException(errorMessageFromResponse(responseBody));
			} else {
				throw new RuntimeException("http error:" + msg);
			}
		}

		return response.getEntity().getContent();
	}

	private String errorMessageFromResponse(String string) {
		ObjectMapper om = new ObjectMapper();
		try {
			@SuppressWarnings("unchecked")
			Map<String, String> map = om.readValue(string, Map.class);

			return map.get("error");
		} catch (Exception e) {

			return "unknown error";
		}
	}

	protected InputStream getStream(String privateKey, String url, String password) throws Exception {
		HttpGet get = new HttpGet(endpoint + url);
		return hit(privateKey, get, password);
	}

	protected String getString(String privateKey, String url, String password) throws Exception {
		StringWriter sw = new StringWriter();
		IOUtils.copy(getStream(privateKey, url, password), sw);
		return sw.toString();
	}

	protected String postString(String privateKey, String url, String object, String password) throws Exception {
		HttpPost post = new HttpPost(endpoint + url);
		StringEntity entity = new StringEntity(object);
		InputStream is = hit(privateKey, post, password, entity);
		StringWriter sw = new StringWriter();
		IOUtils.copy(is, sw);
		return sw.toString();
	}

	protected String delete(String privateKey, String url, String password) throws Exception {
		HttpDelete delete = new HttpDelete(endpoint + url);
		InputStream is = hit(privateKey, delete, password, null);
		StringWriter sw = new StringWriter();
		IOUtils.copy(is, sw);
		return sw.toString();
	}

	protected void putString(String privateKey, String url, String object, String password) throws Exception {
		HttpPut put = new HttpPut(endpoint + url);
		StringEntity entity = new StringEntity(object);
		hit(privateKey, put, password, entity);

	}

	public String getPaymentToken() {
		return paymentToken;
	}

	public void setPaymentToken(String paymentToken) {
		this.paymentToken = paymentToken;
	}

	public String getEndpoint() {
		return endpoint;
	}

	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	public String getAdditionalHeader() {
		return additionalHeader;
	}

	public void setAdditionalHeader(String additionalHeader) {
		this.additionalHeader = additionalHeader;
	}
}
