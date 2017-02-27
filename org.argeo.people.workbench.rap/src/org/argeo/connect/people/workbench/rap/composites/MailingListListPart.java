package org.argeo.connect.people.workbench.rap.composites;

import javax.jcr.Node;

import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.ui.widgets.TagLikeListPart;
import org.argeo.connect.ui.workbench.AppWorkbenchService;
import org.argeo.connect.ui.workbench.parts.AbstractConnectEditor;
import org.eclipse.swt.widgets.Composite;

public class MailingListListPart extends TagLikeListPart {
	private static final long serialVersionUID = 6569267624309620699L;

	public MailingListListPart(AbstractConnectEditor editor, Composite parent, int style,
			ResourcesService resourceService, AppWorkbenchService appWorkbenchService, String tagId, Node taggable,
			String taggablePropName, String newTagMsg) {
		super(editor, parent, style, resourceService, appWorkbenchService, tagId, taggable, taggablePropName,
				newTagMsg);
	}

	protected String getLinkText(String taggablePropName, String value) {
		if (taggablePropName.equals(PeopleNames.PEOPLE_MAILING_LISTS))
			return " @<a>" + value + "</a>";
		else
			return super.getLinkText(taggablePropName, value);
	}
}
