package org.argeo.connect.people.ui.toolkits;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelConstants;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.PeopleImages;
import org.argeo.connect.people.ui.PeopleUiConstants;
import org.argeo.connect.people.ui.commands.AddEntityReference;
import org.argeo.connect.people.ui.commands.OpenEntityEditor;
import org.argeo.connect.people.ui.composites.ContactPanelComposite;
import org.argeo.connect.people.ui.utils.PeopleUiUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.eclipse.ui.utils.CommandUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Centralizes creation of commonly used people contact controls (typically Text
 * and composite widget) to be used in various forms.
 */
public class ContactToolkit {

	private final FormToolkit toolkit;
	private final IManagedForm form;
	private final PeopleService peopleService;

	public ContactToolkit(FormToolkit toolkit, IManagedForm form,
			PeopleService peopleService) {
		this.toolkit = toolkit;
		this.form = form;
		this.peopleService = peopleService;
	}

	/**
	 * Populate a parent composite with controls to manage mailing list
	 * membership of an organisation or a person
	 * 
	 * @param parent
	 * @param entity
	 */
	public void populateMailingListMembershipPanel(final Composite parent,
			final Node entity, final String openEditorCmdId) {
		GridLayout gl = PeopleUiUtils.gridLayoutNoBorder(2);
		gl.marginBottom = 5;
		parent.setLayout(gl);

		final Composite nlCmp = new Composite(parent, SWT.NO_FOCUS);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, false);
		nlCmp.setLayoutData(gd);

		RowLayout rl = new RowLayout(SWT.HORIZONTAL);
		rl.wrap = true;
		rl.marginHeight = 0;
		rl.marginLeft = 5;
		rl.marginRight = 0;
		nlCmp.setLayout(rl);

		final Button addBtn = new Button(parent, SWT.PUSH);
		addBtn.setText("Add new mailing list(s)");
		gd = new GridData(SWT.CENTER, SWT.TOP, false, false);
		gd.widthHint = 135;
		addBtn.setLayoutData(gd);

		AbstractFormPart editPart = new AbstractFormPart() {
			public void refresh() {
				super.refresh();
				// We redraw the full control at each refresh, might be a more
				// efficient way to do
				Control[] oldChildren = nlCmp.getChildren();
				for (Control child : oldChildren)
					child.dispose();

				try {
					List<Node> referencings = getNodeReferencing(entity);

					for (final Node node : referencings) {
						final Node parNode = getParentMailingList(node);
						Composite tagCmp = toolkit.createComposite(nlCmp,
								SWT.NO_FOCUS);
						tagCmp.setLayout(PeopleUiUtils.gridLayoutNoBorder(2));

						Link link = new Link(tagCmp, SWT.NONE);
						link.setText("<a>"
								+ CommonsJcrUtils.get(parNode,
										Property.JCR_TITLE) + "</a>");
						link.setToolTipText(CommonsJcrUtils.get(parNode,
								Property.JCR_DESCRIPTION));
						link.setData(PeopleUiConstants.MARKUP_ENABLED,
								Boolean.TRUE);

						link.addSelectionListener(new SelectionAdapter() {
							private static final long serialVersionUID = 1L;

							@Override
							public void widgetSelected(
									final SelectionEvent event) {
								Map<String, String> params = new HashMap<String, String>();
								params.put(OpenEntityEditor.PARAM_ENTITY_UID,
										CommonsJcrUtils.get(parNode,
												PeopleNames.PEOPLE_UID));
								CommandUtils.callCommand(openEditorCmdId,
										params);
							}
						});

						if (CommonsJcrUtils.isNodeCheckedOutByMe(entity)) {
							final Button deleteBtn = new Button(tagCmp,
									SWT.FLAT);
							deleteBtn.setData(PeopleUiConstants.CUSTOM_VARIANT,
									PeopleUiConstants.CSS_FLAT_IMG_BUTTON);
							deleteBtn.setImage(PeopleImages.DELETE_BTN_LEFT);

							deleteBtn
									.addSelectionListener(new SelectionAdapter() {
										private static final long serialVersionUID = 1L;

										@Override
										public void widgetSelected(
												final SelectionEvent event) {
											try {
												boolean wasCheckedOut = CommonsJcrUtils
														.isNodeCheckedOutByMe(parNode);
												if (!wasCheckedOut)
													CommonsJcrUtils
															.checkout(parNode);
												node.remove();
												if (wasCheckedOut)
													parNode.getSession().save();
												else
													CommonsJcrUtils
															.saveAndCheckin(parNode);
											} catch (RepositoryException e) {
												throw new PeopleException(
														"unable to initialise deletion",
														e);
											}
											for (IFormPart part : form
													.getParts()) {
												((AbstractFormPart) part)
														.markStale();
												part.refresh();
											}
										}
									});
						}
					}
					addBtn.setVisible(CommonsJcrUtils
							.isNodeCheckedOutByMe(entity));
					nlCmp.layout(false);
					parent.getParent().layout();
				} catch (RepositoryException re) {
					throw new PeopleException(
							"Error while refreshing mailing list appartenance",
							re);
				}
			}
		};

