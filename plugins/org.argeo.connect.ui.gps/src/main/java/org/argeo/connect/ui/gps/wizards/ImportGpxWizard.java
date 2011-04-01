package org.argeo.connect.ui.gps.wizards;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.gpx.TrackDao;
import org.argeo.eclipse.ui.dialogs.Error;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.Wizard;

public class ImportGpxWizard extends Wizard {
	private Node baseNode;
	private TrackDao trackDao;

	private Map<String, Node> nodesToImport = new TreeMap<String, Node>();

	private DefineModelWizardPage defineModelPage;

	public ImportGpxWizard(TrackDao trackDao, Node baseNode) {
		super();
		this.trackDao = trackDao;
		this.baseNode = baseNode;
	}

	@Override
	public void addPages() {
		try {
			defineModelPage = new DefineModelWizardPage(baseNode);
			addPage(defineModelPage);
		} catch (Exception e) {
			throw new ArgeoException("Cannot add page to wizard ", e);
		}
	}

	@Override
	public boolean performFinish() {
		if (!canFinish())
			return false;

		Boolean failed = false;
		List<String> nodesPath = defineModelPage.getNodesToImportPath();
		final int nodeSize = nodesPath.size();
		final Map<String, Node> nodesToImport = constructNodeToImport(nodesPath);

		final Stats stats = new Stats();
		Long begin = System.currentTimeMillis();
		try {

			ProgressMonitorDialog dialog = new ProgressMonitorDialog(
					getContainer().getShell());
			dialog.run(true, true, new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) {
					monitor.beginTask("Importing GPX nodes", nodeSize);
					for (String key : nodesToImport.keySet()) {
						importOneNode(nodesToImport.get(key), monitor, stats);
					}
					monitor.done();
				}
			});

			// getContainer().run(true, true, new IRunnableWithProgress() {
			// public void run(IProgressMonitor monitor) {
			// try {
			// monitor.beginTask("Importing GPX nodes", nodeSize);
			// for (String key : nodesToImport.keySet()) {
			// importOneNode(nodesToImport.get(key), monitor,
			// stats);
			// }
			// // TODO REAL IMPORT
			// monitor.done();
			// } catch (Exception e) {
			// if (e instanceof RuntimeException)
			// throw (RuntimeException) e;
			// else
			// throw new ArgeoException("Cannot import GPX nodes",
			// e);
			// }
			// }
			// });
		} catch (Exception e) {
			Error.show("Cannot import GPX nodes", e);
			failed = true;
		}

		Long duration = System.currentTimeMillis() - begin;
		Long durationS = duration / 1000l;
		String durationStr = (durationS / 60) + " min " + (durationS % 60)
				+ " s";
		StringBuffer message = new StringBuffer("Imported\n");
		message.append(stats.nodeCount).append(" nodes\n");
		if (failed)
			message.append(" of planned ").append(nodeSize);
		message.append("\n");
		message.append("in ").append(durationStr).append("\n");
		if (failed)
			MessageDialog.openError(getShell(), "Import failed",
					message.toString());
		else
			MessageDialog.openInformation(getShell(), "Import successful",
					message.toString());
		return true;
	}

	// Binary binary = null;
	// try {
	// for (NodeIterator children = baseNode.getNodes(); children
	// .hasNext();) {
	// Node child = children.nextNode();
	// if (child.isNodeType(NodeType.NT_FILE)
	// && child.getName().endsWith(".gpx")) {
	// binary = child.getNode(Property.JCR_CONTENT)
	// .getProperty(Property.JCR_DATA).getBinary();
	// trackDao.importTrackPoints(child.getPath(),
	// defineModelPage.getSensorName(), binary.getStream());
	// JcrUtils.closeQuietly(binary);
	// }
	// }
	// } catch (RepositoryException e) {
	// throw new ArgeoException("Cannot import GPS from " + baseNode, e);
	// } finally {
	// JcrUtils.closeQuietly(binary);
	// }
	// return false;

	private Map<String, Node> constructNodeToImport(List<String> nodePathes) {
		Map<String, Node> nodes = new TreeMap<String, Node>();
		try {
			for (NodeIterator ni = baseNode.getNodes(); ni.hasNext();) {
				Node node = ni.nextNode();
				String curPath = node.getPath();
				if (nodePathes.contains(curPath)
						&& node.isNodeType(NodeType.NT_FILE)
						&& node.getName().endsWith(".gpx")) {
					nodes.put(curPath, node);
				}

			}
			return nodes;
		} catch (RepositoryException re) {
			throw new ArgeoException("Cannot construct node list", re);
		}
	}

	private boolean importOneNode(Node node, IProgressMonitor monitor,
			Stats stats) {
		Binary binary = null;
		try {

			String name = node.getName();
			monitor.subTask("Importing " + name);

			binary = node.getNode(Property.JCR_CONTENT)
					.getProperty(Property.JCR_DATA).getBinary();
			trackDao.importTrackPoints(node.getPath(),
					defineModelPage.getSensorName(), binary.getStream());
			JcrUtils.closeQuietly(binary);
			stats.nodeCount++;
			monitor.worked(1);
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot import GPS from node", e);
		} finally {
			JcrUtils.closeQuietly(binary);
		}
		return true;
	}

	static class Stats {
		public Long nodeCount = 0l;
	}
}
