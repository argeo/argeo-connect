package org.argeo.connect.people.web.providers;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.jcr.Node;

import org.apache.commons.io.IOUtils;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.ui.PeopleUiConstants;

import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.connect.people.web.PeopleWebConstants;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.rap.rwt.service.ResourceManager;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

/**
 * A label provider that must be defined via spring to get a bundle context
 * before being injected in the correct page
 **/
public class SearchEntitiesLP implements ILabelProvider {
	private static final long serialVersionUID = -7601937314019991492L;

	private PeopleService peopleService;

	// Local objects
	private PersonOverviewLP personOLP;
	private OrgOverviewLP orgOLP;

	// Cache local images
	private Map<String, Image> icons;

	public SearchEntitiesLP(PeopleService peopleService, Display display,
			Map<String, String> iconPathes) {
		this.peopleService = peopleService;
		personOLP = new PersonOverviewLP(
				PeopleWebConstants.OVERVIEW_TYPE_SINGLE_LINE_LIST,
				peopleService);
		orgOLP = new OrgOverviewLP(
				PeopleWebConstants.OVERVIEW_TYPE_SINGLE_LINE_LIST,
				peopleService);

		if (iconPathes != null) {
			createIconMap(display, iconPathes);
		}

	}

	private void createIconMap(Display display, Map<String, String> iconPathes) {
		icons = new HashMap<String, Image>();
		ResourceManager manager = RWT.getResourceManager();
		for (String key : iconPathes.keySet()) {
			InputStream current = null;
			try {
				String value = iconPathes.get(key);
				if (manager.isRegistered(value)) {
					current = manager.getRegisteredContent(value);
					Image image = new Image(display, current);
					icons.put(key, image);
				}
			} finally {
				IOUtils.closeQuietly(current);
			}
		}

	}

	@Override
	public String getText(Object element) {

		Node entity = (Node) element;
		if (CommonsJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_PERSON))
			return // prefixWithImage(PeopleTypes.PEOPLE_PERSON) +
			personOLP.getText(entity);
		else if (CommonsJcrUtils.isNodeType(entity, PeopleTypes.PEOPLE_ORG))
			return // prefixWithImage(PeopleTypes.PEOPLE_ORG) +
			orgOLP.getText(entity);
		else {
			StringBuilder builder = new StringBuilder();
			builder.append("<b>");
			builder.append(peopleService.getDisplayName(entity));
			builder.append("</b>");
			return PeopleUiUtils.replaceAmpersand(builder.toString());
		}
	}

	// DO NOT USE: it introduces problems in the layout with single line rows
	// private String prefixWithImage(String nodeType) {
	// String location = null;
	// if (iconPathes.containsKey(nodeType)) {
	// ResourceManager resourceManager = RWT.getResourceManager();
	// if (resourceManager.isRegistered(iconPathes.get(nodeType)))
	// location = resourceManager
	// .getLocation(iconPathes.get(nodeType));
	// }
	// if (location != null)
	// return "<img width='16' height='16' src=\"" + location + "\"/>";
	// else
	// return "";
	// }

	@Override
	public void addListener(ILabelProviderListener listener) {
	}

	@Override
	public void dispose() {
		if (icons != null)
			for (String key : icons.keySet()) {
				Image image = icons.get(key);
				if (!image.isDisposed())
					image.dispose();
			}
			icons.clear();
	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
	}

	@Override
	public Image getImage(Object element) {
		if (icons == null)
			return null;

		Node node = (Node) element;
		if (icons.containsKey(PeopleTypes.PEOPLE_PERSON)
				&& CommonsJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_PERSON))
			return icons.get(PeopleTypes.PEOPLE_PERSON);
		else if (icons.containsKey(PeopleTypes.PEOPLE_ORG)
				&& CommonsJcrUtils.isNodeType(node, PeopleTypes.PEOPLE_ORG))
			return icons.get(PeopleTypes.PEOPLE_ORG);
		else
			return null;
	}
}