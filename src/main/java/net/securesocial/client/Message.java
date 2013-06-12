package net.securesocial.client;

public class Message {

	private final String id;
	private final String from;
	private final String text;
	private final boolean hasAttachment;
	private String messageKey;

	public Message(String id, String from, String text, boolean hasA) {
		super();
		this.id = id;
		this.from = from;
		this.text = text;
		this.hasAttachment = hasA;
	}

	public String getId() {
		return id;
	}

	public String getFrom() {
		return from;
	}

	public String getText() {
		return text;
	}

	public boolean hasAttachment() {
		return hasAttachment;
	}

	public String getMessageKey() {
		return messageKey;
	}

	public void setMessageKey(String messageKey) {
		this.messageKey = messageKey;
	}

	@Override
	public String toString() {
		return "Message [id=" + id + ", from=" + from + ", text=" + text + ", hasAttachment=" + hasAttachment + "]";
	}
	
	
	
}
