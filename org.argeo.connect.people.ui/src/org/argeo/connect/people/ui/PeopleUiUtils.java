package org.argeo.connect.people.ui;

import static org.argeo.eclipse.ui.EclipseUiUtils.isEmpty;

import java.util.ArrayList;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.PropertyIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.ConnectConstants;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.util.ConnectJcrUtils;
import org.argeo.connect.util.XPathUtils;
import org.argeo.eclipse.ui.EclipseUiUtils;
import org.argeo.jcr.JcrUtils;

public class PeopleUiUtils {
	private final static Log log = LogFactory.getLog(PeopleUiUtils.class);

	/* INTERNATIONALISATION HELPERS */

	/**
	 * Concisely gets the node for the translation of some main properties of
	 * the current node given a language by default such a name is at
	 * {@link PeopleNames#PEOPLE_ALT_LANGS}/lang
	 */
	public static Node getAltLangNode(Node currentNode, String lang) {
		return getAltLangNode(currentNode, PeopleNames.PEOPLE_ALT_LANGS, lang);
	}

	/**
	 * Concisely gets the node for the translation of some main properties of
	 * the current node given a language and the name of an intermediary node
	 * 
	 * If no rel path is given, we use the default
	 * {@link PeopleNames#PEOPLE_ALT_LANGS}
	 */
	public static Node getAltLangNode(Node currentNode, String relPath, String lang) {
		try {
			if (EclipseUiUtils.isEmpty(relPath))
				relPath = PeopleNames.PEOPLE_ALT_LANGS;
			if (!currentNode.hasNode(relPath + "/" + lang))
				return null;
			else {
				return currentNode.getNode(relPath + "/" + lang);
			}
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot get alt property for " + lang, e);
		}
	}

	public static Node getOrCreateAltLanguageNode(Node node, String lang, List<String> mixins) {
		return getOrCreateAltLanguageNode(node, PeopleNames.PEOPLE_ALT_LANGS, lang, mixins);
	}

	public static Node getOrCreateAltLanguageNode(Node node, String relPath, String lang, List<String> mixins) {
		try {
			Node child = JcrUtils.mkdirs(node, relPath + "/" + lang, NodeType.NT_UNSTRUCTURED);
			child.addMixin(NodeType.MIX_LANGUAGE);
			if (mixins != null && !mixins.isEmpty())
				for (String mixin : mixins)
					child.addMixin(mixin);

			child.setProperty(Property.JCR_LANGUAGE, lang);
			return child;
		} catch (RepositoryException e) {
			throw new PeopleException("Cannot create child for language " + lang, e);
		}
	}

	/* IMPORT HELPERS */
	/**
	 * Transforms String property that use the people UID to reference other
	 * entities during import to JCR References. Manage both single and multi
	 * value prop It retrieves and process all properties that have a _puid
	 * suffix
	 */
	public static void translatePuidToRef(Node node, String nodeType, String basePath, boolean updateChildren) {
		try {
			Session session = node.getSession();
			PropertyIterator pit = node.getProperties();
			while (pit.hasNext()) {
				Property currProp = pit.nextProperty();
				String currName = currProp.getName();
				if (currName.endsWith(PeopleConstants.IMPORT_REF_SUFFIX)) {
					String newName = currName.substring(0,
							currName.length() - PeopleConstants.IMPORT_REF_SUFFIX.length());
					if (currProp.isMultiple()) {
						Value[] values = currProp.getValues();
						List<Node> nodes = new ArrayList<Node>();
						for (Value val : values) {
							String currId = val.getString();
							Node referenced = getEntityByUid(session, currId, nodeType, basePath);
							if (referenced == null)
								log.warn("Unable to find referenced node with ID " + currId + " for " + currProp);
							else
								nodes.add(referenced);
						}
						ConnectJcrUtils.setMultipleReferences(node, newName, nodes);
						currProp.remove();
					} else {
						String currId = currProp.getString();
						Node referenced = getEntityByUid(session, currId, nodeType, basePath);
						node.setProperty(newName, referenced);
						currProp.remove();
					}
				}
			}
			if (updateChildren) {
				NodeIterator nit = node.getNodes();
				while (nit.hasNext()) {
					Node currChild = nit.nextNode();
					translatePuidToRef(currChild, nodeType, basePath, updateChildren);
				}
			}
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to perform post import " + "translation on Node " + node, re);
		}
	}

	public static Node getEntityByUid(Session session, String uid, String nodeType, String basePath) {
		if (isEmpty(uid))
			throw new PeopleException("Cannot get entity by id by providing an empty people:uid");
		try {
			QueryManager queryManager = session.getWorkspace().getQueryManager();
			String xpathQueryStr = XPathUtils.descendantFrom(basePath) + "//element(*, " + nodeType + ")";
			String attrQuery = XPathUtils.getPropertyEquals(PeopleNames.PEOPLE_UID, uid);
			if (EclipseUiUtils.notEmpty(attrQuery))
				xpathQueryStr += "[" + attrQuery + "]";
			Query xpathQuery = queryManager.createQuery(xpathQueryStr, ConnectConstants.QUERY_XPATH);
			QueryResult result = xpathQuery.execute();
			NodeIterator ni = result.getNodes();

			if (ni.getSize() == 0)
				return null;
			else if (ni.getSize() > 1) {
				Node first = ni.nextNode();
				throw new PeopleException("Found " + ni.getSize() + " entities for People UID [" + uid
						+ "]\n Info on first occurence: " + "\n Path: " + first.getPath() + "\n Node type: "
						+ first.getPrimaryNodeType().getName());
			} else
				return ni.nextNode();
		} catch (RepositoryException e) {
			throw new PeopleException("Unable to retrieve entity of uid: " + uid + " under " + basePath, e);
		}
	}
}
