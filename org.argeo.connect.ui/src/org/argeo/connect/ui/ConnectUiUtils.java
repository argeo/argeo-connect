package org.argeo.connect.ui;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.connect.AppService;
import org.argeo.connect.ConnectException;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.workbench.AppWorkbenchService;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/** Helper methods for various Connect UIs */
public class ConnectUiUtils {
	// private final static Log log = LogFactory.getLog(ConnectUiUtils.class);

	private static String AMPERSAND = "&#38;";

	/**
	 * Cleans a String by replacing any '&' by its HTML encoding '&#38;' to
	 * avoid <code>SAXParseException</code> while rendering HTML with RWT
	 */
	public static String replaceAmpersand(String value) {
		value = value.replaceAll("&(?![#a-zA-Z0-9]+;)", AMPERSAND);
		return value;
	}

	/** simply add an empty line in a grid data to give some air */
	public static Label addEmptyLine(Composite parent, int height, int colSpan) {
		// Empty line that act as a padding
		Label emptyLbl = new Label(parent, SWT.NONE);
		emptyLbl.setText("");
		GridData gd = EclipseUiUtils.fillWidth(colSpan);
		gd.heightHint = height;
		emptyLbl.setLayoutData(gd);
		return emptyLbl;
	}

	/**
	 * Calls <code>ConnectJcrUtils.get(Node node, String propName)</code> method
	 * and replace any '&' by its html encoding '&amp;' to avoid
	 * <code>IllegalArgumentException</code> while rendering html read only
	 * snippets
	 */
	public static String getRwtCompliantString(Node node, String propName) {
		String value = ConnectJcrUtils.get(node, propName);
		value = replaceAmpersand(value);
		return value;
	}

	/**
	 * Dispose all control children of this composite. Useful for violent
	 * refreshes.
	 * 
	 * @param parent
	 */
	// public static void disposeAllChildren(Composite parent) {
	// // We redraw the full control at each refresh, might be a more
	// // efficient way to do
	// Control[] oldChildren = parent.getChildren();
	// for (Control child : oldChildren)
	// child.dispose();
	// }

	public static boolean isNumbers(String content) {
		int length = content.length();
		for (int i = 0; i < length; i++) {
			char ch = content.charAt(i);
			if (!Character.isDigit(ch)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Shortcut to create a {@link GridLayout} with the given column number with
	 * no margin and no spacing (default are normally 5 px).
	 * makeColumnsEqualWidth parameter is set to false.
	 */
	public static GridLayout noSpaceGridLayout(int nbOfCol) {
		GridLayout gl = new GridLayout(nbOfCol, false);
		gl.marginWidth = gl.marginHeight = gl.horizontalSpacing = gl.verticalSpacing = 0;
		return gl;
	}

	/**
	 * Shortcut to refresh the value of a <code>Text</code> given a Node and a
	 * property Name
	 */
	public static String refreshTextWidgetValue(Text text, Node entity, String propName) {
		String tmpStr = ConnectJcrUtils.get(entity, propName);
		if (EclipseUiUtils.notEmpty(tmpStr))
			text.setText(tmpStr);
		return tmpStr;
	}

	/**
	 * Cleans a String by replacing any '&' by its HTML encoding '&nbsp;' to
	 * insure they are displayed in SWT.Link controls
	 */
	public static String replaceAmpersandforSWTLink(String value) {
		value = value.replaceAll("&", "&&");
		return value;
	}

	public static String createAndConfigureEntity(Shell shell, Session referenceSession, AppService appService,
			AppWorkbenchService appWorkbenchService, String mainMixin, String... additionnalProps) {

		Session tmpSession = null;
		Session mainSession = null;
		try {
			tmpSession = referenceSession.getRepository().login();
			Node draftNode = appService.createDraftEntity(tmpSession, mainMixin);
			for (int i = 0; i < additionnalProps.length - 1; i += 2) {
				draftNode.setProperty(additionnalProps[i], additionnalProps[i + 1]);
			}
			Wizard wizard = appWorkbenchService.getCreationWizard(draftNode);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			if (dialog.open() == Window.OK) {
				String parentPath = "/" + appService.getBaseRelPath(mainMixin);
				mainSession = referenceSession.getRepository().login();
				Node parent = mainSession.getNode(parentPath);
				Node task = appService.publishEntity(parent, mainMixin, draftNode);
				task = appService.saveEntity(task, false);
				referenceSession.refresh(true);
				return task.getPath();
			}
			return null;
		} catch (RepositoryException e1) {
			throw new ConnectException(
					"Unable to create " + mainMixin + " entity with session " + referenceSession.toString(), e1);
		} finally {
			JcrUtils.logoutQuietly(tmpSession);
			JcrUtils.logoutQuietly(mainSession);
		}
	}
}
