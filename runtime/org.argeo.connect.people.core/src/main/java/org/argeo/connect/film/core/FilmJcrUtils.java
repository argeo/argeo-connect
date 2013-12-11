package org.argeo.connect.film.core;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
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
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.argeo.jcr.JcrUtils;

/**
 * static utils methods to manage film concepts. See what can be factorized
 */
public class FilmJcrUtils implements FilmNames {

	public static boolean markAsOriginalTitle(Node currNode, boolean value) {
		// TODO implement this
		return true;
	}

	public static boolean markAsPrimaryTitle(Node currNode, boolean value) {
		// TODO implement this
		return true;
	}

	/** Return a display string for the original title of the given film */
	public static String getTitleForFilm(Node film) {
		try {
			String title = "";
			if (film.hasProperty(FILM_ORIGINAL_TITLE))
				title = film.getProperty(FILM_ORIGINAL_TITLE).getString();
			else if (film.hasProperty(Property.JCR_TITLE))
				title = film.getProperty(Property.JCR_TITLE).getString();
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
				title = altTitle.getProperty(FILM_TITLE).getString();
				if (altTitle.hasProperty(FILM_TITLE_ARTICLE))
					title += ", "
							+ altTitle.getProperty(FILM_TITLE_ARTICLE)
									.getString();
			}
			return title;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get alt " + lang
					+ " title for film", re);
		}
	}

	/**
	 * Return a title node, given a film and a language or null if the title is
	 * not defined for this language
	 */
	public static Node getAltTitleNode(Node film, String lang) {
		try {
			if (film.hasNode(FILM_TITLES) && lang != null) {
				NodeIterator ni = film.getNode(FILM_TITLES).getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.hasProperty(PeopleNames.PEOPLE_LANG)
							&& lang.equals(currNode.getProperty(
									PeopleNames.PEOPLE_LANG).getString()))
						return currNode;
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get title node for film", re);
		}
	}

	/**
	 * Shortcut to add a title for a given language using default values: no
	 * latin pronunciation, not primary neither original
	 * 
	 * @param language
	 *            a String corresponding to this language as defined in
	 *            http://tools.ietf.org/html/rfc5646. Must not be null
	 * */
	public static Node addTitle(Node film, String title, String article,
			String language) {
		return addTitle(film, title, article, null, language, false, false);
	}

	/**
	 * Add an alternative title for a given language
	 * 
	 * @param language
	 *            a String corresponding to this language as defined in
	 *            http://tools.ietf.org/html/rfc5646. Must not be null
	 * */
	public static Node addTitle(Node film, String title, String article,
			String latinPronunciation, String language, boolean isOriginal,
			boolean isPrimary) {
		if (language == null || language.trim().equals(""))
			throw new PeopleException(
					"Language must not be null or an empty String");
		try {
			Node titles = CommonsJcrUtils.getOrCreateDirNode(film, FILM_TITLES);
			// TODO Check for duplicates
			Node tNode = titles.addNode(language, FilmTypes.FILM_TITLE);
			tNode.setProperty(FILM_TITLE, title);
			if (CommonsJcrUtils.checkNotEmptyString(article))
				tNode.setProperty(FILM_TITLE_ARTICLE, article);
			if (CommonsJcrUtils.checkNotEmptyString(latinPronunciation))
				tNode.setProperty(FILM_TITLE_LATIN_PRONUNCIATION,
						latinPronunciation);
			tNode.setProperty(PeopleNames.PEOPLE_LANG, language);
			tNode.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, isPrimary);
			tNode.setProperty(FILM_TITLE_IS_ORIG, isOriginal);

			return tNode;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new Title node", re);
		}
	}

	/**
	 * Return the alternative synopsis node, given a film and a language or null
	 * if the synopsis is not defined for this language
	 */
	public static Node getSynopsisNode(Node film, String lang) {
		try {
			if (film.hasNode(FILM_SYNOPSES) && lang != null) {
				NodeIterator ni = film.getNode(FILM_SYNOPSES).getNodes();
				while (ni.hasNext()) {
					Node currNode = ni.nextNode();
					if (currNode.hasProperty(PeopleNames.PEOPLE_LANG)
							&& lang.equals(currNode.getProperty(
									PeopleNames.PEOPLE_LANG).getString()))
						return currNode;
				}
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unable to get synopse node for film with lang " + lang, re);
		}
	}

	/**
	 * Create a new film print node. TODO manage a better naming mechanism
	 * 
	 * @param film
	 * @return
	 */
	public static Node createFilmPrint(Node film) {
		try {
			Node prints = CommonsJcrUtils.getOrCreateDirNode(film, FILM_PRINTS);
			Node print = prints.addNode(FilmTypes.FILM_PRINT,
					FilmTypes.FILM_PRINT);
			return print;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new film print node", re);
		}
	}

	/**
	 * Add or update the a synopsis node given a film and a language. Short
	 * synopsis is optional
	 * */
	public static Node addOrUpdateSynopsisNode(Node film, String synopsis,
			String synopsisShort, String lang) {
		try {
			Node synopses = JcrUtils.mkdirs(film, FILM_SYNOPSES,
					NodeType.NT_UNSTRUCTURED);
			if (synopsis == null)
				// force creation of the property
				synopsis = "";

			if (CommonsJcrUtils.checkNotEmptyString(lang)) {
				Node sNode = null;
				if (synopses.hasNode(lang))
					sNode = synopses.getNode(lang);
				else
					sNode = synopses.addNode(lang, FilmTypes.FILM_SYNOPSIS);

				sNode.setProperty(SYNOPSIS_CONTENT, synopsis);
				if (CommonsJcrUtils.checkNotEmptyString(synopsisShort))
					sNode.setProperty(SYNOPSIS_CONTENT_SHORT, synopsisShort);
				sNode.setProperty(FILM_LANG, lang);
				return sNode;
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unable to get synopse node for film with lang " + lang, re);
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

		StaticOperand so = factory.literal(session.getValueFactory()
				.createValue(filmId));
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