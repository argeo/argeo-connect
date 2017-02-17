package org.argeo.connect.people.web.pages;

import java.util.Map;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.cms.ui.CmsUiProvider;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.web.parts.PeopleSearchCmp;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/** Default cms page layout for the People apps */
public class PeopleDefaultPage implements CmsUiProvider {

	private ResourcesService resourceService;
	private PeopleService peopleService;
	private Map<String, String> peopleIconPaths;

	// Local UI Providers
	private CmsUiProvider orgPage;
	private CmsUiProvider personPage;
	// private CmsUiProvider tagLikeInstancePage;

	public PeopleDefaultPage(ResourcesService resourceService, PeopleService peopleService,
			Map<String, String> peopleIconPaths) {
		this.peopleService = peopleService;
		this.resourceService = resourceService;
		this.peopleIconPaths = peopleIconPaths;
		orgPage = new OrgPage(resourceService, peopleService);
		personPage = new PersonPage(peopleService, resourceService);
	}

	@Override
	public Control createUi(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		SashForm form = new SashForm(parent, SWT.HORIZONTAL);
		form.setLayoutData(EclipseUiUtils.fillAll());
		Composite leftPannelCmp = new Composite(form, SWT.NO_FOCUS);
		Composite rightPannelCmp = new Composite(form, SWT.NO_FOCUS);
		form.setWeights(new int[] { 2, 4 });

		// A search on the left and the display on the right
		populateSearch(leftPannelCmp, context, rightPannelCmp);
		// default display
		if (!context.isNodeType(PeopleTypes.PEOPLE_ENTITY))
			populateDefaultDisplay(rightPannelCmp, context);
		else
			refreshDisplay(rightPannelCmp, context);

		return form;
	}

	public Viewer populateSearch(Composite parent, Node context, final Composite targetComposite)
			throws RepositoryException {
		parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
		PeopleSearchCmp searchComp = new PeopleSearchCmp(parent, SWT.NO_FOCUS, resourceService, peopleService,
				peopleIconPaths);
		searchComp.populate(context, true);
		searchComp.setLayoutData(EclipseUiUtils.fillAll());

		TableViewer viewer = searchComp.getViewer();
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				Object firstObj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				try {
					refreshDisplay(targetComposite, (Node) firstObj);
				} catch (RepositoryException e) {
					throw new PeopleException("unable to refresh display for " + context, e);
				}
			}
		});
		return null;
	}

	public Control refreshDisplay(Composite parent, Node context) throws RepositoryException {
		EclipseUiUtils.clear(parent);
		if (context.isNodeType(PeopleTypes.PEOPLE_PERSON))
			return personPage.createUi(parent, context);
		else if (context.isNodeType(PeopleTypes.PEOPLE_ORG))
			return orgPage.createUi(parent, context);
		return null;
	}

	public Control populateDefaultDisplay(Composite parent, Node context) throws RepositoryException {
		parent.setLayout(new GridLayout());
		Label lbl = new Label(parent, SWT.NONE);
		lbl.setText("Implement a default display");
		return lbl;
	}
}
