package org.argeo.connect.people.rap.editors.tabs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Value;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.core.versioning.ItemDiff;
import org.argeo.connect.people.core.versioning.VersionDiff;
import org.argeo.connect.people.core.versioning.VersionUtils;
import org.argeo.connect.people.rap.editors.utils.AbstractPeopleEditor;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.PropertyDiff;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A composite to include in a form and that displays the evolutions of a given
 * versionable Node over the time.
 */
public class HistoryLog extends Composite {
	private static final long serialVersionUID = -4736848221960630767L;
	// private final static Log log = LogFactory.getLog(HistoryLog.class);

	private final AbstractPeopleEditor editor;
	private final FormToolkit toolkit;
	private final PeopleService peopleService;
	// private final PeopleWorkbenchService peopleWorkbenchService;
	private final Node entity;
	private DateFormat dateTimeFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	// this page UI Objects
	private MyFormPart myFormPart;

	public HistoryLog(AbstractPeopleEditor editor, Composite parent, int style,
			PeopleService peopleService, Node entity) {
		// PeopleWorkbenchService peopleWorkbenchService,
		super(parent, style);
		this.editor = editor;
		this.toolkit = editor.getFormToolkit();
		this.peopleService = peopleService;
		// this.peopleWorkbenchService = peopleWorkbenchService;
		this.entity = entity;

		// Populate
		populate(this);
	}

	private void populate(Composite parent) {

		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());

		UserManagementService userService = peopleService
				.getUserManagementService();
		// Add info to be able to find the node via the data explorer
		if (userService.isUserInRole(PeopleConstants.ROLE_BUSINESS_ADMIN)
				|| userService.isUserInRole(PeopleConstants.ROLE_ADMIN)) {
			Label label = new Label(parent, SWT.WRAP);
			CmsUtils.markup(label);
			GridData gd = EclipseUiUtils.fillWidth();
			gd.verticalIndent = 3;
			gd.horizontalIndent = 5;
			label.setLayoutData(gd);
			StringBuilder builder = new StringBuilder();
			String puid = CommonsJcrUtils.get(entity, PeopleNames.PEOPLE_UID);
			if (CommonsJcrUtils.checkNotEmptyString(puid)) {
				builder.append("People UID: ").append(puid);
				builder.append(" <br/>");
			}
			builder.append("Path: ").append(CommonsJcrUtils.getPath(entity));
			label.setText(builder.toString());
		}
		Composite historyCmp = new Composite(parent, SWT.NONE);
		historyCmp.setLayoutData(EclipseUiUtils.fillAll());
		historyCmp.setLayout(new FillLayout());
		final Text styledText = toolkit.createText(historyCmp, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		myFormPart = new MyFormPart(styledText);
		myFormPart.initialize(editor.getManagedForm());
		editor.getManagedForm().addPart(myFormPart);
	}

	private class MyFormPart extends AbstractFormPart {
		private final Text text;

		public MyFormPart(Text text) {
			this.text = text;
		}

		@Override
		public void refresh() {
			super.refresh();
			refreshHistory(text);
		}
	}

	protected void refreshHistory(Text styledText) {
		try {
			List<VersionDiff> lst = VersionUtils.listHistoryDiff(entity,
					VersionUtils.DEFAULT_FILTERED_OUT_PROP_NAMES);
			StringBuilder main = new StringBuilder();

			for (int i = lst.size() - 1; i >= 0; i--) {
				StringBuilder firstL = new StringBuilder();
				if (i == 0)
					firstL.append("Creation (");
				else
					firstL.append("Update " + i + " (");

				if (lst.get(i).getUserId() != null)
					firstL.append("User : "
							+ peopleService.getUserManagementService()
									.getUserDisplayName(lst.get(i).getUserId())
							+ ", ");
				if (lst.get(i).getUpdateTime() != null) {
					firstL.append("Date : ");
					firstL.append(dateTimeFormat.format(lst.get(i)
							.getUpdateTime().getTime()));
				}
				firstL.append(")");
				String fl = firstL.toString();
				main.append(fl).append("\n");
				for (int j = 0; j < fl.length(); j++)
					main.append("=");
				main.append("\n");

				StringBuilder buf = new StringBuilder();
				Map<String, ItemDiff> diffs = lst.get(i).getDiffs();
				for (String prop : diffs.keySet()) {
					ItemDiff diff = diffs.get(prop);
					Item refItem = diff.getReferenceItem();
					Item newItem = diff.getObservedItem();
					Item tmpItem = refItem == null ? newItem : refItem;
					if (tmpItem instanceof Property)
						if (((Property) tmpItem).isMultiple())
							appendMultiplePropertyModif(buf,
									(Property) newItem, (Property) refItem);
						else {
							String refValueStr = "";
							String newValueStr = "";
							if (refItem != null)
								refValueStr = getValueAsString(((Property) refItem)
										.getValue());
							if (newItem != null)
								newValueStr = getValueAsString(((Property) newItem)
										.getValue());
							appendPropModif(buf, diff.getType(),
									propLabel(diff.getRelPath()), refValueStr,
									newValueStr);
						}
					else { // node
						String refStr = refItem == null ? null
								: ((Node) refItem).getName();
						String obsStr = newItem == null ? null
								: ((Node) newItem).getName();
						appendNodeModif(buf, (Node) newItem, diff.getType(),
								diff.getRelPath(), refStr, obsStr);
					}
				}
				buf.append("\n");
				main.append(buf);
			}
			styledText.setText(main.toString());
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Cannot generate history for current entity.", e);
		}
	}