		addBtn.addSelectionListener(new SelectionAdapter() {
			private static final long serialVersionUID = 1L;

			@Override
			public void widgetSelected(final SelectionEvent event) {
				Map<String, String> params = new HashMap<String, String>();
				try {
					params.put(AddEntityReference.PARAM_REFERENCED_JCR_ID,
							entity.getIdentifier());
					params.put(AddEntityReference.PARAM_TO_SEARCH_NODE_TYPE,
							PeopleTypes.PEOPLE_MAILING_LIST);
					params.put(AddEntityReference.PARAM_DIALOG_ID,
							PeopleUiConstants.DIALOG_ADD_ML_MEMBERSHIP);
					CommandUtils.callCommand(AddEntityReference.ID, params);
				} catch (RepositoryException e1) {
					throw new PeopleException(
							"Unable to get parent Jcr identifier", e1);
				}
			}
		});

		editPart.initialize(form);
		form.addPart(editPart);
	}

	/** Recursively retrieves the parent Mailing list **/
	private Node getParentMailingList(Node node) throws RepositoryException {
		if (node.isNodeType(PeopleTypes.PEOPLE_MAILING_LIST))
			return node;
		else
			return getParentMailingList(node.getParent());
	}

	private List<Node> getNodeReferencing(Node entity) {
		try {
			Session session = entity.getSession();
			QueryManager queryManager = session.getWorkspace()
					.getQueryManager();
			QueryObjectModelFactory qomFactory = queryManager.getQOMFactory();

			Selector source = qomFactory.selector(
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM,
					PeopleTypes.PEOPLE_MAILING_LIST_ITEM);

			// Parse the String
			StaticOperand so = qomFactory.literal(entity.getProperty(
					PeopleNames.PEOPLE_UID).getValue());
			DynamicOperand dop = qomFactory.propertyValue(
					source.getSelectorName(), PeopleNames.PEOPLE_REF_UID);
			Constraint defaultC = qomFactory.comparison(dop,
					QueryObjectModelConstants.JCR_OPERATOR_EQUAL_TO, so);

			QueryObjectModel query;
			query = qomFactory.createQuery(source, defaultC, null, null);
			QueryResult result = query.execute();
			NodeIterator ni = result.getNodes();

			return JcrUtils.nodeIteratorToList(ni);
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to list entities", e);
		}
	}

	public void createContactPanelWithNotes(Composite parent, final Node entity) {

		parent.setLayout(PeopleUiUtils.gridLayoutNoBorder());
		ContactPanelComposite cpc = new ContactPanelComposite(parent,
				SWT.NO_FOCUS, toolkit, form, entity, peopleService);
		cpc.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
}