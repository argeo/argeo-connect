package org.argeo.connect.versioning;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.jcr.Item;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.version.Version;
import javax.jcr.version.VersionHistory;
import javax.jcr.version.VersionIterator;
import javax.jcr.version.VersionManager;

import org.argeo.connect.ConnectException;

/** History management */
public class VersionUtils {

	// Filtered properties
	public static final List<String> DEFAULT_FILTERED_OUT_PROP_NAMES = asList("jcr:uuid", "jcr:frozenUuid",
			"jcr:frozenPrimaryType", "jcr:primaryType", "jcr:lastModified", "jcr:lastModifiedBy",
			Property.JCR_LAST_MODIFIED_BY);

	public static List<VersionDiff> listHistoryDiff(Node entity, List<String> excludedProperties) {
		try {
			Session session = entity.getSession();
			List<VersionDiff> res = new ArrayList<VersionDiff>();
			VersionManager versionManager = session.getWorkspace().getVersionManager();

			// if (!entity.hasProperty(Property.JCR_CREATED))
			// // Transient item. No history
			// return res;

			VersionHistory versionHistory = null;
			try {
				versionHistory = versionManager.getVersionHistory(entity.getPath());
			} catch (Exception ise) {
				// TODO clean this:
				// Transient items that have just been created have no version
				// history
				// A jackrabbit specific NoSuchItemStateException is then
				// thrown.
				// We catch it and return an empty array
				return res;
			}

			VersionIterator vit = versionHistory.getAllLinearVersions();
			// boolean first = true;
			while (vit.hasNext()) {
				Version version = vit.nextVersion();
				Node node = version.getFrozenNode();

				Version predecessor = null;
				try {
					predecessor = version.getLinearPredecessor();
				} catch (Exception e) {
					// no predecessor throw an exception even if it shouldn't...
					// e.printStackTrace();
				}
				if (predecessor == null) {// original
				} else {
					Map<String, ItemDiff> diffs = VersionUtils.compareNodes(predecessor.getFrozenNode(), node,
							excludedProperties);
					if (!diffs.isEmpty()) {
						String userid = node.hasProperty(Property.JCR_LAST_MODIFIED_BY)
								? node.getProperty(Property.JCR_LAST_MODIFIED_BY).getString() : null;
						Calendar updateTime = node.hasProperty(Property.JCR_LAST_MODIFIED)
								? node.getProperty(Property.JCR_LAST_MODIFIED).getDate() : null;
						res.add(new VersionDiff(null, userid, updateTime, diffs));
					}
				}
			}
			return res;
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot generate history for node " + entity, e);
		}
	}

	/**
	 * Returns an ordered map of differences, either on node or on their
	 * properties
	 */
	public static Map<String, ItemDiff> compareNodes(Node reference, Node observed, List<String> excludedProperties) {
		// It is important to keep the same order
		Map<String, ItemDiff> diffs = new LinkedHashMap<String, ItemDiff>();
		compareNodes(diffs, null, reference, observed, excludedProperties);
		return diffs;
	}

