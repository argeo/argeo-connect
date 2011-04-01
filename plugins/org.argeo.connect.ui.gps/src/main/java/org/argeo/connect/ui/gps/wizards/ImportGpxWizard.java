package org.argeo.connect.ui.gps.wizards;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.gpx.TrackDao;
import org.argeo.connect.ui.gps.GpsTypes;
import org.argeo.jcr.JcrUtils;
import org.eclipse.jface.wizard.Wizard;

public class ImportGpxWizard extends Wizard implements GpsTypes {
	private Node baseNode;
	private TrackDao trackDao;

	private DefineModelWizardPage defineModelPage;

	public ImportGpxWizard(TrackDao trackDao, Node baseNode) {
		super();
		this.trackDao = trackDao;
		this.baseNode = baseNode;
	}

	@Override
	public void addPages() {
		try {
			defineModelPage = new DefineModelWizardPage("defineModelPage");
			addPage(defineModelPage);
		} catch (Exception e) {
			throw new ArgeoException("Cannot add page to wizard ", e);
		}
	}

	@Override
	public boolean performFinish() {
		if (!canFinish())
			return false;

		Binary binary = null;
		try {
			for (NodeIterator children = baseNode.getNodes(); children
					.hasNext();) {
				Node child = children.nextNode();
				if (child.isNodeType(NodeType.NT_FILE)
						&& child.getName().endsWith(".gpx")) {
					binary = child.getNode(Property.JCR_CONTENT)
							.getProperty(Property.JCR_DATA).getBinary();
					trackDao.importTrackPoints(child.getPath(),
							defineModelPage.getSensorName(), binary.getStream());
					JcrUtils.closeQuietly(binary);
				}
			}
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot import GPS from " + baseNode, e);
		} finally {
			JcrUtils.closeQuietly(binary);
		}
		return false;
	}

}
