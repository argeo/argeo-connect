package org.argeo.people.ui.composites;

import javax.jcr.Node;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.ConnectEditor;
import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.parts.TagLikeListPart;
import org.argeo.people.PeopleNames;
import org.eclipse.swt.widgets.Composite;

/** Extend {@code TagLikeListPart} to provide specific label */
public class MailingListListPart extends TagLikeListPart {
	private static final long serialVersionUID = 6569267624309620699L;

	public MailingListListPart(ConnectEditor editor, Composite parent, int style, ResourcesService resourceService,
			SystemWorkbenchService systemWorkbenchService, String tagId, Node taggable, String taggablePropName,
			String newTagMsg) {
		super(editor, parent, style, resourceService, systemWorkbenchService, tagId, taggable, taggablePropName,
				newTagMsg);
	}

	protected String getLinkText(String taggablePropName, String value) {
		if (taggablePropName.equals(PeopleNames.PEOPLE_MAILING_LISTS))
			return " @<a>" + value + "</a>";
		else
			return super.getLinkText(taggablePropName, value);
	}
}
