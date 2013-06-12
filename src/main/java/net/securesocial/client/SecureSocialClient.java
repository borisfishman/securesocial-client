package net.securesocial.client;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
	 * save small amount of private content keyed by guid. It is encrypted with
	 * public key of owner.
	 * 
	 * @param id
	 * @param privateKey
	 * @param password
	 * @param path
	 * @param content
	 */
	public void savePrivateContent(String id, InputStream privateKey, InputStream publicKey, String password, String guid, InputStream content) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			String publicKeyBytes = streamToString(publicKey);

			CryptoWrapper.encrypt(content, baos, publicKeyBytes);
			putString(streamToString(privateKey), "/identities/" + id + "/private/" + guid, new String(baos.toByteArray()), password);

		} catch (Exception ex) {
			throwE(ex);
		}
	}

	/**
	 * get private content back
	 * 
	 * @param id
	 * @param privateKey
	 * @param password
	 * @param path
	 * @param content
	 */
	public void getPrivateContent(String id, InputStream privateKey, String password, String guid, OutputStream content) {
		try {
			String privateKeyString = streamToString(privateKey);
			String encoded = getString(privateKeyString, "/identities/" + id + "/private/" + guid, password);

			CryptoWrapper.decrypt(new ByteArrayInputStream(encoded.getBytes()), content, privateKeyString, password);
		} catch (Exception ex) {
			throwE(ex);
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> getIncomingUsers(String id, InputStream privateKey, String password) {
		try {
			String privateKeyString = streamToString(privateKey);
			String string = getString(privateKeyString, "/identities/" + id + "/incoming/users", password);
			System.out.println("BEEE:" + string);
			Map<String, Map> obj = new ObjectMapper().readValue(string, Map.class);
			return (List<String>) obj.get("users");
		} catch (Exception ex) {
			throwE(ex);
		}
		return null;
	}

	public void send(String data, String sender, String password, InputStream senderPrivateKey, String... recepientIds) {
		send(data, null, null, sender, password, senderPrivateKey, recepientIds);
	}

	public void send(String data, InputStream attachment, Long attachmentSize, String senderId, String password, InputStream senderPrivateKey, String... recepientIds) {

		try {
			ObjectMapper om = new ObjectMapper();

			// build message object
			Map<String, Object> message = new HashMap<String, Object>();
			String messageId = UUID.randomUUID().toString();
			message.put("id", messageId);
			message.put("from", senderId);

			// destinations include individually encrypted keys to message
			// payload
			List<Map<String, Object>> destinations = new ArrayList<Map<String, Object>>();

			String messageKey = UUID.randomUUID().toString();

			for (String targetId : recepientIds) {
				Identity target = getIdentity(targetId);
				Map<String, Object> destination = new HashMap<String, Object>();
				destination.put("userId", targetId);
				Map<String, String> attributes = new HashMap<String, String>();
				// encrypt message key with this user's public key
				attributes.put("key", CryptoWrapper.encrypt(messageKey, target.getPublicKey()));
				destination.put("attributes", attributes);
				destinations.add(destination);
			}

			message.put("destinations", destinations);
			// encrypt message text with message key
			message.put("envelope", CryptoWrapper.encryptWithPassphrase(data, messageKey));
			if(attachment != null) {
				message.put("attachment", "true");
			}
			
			String messageString = om.writeValueAsString(message);
			System.out.println("message:" + messageString);
			
			// send message
			String response = postString(streamToString(senderPrivateKey), "/identities/" + senderId + "/outgoing", messageString, password);
			
			
			if(attachment != null) {
				Map<String, String> map = om.readValue(response, Map.class);
				String uploadUrl = map.get("attachment_upload_url");
				
				simplePut(uploadUrl, attachment, attachmentSize);
			}
			
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
