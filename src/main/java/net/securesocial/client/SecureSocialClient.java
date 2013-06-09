package net.securesocial.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
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
	 * 
	 * @param privateKey
	 * @param publicKey
	 * @param password
	 * @param id
	 *            - new id
	 */
	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id) {
		createIdentity(privateKey, publicKey, password, id, (Map<String, String>) null);
	}

	/**
	 * 
	 * @param privateKey
	 * @param publicKey
	 * @param password
	 * @param id
	 *            - new id
	 * @param name
	 *            - name of new identity
	 */
	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id, String name) {
		Map<String, String> properties = createPropertiesForName(name);
		createIdentity(privateKey, publicKey, password, id, properties);
	}

	private Map<String, String> createPropertiesForName(String name) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("Name", name);
		return properties;
	}

	/**
	 * 
	 * @param privateKey
	 * @param publicKey
	 * @param password
	 * @param id
	 *            - new id
	 * @param properties
	 *            - map of identity properties
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
	/**
	 * Retrieve identity information
	 * @param id of identity
	 * @return identity details
	 */
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

	/**
	 * update existing identity
	 * 
	 * @param id
	 * @param oldPrivateKey
	 * @param newPublicKey
	 * @param password
	 * @param newName
	 */
	public void updateIdentity(String id, InputStream oldPrivateKey, InputStream newPublicKey, String password, String newName) {
		updateIdentity(id, oldPrivateKey, newPublicKey, password, createPropertiesForName(newName));
	}

	/**
	 * update existing identity
	 * 
	 * @param id
	 * @param oldPrivateKey
	 * @param newPublicKey
	 * @param password
	 * @param newProperties
	 */
	public void updateIdentity(String id, InputStream oldPrivateKey, InputStream newPublicKey, String password, Map<String, String> newProperties) {
		try {
			Map<String, Object> newPerson = new HashMap<String, Object>();
			newPerson.put("id", id);
			newPerson.put("publicKey", streamToString(newPublicKey));
			if (newProperties != null) {
				newPerson.put("properties", newProperties);
			}
			ObjectMapper om = new ObjectMapper();
			putString(streamToString(oldPrivateKey), "/identities/" + id, om.writeValueAsString(newPerson), password);
		} catch (Exception ex) {
			throwE(ex);
		}
	}

	/**
	 * save small amount of private content keyed by guid
	 * @param id
	 * @param privateKey
	 * @param password
	 * @param path
	 * @param content
	 */
	public void savePrivateContent(String id, InputStream privateKey, String password, String guid, InputStream content) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			IOUtils.copy(content, baos);
			byte[] encoded = Base64.encodeBase64(baos.toByteArray());
			putString(streamToString(privateKey), "/identities/" + id + "/private/" + guid, new String(encoded), password);

		} catch (Exception ex) {
			throwE(ex);
		}
	}

	/**
	 * get private content back
	 * @param id
	 * @param privateKey
	 * @param password
	 * @param path
	 * @param content
	 */
	public void getPrivateContent(String id, InputStream privateKey, String password, String guid, OutputStream content) {
		try {
			String encoded = getString(streamToString(privateKey), "/identities/" + id + "/private/" + guid, password);
			byte[] bytes = Base64.decodeBase64(encoded);
			IOUtils.copy(new ByteArrayInputStream(bytes), content);
		} catch (Exception ex) {
			throwE(ex);
		}
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
