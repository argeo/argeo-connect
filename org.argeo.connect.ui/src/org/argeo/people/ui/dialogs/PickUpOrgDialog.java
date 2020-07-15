package org.argeo.people.ui.dialogs;

import javax.jcr.Node;
import javax.jcr.Session;

import org.argeo.connect.ui.SystemWorkbenchService;
import org.argeo.connect.ui.parts.FilterEntitiesVirtualTable;
import org.argeo.people.PeopleTypes;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/** Dialog with a filtered list to choose an organisation */
public class PickUpOrgDialog extends TrayDialog {
	private static final long serialVersionUID = -2526572299370624808L;

	// Business objects
	private final Session session;
	private SystemWorkbenchService systemWorkbenchService;
	private Node selectedNode;

	// this page widgets and UI objects
	private FilterEntitiesVirtualTable tableCmp;
	private final String title;

	public PickUpOrgDialog(Shell parentShell, String title, Session session,
			SystemWorkbenchService systemWorkbenchService, Node referencingNode) {
		super(parentShell);
		this.title = title;
		this.session = session;
		this.systemWorkbenchService = systemWorkbenchService;
	}

	protected Point getInitialSize() {
		return new Point(400, 600);
	}

	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);

		// final Button seeAllChk = new Button(dialogArea, SWT.CHECK);
		// seeAllChk.setText("See all organisation");

		int style = SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL;
		tableCmp = new FilterEntitiesVirtualTable(dialogArea, style, session, systemWorkbenchService,
				PeopleTypes.PEOPLE_ORG);
		tableCmp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Add listeners
		tableCmp.getTableViewer().addDoubleClickListener(new MyDoubleClickListener());
		tableCmp.getTableViewer().addSelectionChangedListener(new MySelectionChangedListener());

		parent.pack();
		return dialogArea;
	}

	public Node getSelected() {
		return selectedNode;
	}

	protected void configureShell(Shell shell) {
		super.configureShell(shell);
		shell.setText(title);
	}

	class MySelectionChangedListener implements ISelectionChangedListener {
		@Override
		public void selectionChanged(SelectionChangedEvent event) {
			if (event.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) event.getSelection()).getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
			}
		}
	}

	class MyDoubleClickListener implements IDoubleClickListener {
		public void doubleClick(DoubleClickEvent evt) {
			if (evt.getSelection().isEmpty())
				return;

			Object obj = ((IStructuredSelection) evt.getSelection()).getFirstElement();
			if (obj instanceof Node) {
				selectedNode = (Node) obj;
				okPressed();
			}
		}
	}
}
