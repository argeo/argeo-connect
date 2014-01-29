package org.argeo.connect.film.core;

import java.util.Calendar;

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

	public static boolean markAsOriginalTitle(Node parentNode,
			Node primaryChild, boolean isOriginal) {

		// TODO: remove original from other
		// put first

		try {
			// check if changed:
			if (((!isOriginal) && !primaryChild.hasProperty(FILM_TITLE_IS_ORIG))
					|| isOriginal
					&& primaryChild.hasProperty(FILM_TITLE_IS_ORIG)
					&& primaryChild.getProperty(FILM_TITLE_IS_ORIG)
							.getBoolean())
				return false;

			Node oldOriginal = getOriginalTitle(parentNode);
			if (oldOriginal != null)
				oldOriginal.setProperty(FILM_TITLE_IS_ORIG, false);

			if (isOriginal) {
				String titleStr = CommonsJcrUtils.get(primaryChild,
						FILM_TITLE_VALUE);
				String artStr = CommonsJcrUtils.get(primaryChild,
						FILM_TITLE_ARTICLE);
				String latinStr = CommonsJcrUtils.get(primaryChild,
						FILM_TITLE_LATIN_PRONUNCIATION);
				if (CommonsJcrUtils.checkNotEmptyString(titleStr))
					parentNode.setProperty(FILM_CACHE_OTITLE, titleStr);
				if (CommonsJcrUtils.checkNotEmptyString(artStr))
					parentNode.setProperty(FILM_CACHE_OTITLE_ARTICLE, artStr);
				if (CommonsJcrUtils.checkNotEmptyString(latinStr))
					parentNode.setProperty(FILM_CACHE_OTITLE_LATIN, latinStr);
			} else {
				if (parentNode.hasProperty(FILM_CACHE_OTITLE))
					parentNode.setProperty(FILM_CACHE_OTITLE, "");
				if (parentNode.hasProperty(FILM_CACHE_OTITLE_ARTICLE))
					parentNode.setProperty(FILM_CACHE_OTITLE_ARTICLE, "");
				if (parentNode.hasProperty(FILM_CACHE_OTITLE_LATIN))
					parentNode.setProperty(FILM_CACHE_OTITLE_LATIN, "");
			}
			primaryChild.setProperty(FILM_TITLE_IS_ORIG, isOriginal);
			return true;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to mark " + primaryChild
					+ " as primary", re);
		}
	}

	private static Node getSpecialTitle(Node parentNode, String propName) {
		try {
			if (!parentNode.hasNode(FILM_TITLES))
				return null;
			NodeIterator nit = parentNode.getNode(FILM_TITLES).getNodes();
			while (nit.hasNext()) {
				Node currNode = nit.nextNode();
				if (currNode.hasProperty(propName)
						&& currNode.getProperty(propName).getBoolean())
					return currNode;
			}
			return null;
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unable to get original/primary title node for "
							+ parentNode + " with property " + propName, re);
		}
	}

	public static Node getOriginalTitle(Node parentNode) {
		return getSpecialTitle(parentNode, FILM_TITLE_IS_ORIG);
	}

	public static Node getPrimaryTitle(Node parentNode) {
		return getSpecialTitle(parentNode, PeopleNames.PEOPLE_IS_PRIMARY);
	}

	public static boolean updatePrimaryTitle(Node parentNode, Node primaryChild) {
		// TODO: put first
		try {
			if (primaryChild.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
					&& primaryChild.getProperty(PeopleNames.PEOPLE_IS_PRIMARY)
							.getBoolean())
				return false; // already primary, nothing to do

			Node oldPrimary = getPrimaryTitle(parentNode);
			if (oldPrimary != null)
				oldPrimary.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, false);

			String titleStr = CommonsJcrUtils.get(primaryChild,
					FILM_TITLE_VALUE);
			String artStr = CommonsJcrUtils.get(primaryChild,
					FILM_TITLE_ARTICLE);
			if (CommonsJcrUtils.isEmptyString(titleStr))
				throw new PeopleException("Unable to set as primary "
						+ "a title with an empty value");
			else {
				primaryChild.setProperty(PeopleNames.PEOPLE_IS_PRIMARY, true);
				if (CommonsJcrUtils.checkNotEmptyString(artStr))
					titleStr += ", " + artStr;
				parentNode.setProperty(Property.JCR_TITLE, titleStr);
			}
			return true;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to mark " + primaryChild
					+ " as primary", re);
		}
	}

	/** Return a display string for the original title of the given film */
	public static String getTitleForFilm(Node film) {
		try {
			if (film.hasProperty(Property.JCR_TITLE))
				return film.getProperty(Property.JCR_TITLE).getString();

			// should never be used remove.
			String title = "";
			if (film.hasProperty(FILM_CACHE_OTITLE))
				title = film.getProperty(FILM_CACHE_OTITLE).getString();
			if (film.hasProperty(FILM_CACHE_OTITLE_ARTICLE))
				title += ", "
						+ film.getProperty(FILM_CACHE_OTITLE_ARTICLE)
								.getString();
			return title;
		} catch (RepositoryException re) {
			throw new ArgeoException("Unable to get title for film", re);
		}
	}

	/** Return a display string for the original title of the given film */
	@Deprecated
	public static void setOriginalTitle(Node film, String origTitle,
			String origTitleArticle, String origLatinTitle) {
		addTitle(film, origTitle, origTitleArticle, origLatinTitle, null, true,
				true);
		// try {
		// film.setProperty(FILM_ORIGINAL_TITLE, origTitle);
		// if (CommonsJcrUtils.checkNotEmptyString(origTitleArticle))
		// film.setProperty(FILM_ORIG_TITLE_ARTICLE, origTitleArticle);
		// if (CommonsJcrUtils.checkNotEmptyString(origLatinTitle))
		// film.setProperty(FILM_ORIG_LATIN_TITLE, origLatinTitle);
		// } catch (RepositoryException re) {
		// throw new ArgeoException(
		// "Unable to set original title on film node", re);
		// }
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
				title = altTitle.getProperty(FILM_TITLE_VALUE).getString();
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
			tNode.setProperty(FILM_TITLE_VALUE, title);
			if (CommonsJcrUtils.checkNotEmptyString(article))
				tNode.setProperty(FILM_TITLE_ARTICLE, article);
			if (CommonsJcrUtils.checkNotEmptyString(latinPronunciation))
				tNode.setProperty(FILM_TITLE_LATIN_PRONUNCIATION,
						latinPronunciation);
			tNode.setProperty(PeopleNames.PEOPLE_LANG, language);
			if (isOriginal)
				markAsOriginalTitle(film, tNode, true);

			if (isPrimary)
				updatePrimaryTitle(film, tNode);

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
	 * Create a new film timestamp.
	 * 
	 * @param film
	 * @return
	 */
	public static Node createTimestamp(Node film, Calendar date, String title,
			String description) {
		try {
			Node timestamps = CommonsJcrUtils.getOrCreateDirNode(film,
					FILM_TIMESTAMPS);
			String name = date.get(Calendar.YEAR) + "-"
					+ (date.get(Calendar.MONTH) + 1) + "-"
					+ date.get(Calendar.DAY_OF_MONTH);
			Node timestamp = timestamps.addNode(name, FilmTypes.FILM_TIMESTAMP);
			timestamp.setProperty(Property.JCR_TITLE, title);
			timestamp.setProperty(Property.JCR_DESCRIPTION, description);
			timestamp.setProperty(FILM_TIMESTAMP_VALUE, date);
			return timestamp;
		} catch (RepositoryException re) {
			throw new PeopleException("Unable to add a new film print node", re);
		}
	}

	/**
	 * Create a new film award.
	 * 
	 * @param film
	 * @return
	 */
	public static Node createAward(Node film, Calendar date, String isoCountry,
			String title, String description) {
		try {
			Node timestamps = CommonsJcrUtils.getOrCreateDirNode(film,
					FILM_TIMESTAMPS);
			String name = date.get(Calendar.YEAR) + "-"
					+ (date.get(Calendar.MONTH) + 1) + "-"
					+ date.get(Calendar.DAY_OF_MONTH);
			Node award = timestamps.addNode(name, FilmTypes.FILM_AWARD);
			award.setProperty(Property.JCR_TITLE, title);
			award.setProperty(Property.JCR_DESCRIPTION, description);
			award.setProperty(FILM_TIMESTAMP_VALUE, date);
			award.setProperty(FILM_AWARD_COUNTRY_ISO, isoCountry);
			return award;
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
				sNode.setProperty(PeopleNames.PEOPLE_LANG, lang);
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