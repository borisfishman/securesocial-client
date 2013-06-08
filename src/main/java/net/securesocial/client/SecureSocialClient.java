package net.securesocial.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * work in progress!
 * 
 * 
 */
public class SecureSocialClient extends BaseCommunicator {

	/**
	 * get list of new IDs
	 * 
	 * @return list of ids
	 */
	public List<String> getSuggestedIds() {
		ObjectMapper om = new ObjectMapper();
		try {
			@SuppressWarnings("unchecked")
			Map<String, List<String>> map = om.readValue(getStream(null, "/newnames", null), Map.class);
			return map.get("newnames");
		} catch (Exception ex) {
			throwE(ex);
		}
		return null;

	}

	/**
	 * create new identity
	 * @param privateKey
	 * @param publicKey
	 * @param password
	 * @param id - new id
	 */
	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id) {
		createIdentity(privateKey, publicKey, password, id, (Map<String, String>) null);
	}

	/**
	 * 
	 * @param privateKey
	 * @param publicKey
	 * @param password
	 * @param id - new id
	 * @param name - name of new identity
	 */
	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id, String name) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("Name", name);
		createIdentity(privateKey, publicKey, password, id, properties);
	}

	/**
	 * 
	 * @param privateKey
	 * @param publicKey
	 * @param password
	 * @param id - new id
	 * @param properties - map of identity properties
	 */
	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id, Map<String, String> properties) {
		try {
			Map<String, Object> newPerson = new HashMap<String, Object>();
			newPerson.put("id", id);
			newPerson.put("publicKey", streamToString(publicKey));
			if (properties != null) {
				newPerson.put("properties", properties);
			}
			ObjectMapper om = new ObjectMapper();
			postString(streamToString(privateKey), "/identities", om.writeValueAsString(newPerson), password);
		} catch (Exception ex) {
			throwE(ex);
		}

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Identity getIdentity(String id) {
		try {
			String string = getString(null, "/identities/" + id, null);
			Map<String, Map> obj = new ObjectMapper().readValue(string, Map.class);
			Map<String, Object> realObj = obj.get("identity");
			return fromObjectMap(realObj);
		} catch (Exception e) {
			throwE(e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	private Identity fromObjectMap(Map<String, Object> realObj) {
		Identity result = new Identity((String) realObj.get("id"), (String) realObj.get("publicKey"));
		Map<String, String> properties = (Map<String, String>) realObj.get("properties");
		if (properties != null) {
			result.getProperties().putAll(properties);
		}
		return result;
	}

	private String streamToString(InputStream input) throws IOException {
		StringWriter sw = new StringWriter();
		IOUtils.copy(input, sw);
		return sw.toString();
	}
}
