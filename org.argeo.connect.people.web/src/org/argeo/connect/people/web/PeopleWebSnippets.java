package org.argeo.connect.people.web;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.TagService;
import org.argeo.connect.people.ui.PeopleUiUtils;

public class PeopleWebSnippets {
	/** a snippet to display tags that are linked to the current entity */
	public static String getTagLikeValues(PeopleService peopleService,
			Node entity, String propertyName, String prefix) {
		StringBuilder builder = new StringBuilder();

		TagService tagService = peopleService.getTagService();
		try {
			Session session = entity.getSession();
			String tagParentPath = null;
			// TODO fix this
			if (PeopleNames.PEOPLE_MAILING_LISTS.equals(propertyName))
				tagParentPath = peopleService
						.getResourceBasePath(PeopleTypes.PEOPLE_MAILING_LIST);
			else
				tagParentPath = peopleService
						.getResourceBasePath(PeopleTypes.PEOPLE_TAG);
			if (entity.hasProperty(propertyName)) {
				for (Value value : entity.getProperty((propertyName))
						.getValues()) {
					String valueStr = value.getString();
					Node tag = tagService.getRegisteredTag(session,
							tagParentPath, valueStr);
					builder.append(prefix);
					builder.append("<a href=\"#" + tag.getPath() + "\">")
							.append(valueStr).append("</a> ");
				}
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Error while tag like property "
					+ propertyName + " values for node " + entity, e);
		}
		return PeopleUiUtils.replaceAmpersand(builder.toString().trim());
	}
}
