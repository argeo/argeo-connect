package org.argeo.connect.people.rap.toolkits;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.UserManagementService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.utils.PeopleRapUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.PropertyDiff;
import org.argeo.jcr.VersionDiff;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class HistoryToolkit {
	private final static Log log = LogFactory.getLog(HistoryToolkit.class);

	private Repository repository;
	private PeopleService peopleService;

	// private List<String> relevantAttributeList;

	// private List<String> knownPrefixes;

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private Node entity;

	private DateFormat dateTimeFormat = new SimpleDateFormat(
			PeopleRapConstants.DEFAULT_DATE_TIME_FORMAT);

	public HistoryToolkit(FormToolkit toolkit, IManagedForm form,
			Repository repository, PeopleService peopleService, Node entity) {
		this.toolkit = toolkit;
		this.form = form;
		this.entity = entity;
		this.peopleService = peopleService;
		this.repository = repository;
	}

	public void populateHistoryPanel(Composite parent) {
		try {
			parent.setLayout(PeopleRapUtils.noSpaceGridLayout());

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
			historyCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
					true));
			historyCmp.setLayout(new FillLayout());
			final Text styledText = toolkit.createText(historyCmp, "",
					SWT.BORDER | SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
			// styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
			// true));
			refreshHistory(styledText);
			// styledText.setEditable(false);

			AbstractFormPart part = new AbstractFormPart() {
				public void commit(boolean onSave) {
					if (onSave)
						super.commit(onSave);
				}

				public void refresh() {
					super.refresh();
					refreshHistory(styledText);
				}
			};
			part.initialize(form);
			form.addPart(part);
		} catch (Exception e) {
			throw new PeopleException(
					"Unexpected error while creating history form part", e);
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

				StringBuffer buf = new StringBuffer("");
				Map<String, PropertyDiff> diffs = lst.get(i).getDiffs();
				props: for (String prop : diffs.keySet()) {
					PropertyDiff pd = diffs.get(prop);

					String propName = pd.getRelPath();
					if ("jcr:uuid".equals(propName))
						continue props;

					Value refValue = pd.getReferenceValue();
					Value newValue = pd.getNewValue();
					String refValueStr = "";
					String newValueStr = "";

					if (refValue != null) {
						if (refValue.getType() == PropertyType.DATE) {
							refValueStr = dateTimeFormat.format(refValue
									.getDate().getTime());
						} else
							refValueStr = refValue.getString();
					}
					if (newValue != null) {
						if (newValue.getType() == PropertyType.DATE) {
							newValueStr = dateTimeFormat.format(newValue
									.getDate().getTime());
						} else
							newValueStr = newValue.getString();
					}

					// TODO Check if current property is part of the relevant
					// fields.
					// if (!relevantAttributeList.contains(propName))
					// continue props;

					if (pd.getType() == PropertyDiff.MODIFIED) {
						buf.append(propLabel(propName)).append(": ");
						buf.append(refValueStr);
						buf.append(" > ");
						buf.append(newValueStr);
						buf.append("\n");
					} else if (pd.getType() == PropertyDiff.ADDED
							&& !"".equals(newValueStr)) {
						// we don't list property that have been added with an
						// empty string as value
						buf.append(propLabel(propName)).append(": ");
						buf.append(" + ");
						buf.append(newValueStr);
						buf.append("\n");
					} else if (pd.getType() == PropertyDiff.REMOVED) {
						buf.append(propLabel(propName)).append(": ");
						buf.append(" - ");
						buf.append(refValueStr);
						buf.append("\n");
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

	private List<VersionDiff> listHistoryDiff() {
		Session session = null;
		try {
			session = repository.login();
			List<VersionDiff> res = new ArrayList<VersionDiff>();
			VersionManager versionManager = session.getWorkspace()
					.getVersionManager();
			VersionHistory versionHistory = versionManager
					.getVersionHistory(entity.getPath());

			VersionIterator vit = versionHistory.getAllLinearVersions();
			boolean first = true;
			while (vit.hasNext()) {
				Version version = vit.nextVersion();
				Node node = version.getFrozenNode();

				if (first && node != null && log.isTraceEnabled()) {
					// Helper to easily find the path of the technical
					// node under jcr:system/jcr:versionStorage that
					// manage versioning
					if (log.isTraceEnabled())
						log.trace("Retrieving history using frozenNode : "
								+ node);
					first = false;
				}

				Version predecessor = null;
				try {
					predecessor = version.getLinearPredecessor();
				} catch (Exception e) {
					// no predecessor seems to throw an exception even if it
					// shouldn't...
					// e.printStackTrace();
				}
				if (predecessor == null) {// original
				} else {
					Map<String, PropertyDiff> diffs = CommonsJcrUtils
							.diffProperties(predecessor.getFrozenNode(), node);
					if (!diffs.isEmpty()) {
						VersionDiff vd;
						vd = new VersionDiff(
								node.hasProperty(Property.JCR_LAST_MODIFIED_BY) ? node
										.getProperty(
												Property.JCR_LAST_MODIFIED_BY)
										.getString() : null,
								node.hasProperty(Property.JCR_LAST_MODIFIED) ? node
										.getProperty(Property.JCR_LAST_MODIFIED)
										.getDate()
										: null, diffs);
						res.add(vd);
					}
				}
			}
			return res;
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot generate history for entity", e);
		} finally {
			JcrUtils.logoutQuietly(session);
		}

	}

	// /** Small hack to enhence prop label rendering **/
	// public void setKnownPrefixes(List<String> knownPrefixes)
	// {
	// this.knownPrefixes = knownPrefixes;
	// }

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

		// try {
		// String prop = str.substring("people:".length());
		// return PeopleUiPlugin.getMessage("extractLbl." + prop);
		// } catch (Exception e) {
		// // property not listed in internationalization file
		// return str;
		// }
	}

}