	/** Recursively compares 2 nodes */
	static void compareNodes(Map<String, ItemDiff> diffs, String relPath, Node reference, Node observed,
			List<String> excludedProperties) {
		Map<String, ItemDiff> localDiffs = new LinkedHashMap<String, ItemDiff>();
		try {
			compareProperties(localDiffs, relPath, reference, observed, excludedProperties);

			// Removed and modified Node
			NodeIterator nit = reference.getNodes();
			while (nit.hasNext()) {
				Node n = nit.nextNode();
				String refRelPath = getRelPath(reference, n);

				String currNodePath = (relPath != null ? relPath + "/" : "") + refRelPath;
				if (observed.hasNode(refRelPath)) {
					Map<String, ItemDiff> modDiffs = new LinkedHashMap<String, ItemDiff>();
					compareNodes(modDiffs, currNodePath, n, observed.getNode(refRelPath), excludedProperties);
					if (!modDiffs.isEmpty()) {
						ItemDiff iDiff = new ItemDiff(ItemDiff.MODIFIED, currNodePath, n, observed.getNode(refRelPath));
						localDiffs.put(currNodePath, iDiff);
						localDiffs.putAll(modDiffs);
					}
				} else {
					ItemDiff iDiff = new ItemDiff(ItemDiff.REMOVED, currNodePath, n, null);
					localDiffs.put(currNodePath, iDiff);
					addAllProperties(localDiffs, ItemDiff.REMOVED, true, n, excludedProperties);
				}
			}
			// Added nodes
			nit = observed.getNodes();
			while (nit.hasNext()) {
				Node n = nit.nextNode();
				String obsRelPath = getRelPath(observed, n);
				String currNodePath = (relPath != null ? relPath + "/" : "") + obsRelPath;
				if (!reference.hasNode(obsRelPath)) {
					ItemDiff iDiff = new ItemDiff(ItemDiff.ADDED, currNodePath, null, n);
					localDiffs.put(currNodePath, iDiff);
					// This triggers the display of duplicated properties when a
					// sub node is added. Violently commented out for the time
					// being.
					// TODO rework this
					// addAllProperties(localDiffs, ItemDiff.ADDED, true, n,
					// excludedProperties);
				}
			}
			// Modification found, we add them
			if (!localDiffs.isEmpty()) {
				// Small hack to avoid putting a line for parent node with only
				// a modification on their children
				// Typically, when we modify a contact, we don't want to have a
				// line for the people:contact parent
				if (localDiffs.size() >= 2) {
					Iterator<ItemDiff> it = localDiffs.values().iterator();
					if (isNodeDiff(it.next()) && isNodeDiff(it.next())) {
						// remove the first
						localDiffs.remove(localDiffs.keySet().iterator().next());
					}
				}
				diffs.putAll(localDiffs);
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot diff " + reference + " and " + observed, e);
		}
	}

	static public boolean isNodeDiff(ItemDiff diff) {
		Item refItem = diff.getReferenceItem();
		Item newItem = diff.getObservedItem();
		Item tmpItem = refItem == null ? newItem : refItem;
		return tmpItem instanceof Node;
	}

	static void addAllProperties(Map<String, ItemDiff> diffs, Integer type, boolean trackSubNode, Node node,
			List<String> excludedProperties) throws RepositoryException {
		PropertyIterator pit = node.getProperties();
		props: while (pit.hasNext()) {
			Property p = pit.nextProperty();
			String name = p.getName();
			if (excludedProperties.contains(name))
				continue props;
			ItemDiff iDiff;
			if (ItemDiff.ADDED == type)
				iDiff = new ItemDiff(ItemDiff.ADDED, name, null, p);
			else
				iDiff = new ItemDiff(ItemDiff.REMOVED, name, p, null);
			diffs.put(name, iDiff);
		}

		// TODO: for the time being, we do not yet add sub nodes and their
		// properties in the diff
		// Investigate: We might have to also deal and update this to correctly
		// managed added rel pathes.

		// NodeIterator nit = node.getNodes();
		// nodes: while (nit.hasNext()) {
		// Node n = nit.nextNode();
		// String name = n.getName();
		// // TODO add nodes to ignore in the property list
		// if (excludedProperties.contains(name))
		// continue nodes;
		// ItemDiff iDiff;
		// if (ItemDiff.ADDED == type){
		// iDiff = new ItemDiff(ItemDiff.ADDED, name, null, n);
		// localDiffs.put(currNodePath, iDiff);
		// addAllProperties(localDiffs, ItemDiff.ADDED, n,
		// excludedProperties);
		//
		// localDiffs, ItemDiff.ADDED, n,
		// excludedProperties
		// }
		// else
		// iDiff = new ItemDiff(ItemDiff.REMOVED, name, p, null);
		// diffs.put(name, iDiff);
		// }
		//

	}

	/**
	 * Compare the properties of two nodes. Extends
	 * <code>JcrUtils.diffPropertiesLevel</code> to also track differences in
	 * multiple value properties and sub graph. No property is skipped (among
	 * other all technical jcr:... properties) to be able to track jcr:title and
	 * description properties, among other. Filtering must be applied afterwards
	 * to only keep relevant properties.
	 */
	static void compareProperties(Map<String, ItemDiff> diffs, String relPath, Node reference, Node observed,
			List<String> excludedProperties) {
		try {
			// Removed and modified properties
			PropertyIterator pit = reference.getProperties();
			props: while (pit.hasNext()) {
				Property p = pit.nextProperty();
				String name = p.getName();
				String relName = propertyRelPath(relPath, name);
				if (excludedProperties.contains(name))
					continue props;
				if (!observed.hasProperty(name)) {
					ItemDiff iDiff = new ItemDiff(ItemDiff.REMOVED, name, p, null);
					diffs.put(relName, iDiff);
				} else {
					if (p.isMultiple()) {

						Value[] refValues = p.getValues();
						Value[] newValues = observed.getProperty(name).getValues();
						refValues: for (Value refValue : refValues) {
							for (Value newValue : newValues) {
								if (refValue.equals(newValue))
									continue refValues;
							}
							// At least one value has been removed -> modified
							// prop
							ItemDiff iDiff = new ItemDiff(ItemDiff.MODIFIED, name, p, observed.getProperty(name));
							diffs.put(relName, iDiff);
							continue props;
						}

						newValues: for (Value newValue : newValues) {
							for (Value refValue : refValues) {
								if (refValue.equals(newValue))
									continue newValues;
							}
							// At least one value has been added -> modified
							// prop
							ItemDiff iDiff = new ItemDiff(ItemDiff.MODIFIED, name, p, observed.getProperty(name));
							diffs.put(relName, iDiff);
							continue props;
						}
					} else {
						Value referenceValue = p.getValue();
						Value newValue = observed.getProperty(name).getValue();
						if (!referenceValue.equals(newValue)) {
							ItemDiff iDiff = new ItemDiff(ItemDiff.MODIFIED, name, p, observed.getProperty(name));
							diffs.put(relName, iDiff);
						}
					}
				}
			}
			// Added properties
			pit = observed.getProperties();
			props: while (pit.hasNext()) {
				Property p = pit.nextProperty();
				String name = p.getName();
				String relName = propertyRelPath(relPath, name);
				if (excludedProperties.contains(name))
					continue props;
				if (!reference.hasProperty(name)) {
					ItemDiff pDiff = new ItemDiff(ItemDiff.ADDED, name, null, p);
					diffs.put(relName, pDiff);
				}
			}
		} catch (RepositoryException e) {
			throw new ConnectException("Cannot diff " + reference + " and " + observed, e);
		}
	}

	private static String getRelPath(Node parent, Node descendant) throws RepositoryException {
		String pPath = parent.getPath();
		String dPath = descendant.getPath();
		if (!dPath.startsWith(pPath))
			throw new ConnectException(
					"Cannot get rel path for " + descendant + ". It is not a descendant of " + parent);
		String relPath = dPath.substring(pPath.length());
		if (relPath.startsWith("/"))
			relPath = relPath.substring(1);
		return relPath;
	}

	/** Builds a property relPath to be used in the diff map. */
	private static String propertyRelPath(String baseRelPath, String propertyName) {
		if (baseRelPath == null)
			return propertyName;
		else
			return baseRelPath + '/' + propertyName;
	}
}