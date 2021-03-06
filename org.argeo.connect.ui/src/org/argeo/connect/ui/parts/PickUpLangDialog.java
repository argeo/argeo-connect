package org.argeo.connect.ui.parts;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.PropertyType;
import javax.jcr.Session;

import org.argeo.connect.ConnectConstants;
import org.argeo.connect.resources.ResourcesNames;
import org.argeo.connect.resources.ResourcesService;
import org.argeo.connect.resources.ResourcesTypes;
import org.argeo.connect.ui.widgets.SimpleJcrTableComposite;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.eclipse.ui.jcr.lists.JcrColumnDefinition;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/** Dialog with a filtered list to choose a language */
public class PickUpLangDialog extends TrayDialog {
	private static final long serialVersionUID = 3766899676609659573L;

	// Context
	private ResourcesService resourceService;
	private final Session session;
	private Node selectedNode;

	// this page widgets and UI objects
	private SimpleJcrTableComposite tableCmp;
	private final String title;

	private List<JcrColumnDefinition> colDefs = new ArrayList<JcrColumnDefinition>();
	{ // By default, it displays only title
		colDefs.add(new JcrColumnDefinition(null, ResourcesNames.RESOURCES_TAG_CODE, PropertyType.STRING, "Iso Code", 100));
		colDefs.add(new JcrColumnDefinition(null, Property.JCR_TITLE, PropertyType.STRING, "Label", 300));
	};

	public PickUpLangDialog(Shell parentShell, ResourcesService resourceService, Session session, String title) {
		super(parentShell);
		this.title = title;
		this.session = session;
		this.resourceService = resourceService;
	}

	protected Point getInitialSize() {
		return new Point(600, 400);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		Node langTagParent = resourceService.getTagLikeResourceParent(session, ConnectConstants.RESOURCE_LANG);

		int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
		tableCmp = new SimpleJcrTableComposite(dialogArea, style, session, ConnectJcrUtils.getPath(langTagParent),
				ResourcesTypes.RESOURCES_ENCODED_TAG, colDefs, true, false);
		tableCmp.setLayoutData(EclipseUiUtils.fillAll());

		// Add listeners
		tableCmp.getTableViewer().addDoubleClickListener(new MyDoubleClickListener());
		tableCmp.getTableViewer().addSelectionChangedListener(new MySelectionChangedListener());

		parent.pack();
		return dialogArea;
	}

	public String getSelected() {
		if (selectedNode != null)
			return ConnectJcrUtils.get(selectedNode, ResourcesNames.RESOURCES_TAG_CODE);
		else
			return null;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty())
				selectedNode = null;
			else {
				Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (obj instanceof Node) {
					selectedNode = (Node) obj;
				}
			}
		}
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty()) {
				selectedNode = null;
				return;
			} else {
				Object obj = ((IStructuredSelection) evt.getSelection()).getFirstElement();
				if (obj instanceof Node) {
					selectedNode = (Node) obj;
					okPressed();
				}
			}
		}
	}
}
