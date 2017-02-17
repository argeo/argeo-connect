package org.argeo.connect;

/** JCR node types managed by Connect */
public interface ConnectTypes {

	/* COMMON CONCEPTS */
	String CONNECT_BASE = "connect:base";
	String CONNECT_ENTITY = "connect:entity";
	String CONNECT_EXTERNAL_ID = "connect:externalId";
	String CONNECT_TAGGABLE = "connect:taggable";
	String CONNECT_ORDERABLE = "connect:orderable";

	/* TASKS AND ACTIVITIES */
	String CONNECT_ACTIVITY = "connect:activity";
	String CONNECT_TASK = "connect:task";
	String CONNECT_POLL = "connect:poll";

	String CONNECT_NOTE = "connect:note";
	String CONNECT_SENT_EMAIL = "connect:sentEmail";
	String CONNECT_CALL = "connect:call";
	String CONNECT_MEETING = "connect:meeting";
	String CONNECT_SENT_LETTER = "connect:sentLetter";
	String CONNECT_SENT_FAX = "connect:sentFax";
	String CONNECT_PAYMENT = "connect:payment";
	String CONNECT_REVIEW = "connect:review";
	String CONNECT_CHAT = "connect:chat";
	String CONNECT_TWEET = "connect:tweet";
	String CONNECT_BLOG_POST = "connect:blogPost";
	String CONNECT_RATE = "connect:rate";

//	/* RESOURCES */
//	String CONNECT_NODE_TEMPLATE = "connect:nodeTemplate";
//	String CONNECT_TAG_PARENT = "connect:tagParent";
//	String CONNECT_TAG_INSTANCE = "connect:tagInstance";
//	String CONNECT_TAG_ENCODED_INSTANCE = "connect:encodedTagInstance";

}
