package net.securesocial.client;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * work in progress!
 * 
 *
 */
public class SecureSocialClient extends BaseCommunicator {

	/**
	 * get list of new IDs
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
	
	public Identity createIdentity(InputStream privateKey, InputStream publicKey, String newId) {
		return null;
		
	}
}
