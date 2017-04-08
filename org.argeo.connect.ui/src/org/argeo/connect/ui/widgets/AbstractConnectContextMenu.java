package org.argeo.connect.ui.widgets;

import java.util.HashMap;
import java.util.Map;

import org.argeo.cms.util.CmsUtils;
import org.argeo.connect.ui.ConnectUiStyles;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Generic popup context menu for TableViewer to enable single sourcing between
 * CMS and Workbench
 */
public abstract class AbstractConnectContextMenu extends Shell {
	private static final long serialVersionUID = 5355397086046402434L;

	// Local context

	private final static String KEY_ACTION_ID = "actionId";
	private final String[] defaultActions;
	private Map<String, Button> actionButtons = new HashMap<String, Button>();

	public AbstractConnectContextMenu(Display display, String[] defaultActions) {
		super(display, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		this.defaultActions = defaultActions;
	}

	protected void createControl() {
		setLayout(EclipseUiUtils.noSpaceGridLayout());
		Composite boxCmp = new Composite(this, SWT.NO_FOCUS | SWT.BORDER);
		boxCmp.setLayout(EclipseUiUtils.noSpaceGridLayout());
		CmsUtils.style(boxCmp, ConnectUiStyles.CONTEXT_MENU_BOX);
		createContextMenu(boxCmp);
		addShellListener(new ActionsShellListener());
	}

	protected void createContextMenu(Composite boxCmp) {
		ActionsSelListener asl = new ActionsSelListener();
		for (String actionId : defaultActions) {
			Button btn = new Button(boxCmp, SWT.FLAT | SWT.PUSH | SWT.LEAD);
			btn.setText(getLabel(actionId));
			btn.setLayoutData(EclipseUiUtils.fillWidth());
			CmsUtils.markup(btn);
			CmsUtils.style(btn, actionId + ConnectUiStyles.BUTTON_SUFFIX);
			btn.setData(KEY_ACTION_ID, actionId);
			btn.addSelectionListener(asl);
			actionButtons.put(actionId, btn);
		}
	}

	protected void setVisible(boolean visible, String... buttonIds) {
		for (String id : buttonIds) {
			Button button = actionButtons.get(id);
			button.setVisible(visible);
			GridData gd = (GridData) button.getLayoutData();
			gd.heightHint = visible ? SWT.DEFAULT : 0;
		}
	}

	public void show(Control source, Point location, IStructuredSelection selection) {
		if (isVisible())
			setVisible(false);

		if (aboutToShow(source, location, selection)) {
			pack();
			layout();
			if (source instanceof Control)
				setLocation(((Control) source).toDisplay(location.x, location.y));
			open();
		}
	}

	class StyleButton extends Label {
		private static final long serialVersionUID = 7731102609123946115L;

		public StyleButton(Composite parent, int swtStyle) {
			super(parent, swtStyle);
		}
	}

	class ActionsSelListener extends SelectionAdapter {
		private static final long serialVersionUID = -1041871937815812149L;

		@Override
		public void widgetSelected(SelectionEvent e) {
			Object eventSource = e.getSource();
			if (eventSource instanceof Button) {
				Button pressedBtn = (Button) eventSource;
				performAction((String) pressedBtn.getData(KEY_ACTION_ID));
			}
		}
	}

	class ActionsShellListener extends org.eclipse.swt.events.ShellAdapter {
		private static final long serialVersionUID = -5092341449523150827L;

		@Override
		public void shellDeactivated(ShellEvent e) {
			setVisible(false);
		}
	}

	protected abstract boolean performAction(String actionId);

	protected abstract boolean aboutToShow(Control source, Point location, IStructuredSelection selection);

	protected abstract String getLabel(String actionId);
}
