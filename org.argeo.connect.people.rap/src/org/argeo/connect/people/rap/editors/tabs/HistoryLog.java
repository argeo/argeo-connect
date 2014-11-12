package org.argeo.connect.people.rap.editors.tabs;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.PropertyType;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.core.versioning.ItemDiff;
import org.argeo.connect.people.core.versioning.VersionDiff;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.PropertyDiff;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * A composite to include in a form and that displays the evolutions of a given
 * versionable Node over the time.
 */
public class HistoryLog extends Composite {
	private static final long serialVersionUID = -4736848221960630767L;
	// private final static Log log = LogFactory.getLog(HistoryLog.class);

	private final FormToolkit toolkit;
	private final PeopleService peopleService;
	// private final PeopleWorkbenchService peopleWorkbenchService;
	private final Node entity;
	private DateFormat dateTimeFormat = new SimpleDateFormat(
			PeopleUiConstants.DEFAULT_DATE_TIME_FORMAT);

	// this page UI Objects
	private MyFormPart myFormPart;

	public HistoryLog(FormToolkit toolkit, IManagedForm form, Composite parent,
			int style, PeopleService peopleService, Node entity) {
		// PeopleWorkbenchService peopleWorkbenchService,
		super(parent, style);
		this.toolkit = toolkit;
		this.peopleService = peopleService;
		// this.peopleWorkbenchService = peopleWorkbenchService;
		this.entity = entity;

		// Populate
		populate(form, this);
	}

