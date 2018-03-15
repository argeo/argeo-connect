package org.argeo.people.workbench.rap.composites;

import javax.jcr.Node;

import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.workbench.SystemWorkbenchService;
import org.argeo.connect.workbench.TagLikeListPart;
import org.argeo.connect.workbench.parts.AbstractConnectEditor;
import org.argeo.people.PeopleNames;
import org.eclipse.swt.widgets.Composite;

/** Extend {@code TagLikeListPart} to provide specific label */
public class MailingListListPart extends TagLikeListPart {
	private static final long serialVersionUID = 6569267624309620699L;

	public MailingListListPart(AbstractConnectEditor editor, Composite parent, int style,
			ResourcesService resourceService, SystemWorkbenchService systemWorkbenchService, String tagId,
			Node taggable, String taggablePropName, String newTagMsg) {
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
