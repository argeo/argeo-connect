package org.argeo.connect.ui;

import static org.argeo.cms.ui.theme.CmsImages.createAction;
import static org.argeo.cms.ui.theme.CmsImages.createIcon;
import static org.argeo.cms.ui.theme.CmsImages.createType;

import org.argeo.cms.ui.theme.CmsImages;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class ConnectImages {
	final private static String CONTACT_TYPES_BASE = CmsImages.ICONS_BASE + "contactTypes/";

	private static Image createContactType(String name) {
		return CmsImages.createImg(CONTACT_TYPES_BASE + name);
	}

	//
	// GENERIC ICONS
	//
	public final static Image PRIMARY = createIcon("primary.png");
	public final static Image PRIMARY_NOT = createIcon("primaryNOT.png");
	public final static Image ORIGINAL = createIcon("first.png");
	public final static Image NO_PICTURE = createIcon("noPicture.gif");
	// Actions
	public final static Image DELETE = createAction("delete.png");
	public final static Image DELETE_LEFT = createAction("delete_left.gif");
	public final static Image MERGE = createAction("merge.gif");
	// Image Descriptors still required for some Actions
	public final static ImageDescriptor IMG_DESC_EDIT = CmsImages.createDesc(CmsImages.ACTIONS_BASE + "edit.gif");
	public final static ImageDescriptor IMG_DESC_ADD = CmsImages.createDesc(CmsImages.ACTIONS_BASE + "add.png");
	public final static ImageDescriptor IMG_DESC_CLOSE = CmsImages.createDesc(CmsImages.ACTIONS_BASE + "close.png");
	public final static Image ADD = IMG_DESC_ADD.createImage();
	public final static Image EDIT = IMG_DESC_EDIT.createImage();
	public final static Image CLOSE = IMG_DESC_CLOSE.createImage();

	//
	// ASSEMBLY
	//
	public final static Image DASHBOARD = createType("dashboard.png");
	public final static Image SEARCH = createAction("search.png");

	//
	// PEOPLE
	//
	public final static Image GROUP = createType("group.png");
	public final static Image USER = createType("person.png");
	public final static Image ROLE = createType("role.gif");
	public final static Image TAG = createType("tag.png");
	public final static Image PERSON = createType("person.png");
	public final static Image ORG = createType("organisation.png");
	public final static Image MAILING_LIST = createType("mailingList.gif");
	// Contact types
	public final static Image DEFAULT_MAIL = createContactType("email.png");
	public final static Image DEFAULT_PHONE = createContactType("defaultPhone.png");
	public final static Image PHONE_DIRECT = createContactType("telephone.png");
	public final static Image FAX = createContactType("fax.png");
	public final static Image MOBILE = createContactType("mobile.png");
	public final static Image DEFAULT_ADDRESS = createContactType("address.png");
	public final static Image DEFAULT_URL = createContactType("link.png");
	public final static Image WORK = createContactType("workAddress.png");
	public final static Image PRIVATE_HOME_PAGE = createContactType("house_link.png");
	// Social media
	public final static Image DEFAULT_SOCIAL_MEDIA = createContactType("socialmedia.png");
	public final static Image GOOGLEPLUS = createContactType("googleplus.png");
	public final static Image SKYPE = createContactType("skype.png");
	public final static Image TWITTER = createContactType("twitter.png");
	public final static Image LINKEDIN = createContactType("linkedin.png");
	public final static Image FACEBOOK = createContactType("facebook.png");
	public final static Image XING = createContactType("xing.png");
	public final static Image DEFAULT_IMPP = createContactType("impp.png");

	//
	// DOCUMENTS
	//
	public final static Image FOLDER = createType("folder.png");
	public final static Image FILE = createType("file.png");
	public final static Image BOOKMARK = FOLDER;
	public final static Image SHARED_FOLDER = FOLDER;
	public final static Image DOCUMENTS = createType("documents.png");

	//
	// ACTIVITIES
	//
	public final static Image DONE_TASK = createType("doneTask.png");
	public final static Image TODO = createType("todo.gif");
	public final static Image RATE = createType("todo.gif");
	// TODO We still use contact images: get more specific icons
	public final static Image ACTIVITY = createType("activity.gif");
	public final static Image NOTE = createType("note.gif");
	public final static Image SENT_MAIL = createType("sentMail.png");
	public final static Image PHONE_CALL = createType("phoneCall.png");
	public final static Image SENT_FAX = createType("sentFax.png");
	// TODO find icons for other types:
	public final static Image DUMMY_UNDEFINED = createType("noImage.gif");
	public final static Image MEETING = DUMMY_UNDEFINED;
	public final static Image POST_MAIL = DUMMY_UNDEFINED;
	public final static Image PAYMENT = DUMMY_UNDEFINED;
	public final static Image REVIEW = DUMMY_UNDEFINED;
	public final static Image CHAT = DUMMY_UNDEFINED;
	public final static Image TWEET = DUMMY_UNDEFINED;
	public final static Image BLOG = DUMMY_UNDEFINED;

	//
	// TRACKER
	//
	public final static Image ISSUE = createType("bug.gif");
	public final static Image TASK = createType("task.png");
	public final static Image PROJECT = createType("project.png");
	public final static Image MILESTONE = createType("milestone.png");
	public final static Image SPECIFICATION = createType("specification.gif");
	public final static Image CATEGORY = createType("category.gif");
}
