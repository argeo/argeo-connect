package org.argeo.connect.ui.gps.editors;

import javax.jcr.Node;
import javax.jcr.Property;
import javax.jcr.RepositoryException;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ui.gps.ConnectUiGpsPlugin;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;

public abstract class AbstractCleanDataEditorPage extends FormPage {
	// implements ConnectNames, ConnectTypes, ConnectGpsLabels {

	// Images
	protected final static Image CHECKED = ConnectUiGpsPlugin
			.getImageDescriptor("icons/checked.gif").createImage();
	protected final static Image UNCHECKED = ConnectUiGpsPlugin
			.getImageDescriptor("icons/unchecked.gif").createImage();

	public AbstractCleanDataEditorPage(FormEditor editor, String id,
			String title) {
		super(editor, id, title);
		// TODO Auto-generated constructor stub
	}

	public CleanDataEditor getEditor() {
		return (CleanDataEditor) super.getEditor();
	}

	/** Factorize the creation of table columns */
	protected TableViewerColumn createTableViewerColumn(TableViewer viewer,
			String title, int bound) {
		final TableViewerColumn viewerColumn = new TableViewerColumn(viewer,
				SWT.NONE);
		final TableColumn column = viewerColumn.getColumn();
		column.setText(title);
		column.setWidth(bound);
		column.setResizable(true);
		column.setMoveable(true);
		return viewerColumn;
	}

	/**
	 * Check if the current session is already completed and thus not editable
	 * anymore
	 */
	protected boolean isSessionAlreadyComplete() {
		try {
			// Cannot edit a completed session
			return getEditor().getCurrentSessionNode()
					.getProperty(ConnectNames.CONNECT_IS_SESSION_COMPLETE)
					.getBoolean();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Cannot access node to see if it has already been imported.");
		}
	}

	/**
	 * Returns the cleanSession name: it is the corresponding jcr node name
	 * 
	 */
	protected String getCleanSession() {

		String csName;
		try {
			Node sessionNode = getEditor().getCurrentSessionNode();
			csName = sessionNode.getName();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while retrieving node session name.", re);
		}
		return csName;
	}

	/**
	 * Returns the localRepositoryName: it is the corresponding jcr node name
	 * 
	 */
	protected String getReferential() {
		try {
			Node sessionNode = getEditor().getCurrentSessionNode();
			if (!sessionNode.hasProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
					|| "".equals(sessionNode
							.getProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
							.getString().trim())) {
				// ErrorFeedback.show("No local repository has been defined yet.");
				return null;
			} else
				return sessionNode.getProperty(
						ConnectNames.CONNECT_LOCAL_REPO_NAME).getString();
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while retrieving node session name.", re);
		}
	}

	protected String getReferentialDisplayName() {
		try {
			Node sessionNode = getEditor().getCurrentSessionNode();
			if (!sessionNode.hasProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
					|| "".equals(sessionNode
							.getProperty(ConnectNames.CONNECT_LOCAL_REPO_NAME)
							.getString().trim())) {
				return null;
			} else {
				Node tmp = getEditor().getTrackDao()
						.getLocalRepositoriesParentNode(
								getEditor().getJcrSession());
				return tmp
						.getNode(
								sessionNode.getProperty(
										ConnectNames.CONNECT_LOCAL_REPO_NAME)
										.getString())
						.getProperty(Property.JCR_TITLE).getString();
			}
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexpected error while retrieving node session name.", re);
		}
	}

	@Override
	public void setActive(boolean active) {
		super.setActive(active);
		((CleanDataEditor) getEditor()).refreshReadOnlyState();
	}

}
