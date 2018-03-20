package org.argeo.connect.ui.util;

import javax.jcr.Node;

import org.argeo.cms.util.CmsUtils;
import org.argeo.cms.widgets.auth.CmsLoginShell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class ConnectLoginShell extends CmsLoginShell {
	private final Node context;

	public ConnectLoginShell(Control source, Node context) {
		super(CmsUtils.getCmsView());
		this.context = context;
		createUi();
		open();
	}

	@Override
	protected Shell createShell() {
		Shell backgroundShell = new Shell(getDisplay(), SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		backgroundShell.setMaximized(true);
		backgroundShell.setAlpha(128);
		backgroundShell.setBackground(getDisplay().getSystemColor(SWT.COLOR_BLACK));
		Shell shell = new Shell(backgroundShell, SWT.NO_TRIM | SWT.BORDER | SWT.ON_TOP);
		shell.setLayout(new GridLayout());
		shell.setSize(getInitialSize());

		Rectangle shellBounds = Display.getCurrent().getBounds();// RAP
		Point dialogSize = shell.getSize();
		int x = shellBounds.x + (shellBounds.width - dialogSize.x) / 2;
		int y = shellBounds.y + (shellBounds.height - dialogSize.y) / 2;
		shell.setLocation(x, y);

		shell.addShellListener(new ShellAdapter() {
			private static final long serialVersionUID = -2701270481953688763L;

			@Override
			public void shellDeactivated(ShellEvent e) {
				closeShell();
			}
		});
		return shell;
	}

	protected Point getInitialSize() {
		return new Point(400, 400);
	}

	protected Shell getBackgroundShell() {
		if (getShell().getParent() instanceof Shell)
			return (Shell) getShell().getParent();
		else
			return null;
	}

	@Override
	public void open() {
		Shell backgroundShell = getBackgroundShell();
		if (backgroundShell != null && !backgroundShell.isDisposed()) {
			backgroundShell.open();
		}
		super.open();
	}

	protected void closeShell() {
		Shell backgroundShell = getBackgroundShell();
		if (backgroundShell != null && !backgroundShell.isDisposed()) {
			backgroundShell.close();
			backgroundShell.dispose();
		}

		super.closeShell();
	}

	private Display getDisplay() {
		try {
			Display display = Display.getCurrent();
			if (display != null)
				return display;
			else
				return Display.getDefault();
		} catch (Exception e) {
			return Display.getCurrent();
		}
	}


	protected Node getContext() {
		return context;
	}


}