	private void populate(IManagedForm form, Composite parent) {

		parent.setLayout(PeopleUiUtils.noSpaceGridLayout());

		UserManagementService userService = peopleService
				.getUserManagementService();

		if (userService.isUserInRole(PeopleConstants.ROLE_BUSINESS_ADMIN)
				|| userService.isUserInRole(PeopleConstants.ROLE_ADMIN)) {
			Label label = new Label(parent, SWT.NONE);
			label.setText("People UID: "
					+ CommonsJcrUtils.get(entity, PeopleNames.PEOPLE_UID));
			GridData gd = new GridData(SWT.FILL, SWT.TOP, true, false);
			gd.verticalIndent = 3;
			gd.horizontalIndent = 5;
			label.setLayoutData(gd);
		}
		Composite historyCmp = new Composite(parent, SWT.NONE);
		historyCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		historyCmp.setLayout(new FillLayout());
		final Text styledText = toolkit.createText(historyCmp, "", SWT.BORDER
				| SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		// styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
		// true));
		refreshHistory(styledText);
		// styledText.setEditable(false);

		myFormPart = new MyFormPart(styledText);
		myFormPart.initialize(form);
		form.addPart(myFormPart);
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
			List<VersionDiff> lst = listHistoryDiff();
			StringBuffer main = new StringBuffer("");

			for (int i = lst.size() - 1; i >= 0; i--) {
				if (i == 0)
					main.append("Creation (");
				else
					main.append("Update " + i + " (");

				if (lst.get(i).getUserId() != null)
					main.append("User : "
							+ peopleService.getUserManagementService()
									.getUserDisplayName(lst.get(i).getUserId())
							+ ", ");
				if (lst.get(i).getUpdateTime() != null) {
					main.append("Date : ");
					main.append(dateTimeFormat.format(lst.get(i)
							.getUpdateTime().getTime()));
				}
				main.append(")\n");

				StringBuilder buf = new StringBuilder();
				Map<String, ItemDiff> diffs = lst.get(i).getDiffs();
				loop: for (String prop : diffs.keySet()) {

					ItemDiff pd = diffs.get(prop);

					String propName = pd.getRelPath();
					if (propName.endsWith("jcr:uuid"))
						continue loop;
					// TODO Check if current property is part of the relevant
					// fields.
					// if (!relevantAttributeList.contains(propName))
					// continue props;

					Item refItem = pd.getReferenceItem();
					Item newItem = pd.getObservedItem();

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
							appendModif(buf, pd.getType(),
									propLabel(pd.getRelPath()), refValueStr,
									newValueStr);
						}
					else { // node
						String refStr = refItem == null ? null
								: ((Node) refItem).getName();
						String obsStr = newItem == null ? null
								: ((Node) newItem).getName();
						appendModif(buf, pd.getType(), pd.getRelPath(), refStr,
								obsStr);
						if (pd.getType() == ItemDiff.ADDED)
							appendNodeProperties(buf, ItemDiff.ADDED,
									(Node) newItem);
						else if (pd.getType() == ItemDiff.REMOVED)
							appendNodeProperties(buf, ItemDiff.REMOVED,
									(Node) refItem);
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

	private void appendNodeProperties(StringBuilder buf, Integer type, Node node)
			throws RepositoryException {
		PropertyIterator pit = node.getProperties();
		loop: while (pit.hasNext()) {
			Property prop = pit.nextProperty();
			String propName = prop.getName();
			if (propName.endsWith("jcr:uuid"))
				continue loop;

			if (prop.isMultiple())
				; // TODO
			else {
				buf.append("\t");
				buf.append(propLabel(propName)).append(": ");
				buf.append(type == ItemDiff.ADDED ? " + " : " - ");
				buf.append(getValueAsString(prop.getValue()));
				buf.append("\n");
			}

		}

	}

	private void appendModif(StringBuilder buf, Integer type, String label,
			String oldValue, String newValue) {

		if (type == PropertyDiff.MODIFIED) {
			buf.append(label).append(": ");
			buf.append(oldValue);
			buf.append(" > ");
			buf.append(newValue);
			buf.append("\n");
		} else if (type == PropertyDiff.ADDED && !"".equals(newValue)) {
			// we don't list property that have been added with an
			// empty string as value
			buf.append(label).append(": ");
			buf.append(" + ");
			buf.append(newValue);
			buf.append("\n");
		} else if (type == PropertyDiff.REMOVED) {
			buf.append(label).append(": ");
			buf.append(" - ");
			buf.append(oldValue);
			buf.append("\n");
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
				appendModif(builder, PropertyDiff.REMOVED,
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
				appendModif(builder, PropertyDiff.ADDED,
						propLabel(obsProp.getName()), null,
						getValueAsString(newValue));
			}
	}

	private List<VersionDiff> listHistoryDiff() {
		try {
			Session session = entity.getSession();
			List<VersionDiff> res = new ArrayList<VersionDiff>();
			VersionManager versionManager = session.getWorkspace()
					.getVersionManager();
			VersionHistory versionHistory = versionManager
					.getVersionHistory(entity.getPath());

			VersionIterator vit = versionHistory.getAllLinearVersions();
			// boolean first = true;
			while (vit.hasNext()) {
				Version version = vit.nextVersion();
				Node node = version.getFrozenNode();

				Version predecessor = null;
				try {
					predecessor = version.getLinearPredecessor();
				} catch (Exception e) {
					// no predecessor throw an exception even if it shouldn't...
					// e.printStackTrace();
				}
				if (predecessor == null) {// original
				} else {
					Map<String, ItemDiff> diffs = CommonsJcrUtils.diffItems(
							predecessor.getFrozenNode(), node);
					if (!diffs.isEmpty()) {
						String userid = node
								.hasProperty(Property.JCR_LAST_MODIFIED_BY) ? node
								.getProperty(Property.JCR_LAST_MODIFIED_BY)
								.getString() : null;
						Calendar updateTime = node
								.hasProperty(Property.JCR_LAST_MODIFIED) ? node
								.getProperty(Property.JCR_LAST_MODIFIED)
								.getDate() : null;
						res.add(new VersionDiff(null, userid, updateTime, diffs));
					}
				}
			}
			return res;
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot generate history for node "
					+ entity, e);
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