package net.securesocial.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;

/**
 * work in progress!
 * 
 * 
 */
public class SecureSocialClient extends BaseCommunicator implements SecureSocialClientInterface {

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#getSuggestedIds()
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#createIdentity(java.io.InputStream, java.io.InputStream, java.lang.String, java.lang.String)
	 */
	@Override
	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id) {
		createIdentity(privateKey, publicKey, password, id, (Map<String, String>) null);
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#createIdentity(java.io.InputStream, java.io.InputStream, java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void createIdentity(InputStream privateKey, InputStream publicKey, String password, String id, String name) {
		Map<String, String> properties = createPropertiesForName(name);
		createIdentity(privateKey, publicKey, password, id, properties);
	}

	private Map<String, String> createPropertiesForName(String name) {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("Name", name);
		return properties;
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#createIdentity(java.io.InputStream, java.io.InputStream, java.lang.String, java.lang.String, java.util.Map)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#getIdentity(java.lang.String)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#updateIdentity(java.lang.String, java.io.InputStream, java.io.InputStream, java.lang.String, java.lang.String)
	 */
	@Override
	public void updateIdentity(String id, InputStream oldPrivateKey, InputStream newPublicKey, String password, String newName) {
		updateIdentity(id, oldPrivateKey, newPublicKey, password, createPropertiesForName(newName));
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#updateIdentity(java.lang.String, java.io.InputStream, java.io.InputStream, java.lang.String, java.util.Map)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#savePrivateContent(java.lang.String, java.io.InputStream, java.io.InputStream, java.lang.String, java.lang.String, java.io.InputStream)
	 */
	@Override
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

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#getPrivateContent(java.lang.String, java.io.InputStream, java.lang.String, java.lang.String, java.io.OutputStream)
	 */
	@Override
	public void getPrivateContent(String id, InputStream privateKey, String password, String guid, OutputStream content) {
		try {
			String privateKeyString = streamToString(privateKey);
			String encoded = getString(privateKeyString, "/identities/" + id + "/private/" + guid, password);

			CryptoWrapper.decrypt(new ByteArrayInputStream(encoded.getBytes()), content, privateKeyString, password);
		} catch (Exception ex) {
			throwE(ex);
		}
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#getIncomingUsers(java.lang.String, java.io.InputStream, java.lang.String)
	 */
	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<String> getIncomingUsers(String id, InputStream privateKey, String password) {
		try {
			String privateKeyString = streamToString(privateKey);
			String string = getString(privateKeyString, "/identities/" + id + "/incoming/users", password);
			Map<String, Map> obj = new ObjectMapper().readValue(string, Map.class);
			return (List<String>) obj.get("users");
		} catch (Exception ex) {
			throwE(ex);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#sendMessage(java.lang.String, java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)
	 */
	@Override
	public void sendMessage(String data, String sender, String password, InputStream senderPrivateKey, String... recepientIds) {
		sendMessage(data, null, sender, password, senderPrivateKey, recepientIds);
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#sendMessage(java.lang.String, java.io.InputStream, java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void sendMessage(String data, InputStream attachment, String senderId, String password, InputStream senderPrivateKey, String... recepientIds) {

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

			// use random message key
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
			if (attachment != null) {
				message.put("attachment", "true");
			}

			String messageString = om.writeValueAsString(message);
			

			// send message
			String response = postString(streamToString(senderPrivateKey), "/identities/" + senderId + "/outgoing", messageString, password);

			if (attachment != null) {
				Map<String, String> map = om.readValue(response, Map.class);
				String uploadUrl = map.get("attachment_upload_url");
				File tempFile = File.createTempFile("securesocial", ".file");
				try {
					// encrypt attachment to a temp file using message key
					CryptoWrapper.encryptWithPassphrase(attachment, new FileOutputStream(tempFile), messageKey);
					// send temp file
					simplePut(uploadUrl, new FileInputStream(tempFile), tempFile.length());
				} finally {
					tempFile.delete();
				}

			}

		} catch (Exception ex) {
			throwE(ex);
		}
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#acceptMessages(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)
	 */
	@Override
	public void acceptMessages(String myId, String password, InputStream myPrivateKey, String senderId) {
		try {
			getString(streamToString(myPrivateKey), "/identities/" + myId + "/incoming/users/" + senderId, password);
		} catch (Exception ex) {
			throwE(ex);
		}
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#getMessages(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Message> getMessages(String myId, String password, InputStream myPrivateKey, String timeline) {
		try {
			String privateKeyString = streamToString(myPrivateKey);
			
			InputStream stream = getStream(privateKeyString, "/identities/" + myId + "/current/" + timeline, password);
			List<Map<String, Object>> messages = (List<Map<String, Object>>) new ObjectMapper().readValue(stream, Map.class).get("messages");
			List<Message> results = new ArrayList<Message>();
			for (Map<String, Object> message : messages) {
				// for each message
				// extract encrypted message key
				List<Object> destinations = (List<Object>) message.get("destinations");
				Map<String, Object> destination = (Map<String, Object>) destinations.get(0);
				Map<String, String> attributes = (Map<String, String>) destination.get("attributes");
				

				// decrypt message key
				String messageKey = CryptoWrapper.decrypt(attributes.get("key"), privateKeyString, password);
				// decrypt message
				String payload = CryptoWrapper.decryptWithPassphrase((String) message.get("envelope"), messageKey);

				Message result = new Message((String) message.get("id"), (String) message.get("from"), payload, Boolean.parseBoolean((String) message.get("attachment")));
				if (result.hasAttachment()) {
					result.setMessageKey(messageKey);
				}
				results.add(result);
			}
			return results;

		} catch (Exception ex) {
			throwE(ex);
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#getMessageAttachment(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String, net.securesocial.client.Message, java.io.OutputStream)
	 */
	@Override
	public void getMessageAttachment(String myId, String password, InputStream myPrivateKey, String timeline, Message msg, OutputStream output) {
		try {
			InputStream stream = getStream(streamToString(myPrivateKey), "/identities/" + myId + "/current/" + timeline + "/attachment/" + msg.getId(), password);
			CryptoWrapper.decryptWithPassphrase(stream, output, msg.getMessageKey());			
		} catch (Exception ex) {
			throwE(ex);
		}
	}
	
	/* (non-Javadoc)
	 * @see net.securesocial.client.SecureSocialClientInterface#deleteMessage(java.lang.String, java.lang.String, java.io.InputStream, java.lang.String, net.securesocial.client.Message)
	 */
	@Override
	public void deleteMessage(String myId, String password, InputStream myPrivateKey, String timeline, String msgId) {
		try {
			delete(streamToString(myPrivateKey), "/identities/" + myId + "/current/" + timeline + "/" + msgId, password);
		} catch (Exception ex) {
			throwE(ex);
		}
	}
	
	@Override
	public void deleteMessage(String myId, String password, InputStream myPrivateKey, String timeline, Message msg) {
		deleteMessage(myId, password, myPrivateKey, timeline, msg.getId());
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

	@Override
	public List<Identity> findIdentitiesByName(String name) {
		try {
			InputStream stream = getStream(null, "/identities/properties/Name/" + URLEncoder.encode(name, "UTF-8"), null);
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> list = (List<Map<String, Object>>) new ObjectMapper().readValue(stream, Map.class).get("identities");
			List<Identity> result = new ArrayList<Identity>();
			for (Map<String, Object> realObj : list) {
				result.add(fromObjectMap(realObj));
			}
			return result;
		} catch (Exception e) {
			throwE(e);
		}
		return null;
	}

}