	public void refresh() {
		myFormPart.refresh();
	}

	private String getValueAsString(Value refValue) throws RepositoryException {
		String refValueStr;
		if (refValue.getType() == PropertyType.DATE) {
			refValueStr = dateTimeFormat.format(refValue.getDate().getTime());
		}
		// TODO implement other type formatting if needed.
		else
			refValueStr = refValue.getString();

		return refValueStr;
	}

	private void appendPropModif(StringBuilder buf, Integer type, String label,
			String oldValue, String newValue) {

		if (type == ItemDiff.MODIFIED) {
			buf.append("\t");
			buf.append(label).append(": ");
			buf.append(oldValue);
			buf.append(" > ");
			buf.append(newValue);
			buf.append("\n");
		} else if (type == PropertyDiff.ADDED && !"".equals(newValue)) {
			// we don't list property that have been added with an
			// empty string as value
			buf.append("\t");
			buf.append(label).append(": ");
			buf.append(" + ");
			buf.append(newValue);
			buf.append("\n");
		} else if (type == PropertyDiff.REMOVED) {
			buf.append("\t");
			buf.append(label).append(": ");
			buf.append(" - ");
			buf.append(oldValue);
			buf.append("\n");
		}
	}

	private void appendNodeModif(StringBuilder buf, Node node, Integer type,
			String label, String oldValue, String newValue)
			throws RepositoryException {
		if (type == PropertyDiff.MODIFIED) {
			buf.append("Node ");
			buf.append(label).append(" modified: ");
			buf.append("\n");
		} else if (type == PropertyDiff.ADDED) {
			buf.append("Node ");
			buf.append(label).append(" added: ");
			buf.append("\n");
			appendAddedNodeProperties(buf, node, 1);
		} else if (type == PropertyDiff.REMOVED) {
			buf.append("Node ");
			buf.append(label).append(" removed: ");
			buf.append("\n");
		}
	}

	// Small hack to list the properties of a added sub node
	private void appendAddedNodeProperties(StringBuilder builder, Node node,
			int level) throws RepositoryException {
		PropertyIterator pit = node.getProperties();
		while (pit.hasNext()) {
			Property prop = pit.nextProperty();
			if (!VersionUtils.DEFAULT_FILTERED_OUT_PROP_NAMES.contains(prop
					.getName())) {
				String label = propLabel(prop.getName());
				for (int i = 0; i < level; i++)
					builder.append("\t");
				builder.append(label).append(": ");
				builder.append(" + ");
				if (prop.isMultiple())
					builder.append(CommonsJcrUtils.getMultiAsString(
							prop.getParent(), prop.getName(), "; "));
				else
					builder.append(getValueAsString(prop.getValue()));
				builder.append("\n");
			}
		}
		NodeIterator nit = node.getNodes();
		while (nit.hasNext()) {
			Node currNode = nit.nextNode();
			for (int i = 0; i < level; i++)
				builder.append("\t");
			builder.append("Sub Node ");
			builder.append(currNode.getName()).append(" added: ");
			builder.append("\n");
			appendAddedNodeProperties(builder, currNode, level + 1);
		}
	}

	private void appendMultiplePropertyModif(StringBuilder builder,
			Property obsProp, Property refProp) throws RepositoryException {

		Value[] refValues = null;
		if (refProp != null)
			refValues = refProp.getValues();

		Value[] newValues = null;
		if (obsProp != null)
			newValues = obsProp.getValues();
		if (refProp != null)
			refValues: for (Value refValue : refValues) {
				if (obsProp != null)
					for (Value newValue : newValues) {
						if (refValue.equals(newValue))
							continue refValues;
					}
				appendPropModif(builder, PropertyDiff.REMOVED,
						propLabel(refProp.getName()),
						getValueAsString(refValue), null);
			}
		if (obsProp != null)
			newValues: for (Value newValue : newValues) {
				if (refProp != null)
					for (Value refValue : refValues) {
						if (refValue.equals(newValue))
							continue newValues;
					}
				appendPropModif(builder, PropertyDiff.ADDED,
						propLabel(obsProp.getName()), null,
						getValueAsString(newValue));
			}
	}

	/** Small hack to enhance prop label rendering **/
	protected String propLabel(String str) {
		if (str.lastIndexOf(":") < 2)
			return str;
		else
			str = str.substring(str.lastIndexOf(":") + 1);

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < str.length(); i++) {
			char curr = str.charAt(i);
			if (i == 0)
				builder.append(Character.toUpperCase(curr));
			else if (Character.isUpperCase(curr))
				builder.append(" ").append(curr);
			else
				builder.append(curr);
		}

		return builder.toString();
	}
}