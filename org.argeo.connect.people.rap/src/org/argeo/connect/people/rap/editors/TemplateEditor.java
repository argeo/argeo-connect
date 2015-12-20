package org.argeo.connect.people.rap.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.ResourceService;
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.PeopleRapPlugin;
import org.argeo.connect.people.rap.editors.tabs.TemplateValueCatalogue;
import org.argeo.connect.people.rap.editors.util.AbstractPeopleCTabEditor;
import org.argeo.connect.people.rap.editors.util.IVersionedItemEditor;
import org.argeo.connect.people.util.JcrUiUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.rap.rwt.RWT;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.AbstractFormPart;

/**
 * Enable management of a given node template, among other static list
 * (catalogue) management
 */
public class TemplateEditor extends AbstractPeopleCTabEditor implements
		IVersionedItemEditor {

	final static Log log = LogFactory.getLog(TemplateEditor.class);
	public final static String ID = PeopleRapPlugin.PLUGIN_ID
			+ ".templateEditor";

	/* DEPENDENCY INJECTION */
	private ResourceService resourceService;

	// Context
	private Node nodeTemplate;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		nodeTemplate = getNode();
		// TODO workaround to manually add missing mixin
		try {
			if (!nodeTemplate.isNodeType(NodeType.MIX_VERSIONABLE))
				nodeTemplate.addMixin(NodeType.MIX_VERSIONABLE);
			if (!nodeTemplate.isNodeType(NodeType.MIX_LAST_MODIFIED))
				nodeTemplate.addMixin(NodeType.MIX_LAST_MODIFIED);
			Session session = nodeTemplate.getSession();
			if (session.hasPendingChanges()) {
				session.save();
				log.warn("Versionable and last modified mixins "
						+ "have been added to " + nodeTemplate);
			}
		} catch (RepositoryException e) {
			throw new PeopleException(
					"Unable to add missing mixins for node template: "
							+ nodeTemplate, e);
		}

		String shortName = resourceService
				.getItemDefaultEnLabel(JcrUiUtils.get(nodeTemplate,
						PeopleNames.PEOPLE_TEMPLATE_ID));
		setPartName(shortName);
		resourceService = getPeopleService().getResourceService();
	}

	protected void populateTabFolder(CTabFolder folder) {
		String tooltip;
		Composite innerPannel;

		try {
			PropertyIterator pit = nodeTemplate.getProperties();

			loop: while (pit.hasNext()) {
				Property property = pit.nextProperty();

				String propName = property.getName();

				// TODO make this more robust
				if (!property.isMultiple() || propName.startsWith("jcr:"))
					continue loop;

				// TODO enhance
				String propLabel = propName;

				tooltip = "Manage and edit the \"" + propLabel + "\" catalogue";
				innerPannel = addTabToFolder(folder, PeopleRapConstants.CTAB_COMP_STYLE,
						propLabel, PeopleRapConstants.CTAB_EDIT_CATALOGUE + "/"
								+ propName, tooltip);
				innerPannel.setLayout(EclipseUiUtils.noSpaceGridLayout());
				Composite oneBusinessPropertyCatalogue = new TemplateValueCatalogue(
						getSite().getWorkbenchWindow().getWorkbench(), this,
						innerPannel, SWT.NO_FOCUS, getPeopleService(),
						getPeopleWorkbenchService(), nodeTemplate, propName,
						JcrUiUtils.get(nodeTemplate,
								PeopleNames.PEOPLE_TEMPLATE_ID));
				oneBusinessPropertyCatalogue.setLayoutData(EclipseUiUtils.fillAll());
			}
		} catch (RepositoryException e) {
			throw new PeopleException(
					"unable to create property tabs for node template "
							+ nodeTemplate, e);
		}
	}

	@Override
	protected void populateHeader(final Composite parent) {

		try {
			parent.setLayout(new GridLayout());
			final Label editionInfoROLbl = getManagedForm().getToolkit()
					.createLabel(parent, "", SWT.WRAP);
			editionInfoROLbl.setData(RWT.MARKUP_ENABLED, Boolean.TRUE);
			final ColumnLabelProvider festivalLP = new EditionOLP();

			final AbstractFormPart editPart = new AbstractFormPart() {
				// Update values on refresh
				public void refresh() {
					super.refresh();
					String roText = festivalLP.getText(nodeTemplate);
					editionInfoROLbl.setText(roText);
					parent.layout();
				}
			};
			editPart.initialize(getManagedForm());
			getManagedForm().addPart(editPart);
		} catch (Exception e) {
			throw new PeopleException("Cannot create main info section", e);
		}
	}

	// @Override
	// protected Boolean deleteParentOnRemove() {
	// return false;
	// }

	@Override
	protected boolean showDeleteButton() {
		return false;
	}

	private class EditionOLP extends ColumnLabelProvider {

		private static final long serialVersionUID = 1L;

		@Override
		public String getText(Object element) {
			Node node = (Node) element;

			StringBuilder builder = new StringBuilder();
			builder.append("<span style='font-size:15px;'>");

			// first line
			builder.append("<b><big>");
			builder.append(JcrUiUtils.get(node,
					PeopleNames.PEOPLE_TEMPLATE_ID));
			builder.append("</big></b> ");
			// builder.append("<br/><i>");
			// builder.append(JcrUiUtils.get(editionInfo,
			// Property.JCR_DESCRIPTION));
			// builder.append("</i>");
			// builder.append(getFromToSnippet(editionInfo));
			builder.append("</span>");
			return builder.toString();
		}
	}

	@Override
	public void setPeopleService(PeopleService peopleService) {
		super.setPeopleService(peopleService);
		resourceService = peopleService.getResourceService();
	}
}