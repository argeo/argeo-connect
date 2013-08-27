package org.argeo.connect.people.utils;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleTypes;

/**
 * static utils methods to manage film concepts. See what can be factorized
 */
public class FilmJcrUtils implements PeopleNames {

	protected static Node getOrCreateDirNode(Node parent, String dirName)
			throws RepositoryException {
		Node dirNode;
		if (parent.hasNode(dirName))
			dirNode = parent.getNode(dirName);
		else
			dirNode = parent.addNode(dirName, NodeType.NT_UNSTRUCTURED);
		return dirNode;
	}

	// it's a hack that depends on the data we had for the PoC. implement
	// cleanly
	public static String getTitleForFilm(Node film, boolean isPrimary) {
		// try {
		// if (film.hasNode(MSM_TITLES)) {
		// NodeIterator ni = film.getNode(MSM_TITLES).getNodes();
		//
		// while (ni.hasNext()) {
		// Node currNode = ni.nextNode();
		// if (currNode.hasProperty(MSM_IS_PRIMARY)
		// && currNode.getProperty(MSM_IS_PRIMARY)
		// .getBoolean() == isPrimary) {
		// String title = "";
		// title = currNode.getProperty(MSM_FILM_TITLE_VALUE)
		// .getString();
		//
		// if (currNode.hasProperty(MSM_FILM_TITLE_ARTICLE))
		// title += ", "
		// + currNode.getProperty(
		// MSM_FILM_TITLE_ARTICLE).getString();
		// return title;
		// }
		//
		// }
		// }
		// return null;
		// } catch (RepositoryException re) {
		// throw new ArgeoException("Unable to get title for film", re);
		// }
		try {
			return film.getName();
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get title for film", re);
		}
	}

	public static Node getTitleNode(Node film, boolean isPrimary) {
		try {
			if (film.hasNode(PEOPLE_TITLES)) {
				NodeIterator ni = film.getNode(PEOPLE_TITLES).getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.hasProperty(PEOPLE_IS_PRIMARY)
							&& currNode.getProperty(PEOPLE_IS_PRIMARY)
									.getBoolean() == isPrimary) {
						return currNode;
					}

				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get title node for film", re);
		}
	}

	/** Create a title node and add basic info */
	public static Node createTitle(Node film, String name, String title,
			String article, boolean isPrimary, String language) {
		try {
			Node titles = getOrCreateDirNode(film, PEOPLE_TITLES);
			Node tNode = titles.addNode(name, PeopleTypes.PEOPLE_FILM_TITLE);
			tNode.setProperty(PEOPLE_FILM_TITLE_VALUE, title);
			if (!CommonsJcrUtils.isEmptyString(article))
				tNode.setProperty(PEOPLE_FILM_TITLE_ARTICLE, article);
			tNode.setProperty(PEOPLE_IS_PRIMARY, isPrimary);
			if (!CommonsJcrUtils.isEmptyString(language))
				tNode.setProperty(PEOPLE_LANGUAGE, language);
			return tNode;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to add a new synopsis node", re);
		}
	}
}