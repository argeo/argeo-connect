package org.argeo.connect.film.core;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;
import javax.jcr.query.QueryResult;
import javax.jcr.query.qom.Constraint;
import javax.jcr.query.qom.DynamicOperand;
import javax.jcr.query.qom.QueryObjectModel;
import javax.jcr.query.qom.QueryObjectModelFactory;
import javax.jcr.query.qom.Selector;
import javax.jcr.query.qom.StaticOperand;

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

	/** Return a display string for the original title of the given film */
	public static void setOriginalTitle(Node film, String origTitle,
			String origTitleArticle, String origLatinTitle) {
		try {
			film.setProperty(FILM_ORIGINAL_TITLE, origTitle);
			if (CommonsJcrUtils.checkNotEmptyString(origTitleArticle))
				film.setProperty(FILM_ORIG_TITLE_ARTICLE, origTitleArticle);
			if (CommonsJcrUtils.checkNotEmptyString(origLatinTitle))
				film.setProperty(FILM_ORIG_LATIN_TITLE, origLatinTitle);
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unable to set original title on film node", re);
		}
	}

	/**
	 * Return a display string for alternative title corresponding to the given
	 * language or null the title is not defined for this language
	 */
	public static String getAltTitle(Node film, String lang) {
		try {
			String title = null;
			Node altTitle = getAltTitleNode(film, lang);
			if (altTitle != null) {
				title = film.getProperty(FILM_TITLE).getString();
				if (film.hasProperty(FILM_TITLE_ARTICLE))
					title += ", "
							+ film.getProperty(FILM_ORIG_TITLE_ARTICLE)
									.getString();
			}
			return title;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get alt " + lang
					+ " title for film", re);
		}
	}

	/**
	 * 
	 * Return a display string for alternative title corresponding to the given
	 * language or null the title is not defined for this language
	 */
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

	public static Node getFilmWithId(Session session, String filmId)
			throws RepositoryException {
		QueryObjectModelFactory factory = session.getWorkspace()
				.getQueryManager().getQOMFactory();
		final String typeSelector = "film";
		Selector source = factory.selector(FilmTypes.FILM, typeSelector);

		DynamicOperand legalNameDO = factory.propertyValue(
				source.getSelectorName(), FilmNames.FILM_ID);

		// TODO NOT RELIABLE
		String sFilmId = filmId.replaceAll("[^a-zA-Z0-9-]", "");

		StaticOperand so = factory.literal(session.getValueFactory()
				.createValue(sFilmId));
		Constraint defaultC = factory.comparison(legalNameDO,
				QueryObjectModelFactory.JCR_OPERATOR_EQUAL_TO, so);

		QueryObjectModel query = factory.createQuery(source, defaultC, null,
				null);

		QueryResult result = query.execute();
		NodeIterator ni = result.getNodes();
		// TODO clean this to handle multiple result
		if (ni.hasNext())
			return ni.nextNode();
		return null;
	}
}