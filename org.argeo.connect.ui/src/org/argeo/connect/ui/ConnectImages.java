package org.argeo.connect.ui;

import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;

public class ConnectImages {
	private static BundleContext bc = FrameworkUtil.getBundle(ConnectImages.class).getBundleContext();

	final private static String ICONS_BASE = "icons/";
	final private static String TYPES_BASE = ICONS_BASE + "types/";
	final private static String CONTACT_TYPES_BASE = ICONS_BASE + "contactTypes/";
	final private static String ACTIONS_BASE = ICONS_BASE + "actions/";

	private static Image createIcon(String name) {
		return createImg(ICONS_BASE + name);
	}

	private static Image createAction(String name) {
		return createImg(ACTIONS_BASE + name);
	}

	private static Image createType(String name) {
		return createImg(TYPES_BASE + name);
	}

	private static Image createContactType(String name) {
		return createImg(CONTACT_TYPES_BASE + name);
	}

	public static Image createImg(String name) {
		return createDesc(name).createImage();
	}

	private static ImageDescriptor createDesc(String name) {
		URL url = bc.getBundle().getResource(name);
		if (url == null)
			return ImageDescriptor.getMissingImageDescriptor();
		return ImageDescriptor.createFromURL(url);
	}

	//
	// GENERIC ICONS
	//
	public final static Image PRIMARY_BTN = createIcon("primary.gif");
	public final static Image PRIMARY_NOT_BTN = createIcon("primaryNOT.gif");
	public final static Image ORIGINAL_BTN = createIcon("first.png");
	public final static Image NO_PICTURE = createIcon("noPicture.gif");

	//
	// PEOPLE
	//
	public final static Image ICON_GROUP = createType("group.gif");
	public final static Image ICON_USER = createType("user.gif");
	public final static Image ICON_ROLE = createType("role.gif");
	public final static Image ICON_TAG = createType("tag.png");
	public final static Image ICON_PERSON = createType("person.gif");
	public final static Image ICON_ORG = createType("organisation.png");
	public final static Image ICON_MAILING_LIST = createType("mailingList.gif");
	// Contact types
	public final static Image DEFAULT_MAIL = createContactType("email.png");
	public final static Image DEFAULT_PHONE = createContactType("defaultPhone.png");
	public final static Image PHONE_DIRECT = createContactType("telephone.png");
	public final static Image FAX = createContactType("fax.png");
	public final static Image MOBILE = createContactType("mobile.png");
	public final static Image DEFAULT_ADDRESS = createContactType("home.png");
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
	// Impp
	public final static Image DEFAULT_IMPP = createContactType("impp.png");
	// Actions Icons
	public final static Image DELETE_BTN = createAction("delete.gif");
	public final static Image DELETE_BTN_LEFT = createAction("delete_left.gif");
	public final static Image ADD_BTN = createAction("add.png");
	public final static Image MERGE_BTN = createAction("merge.gif");
	public final static Image SEARCH = createAction("search.png");

	//
	// DOCUMENTS
	//
	public final static Image ICON_FOLDER = createType("folder.gif");
	public final static Image ICON_FILE = createType("file.gif");
	public final static Image ICON_BOOKMARK = createType("folder.gif");
	public final static Image ICON_SHARED_FOLDER = createType("folder.gif");
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
	// TODO why do we use descriptors?
	public final static ImageDescriptor IMG_DESC_EDIT = createDesc(ACTIONS_BASE + "edit.gif");
	public final static ImageDescriptor IMG_DESC_ADD = createDesc(ACTIONS_BASE + "add.gif");
	public final static ImageDescriptor IMG_DESC_CLOSE = createDesc(ACTIONS_BASE + "close.png");
	public final static Image ICON_ADD = IMG_DESC_ADD.createImage();
	public final static Image ICON_ISSUE = createType("bug.gif");
	public final static Image ICON_TASK = createType("task.png");
	public final static Image ICON_PROJECT = createType("project.gif");
	public final static Image ICON_MILESTONE = createType("milestone.gif");
	public final static Image ICON_SPEC = createType("specification.gif");
	public final static Image ICON_CATEGORY = createType("category.gif");

	public final static ImageDescriptor TODO_IMGDESC = createDesc(TYPES_BASE + "todo.gif");
}
