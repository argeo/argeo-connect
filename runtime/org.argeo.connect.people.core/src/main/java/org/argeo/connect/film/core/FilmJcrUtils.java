package org.argeo.connect.film.core;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.nodetype.NodeType;

import org.argeo.ArgeoException;
import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.utils.CommonsJcrUtils;

/**
 * static utils methods to manage film concepts. See what can be factorized
 */
public class FilmJcrUtils implements FilmNames {

	protected static Node getOrCreateDirNode(Node parent, String dirName)
			throws RepositoryException {
		Node dirNode;
		if (parent.hasNode(dirName))
			dirNode = parent.getNode(dirName);
		else
			dirNode = parent.addNode(dirName, NodeType.NT_UNSTRUCTURED);
		return dirNode;
	}

	/** Return a display string for the original title of the given film */
	public static String getTitleForFilm(Node film) {
		try {
			String title = "";
			title = film.getProperty(FILM_ORIGINAL_TITLE).getString();
			if (film.hasProperty(FILM_ORIG_TITLE_ARTICLE))
				title += ", "
						+ film.getProperty(FILM_ORIG_TITLE_ARTICLE).getString();
			return title;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get title for film", re);
		}
	}

	public static Node getAltTitleNode(Node film, String lang) {
		try {
			if (film.hasNode(FILM_ALT_TITLES) && lang != null) {
				NodeIterator ni = film.getNode(FILM_ALT_TITLES).getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.hasProperty(FilmNames.FILM_LANG)
							&& lang.equals(currNode.getProperty(
									FilmNames.FILM_LANG).getString()))
						return currNode;
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get title node for film", re);
		}
	}

	/**
	 * Add an alternative title for a given language
	 * 
	 * @param language
	 *            a String corresponding to this language as defined in
	 *            http://tools.ietf.org/html/rfc5646. Must not be null
	 * */
	public static Node addAltTitle(Node film, String title, String article,
			String language) {
		if (language == null || language.trim().equals(""))
			throw new PeopleException(
					"Language must not be null or an empty String");
		try {
			Node titles = getOrCreateDirNode(film, FILM_ALT_TITLES);
			// TODO Check for duplicates
			Node tNode = titles.addNode(language, FilmTypes.FILM_TITLE);
			tNode.setProperty(FILM_TITLE, title);
			if (!CommonsJcrUtils.isEmptyString(article))
				tNode.setProperty(FILM_TITLE_ARTICLE, article);
			tNode.setProperty(FILM_LANG, language);
			return tNode;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new Title node", re);
		}
	}
}