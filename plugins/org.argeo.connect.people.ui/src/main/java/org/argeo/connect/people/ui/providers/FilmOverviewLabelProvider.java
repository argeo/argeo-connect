package org.argeo.connect.people.ui.providers;

import java.util.concurrent.TimeUnit;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleNames;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class FilmOverviewLabelProvider extends ColumnLabelProvider implements
		FilmNames {

	private static final long serialVersionUID = 1L;
	private boolean isSmallList;

	// private PeopleService peopleService;

	public FilmOverviewLabelProvider(boolean isSmallList,
			PeopleService peopleService) {
		this.isSmallList = isSmallList;
		// this.peopleService = peopleService;
	}

	@Override
	public String getText(Object element) {

		try {
			Node node = (Node) element;
			Node film;
			if (node.isNodeType(FilmTypes.FILM))
				film = node;
			else if (node.isNodeType(PeopleTypes.PEOPLE_MEMBER)) {
				film = node.getParent().getParent();
			} else
				throw new PeopleException("Unvalid node type. "
						+ "Cannot display film information");

			StringBuilder builder = new StringBuilder();

			if (isSmallList)
				builder.append("<span>");
			else
				builder.append("<span style='font-size:15px;'>");

			// TODO clean this:
			// Must retrieve primary title
			// Must display corresponding latin prononciation if needed
			// Must display EN title if primary != EN && EN != null

			// first line
			builder.append("<b>");
			String idStr = CommonsJcrUtils.get(film, FilmNames.FILM_ID);
			if (CommonsJcrUtils.checkNotEmptyString(idStr))
				builder.append(idStr).append(" ~ ");
			builder.append("<big> ");
			builder.append(FilmJcrUtils.getTitleForFilm(film));
			builder.append("</big> </b> ");

			// latinTitle
			String latinTitle = CommonsJcrUtils.get(film,
					FILM_CACHE_OTITLE_LATIN);
			if (CommonsJcrUtils.checkNotEmptyString(latinTitle))
				builder.append("<i>").append(latinTitle).append("</i>&#160;");

			Node enTNode = FilmJcrUtils.getAltTitleNode(film,
					PeopleConstants.LANG_EN);

			// english title
			// skipped when not existing or when english is primary title
			if (enTNode != null
					&& (!enTNode.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY) || enTNode
							.hasProperty(PeopleNames.PEOPLE_IS_PRIMARY)
							&& !enTNode.getProperty(
									PeopleNames.PEOPLE_IS_PRIMARY).getBoolean())) {
				String enTitle = FilmJcrUtils.getAltTitle(film,
						PeopleConstants.LANG_EN);
				if (CommonsJcrUtils.checkNotEmptyString(enTitle))
					builder.append(enTitle);
			}
			builder.append("<br/>");

			// Production
			StringBuilder currLine = new StringBuilder();

			String director = CommonsJcrUtils.get(film, FILM_DIRECTOR);
			currLine.append(director);

			String countries = CommonsJcrUtils.getMultiAsString(film,
					FILM_PROD_COUNTRY, ", ");
			String year = CommonsJcrUtils.get(film, FILM_PROD_YEAR);

			boolean hasCountries = CommonsJcrUtils
					.checkNotEmptyString(countries);
			boolean hasYear = CommonsJcrUtils.checkNotEmptyString(year);

			if (hasCountries || hasYear) {
				currLine.append(" [");
				currLine.append(countries);
				if (hasCountries && hasYear)
					currLine.append(", ");
				currLine.append(year);
				currLine.append("]");
			} else
				currLine.append(", ");

			String origLang = CommonsJcrUtils.getMultiAsString(film,
					FILM_ORIGINAL_LANGUAGE, " / ");
			if (CommonsJcrUtils.checkNotEmptyString(origLang))
				currLine.append("&#160;").append(origLang);// origLang.toUpperCase()

			if (currLine.length() > 0)
				builder.append(currLine.toString()).append("<br/>");

			// NEW LINE
			currLine = new StringBuilder();

			// length
			Long length = CommonsJcrUtils.getLongValue(film, FILM_LENGTH);
			if (length != null)
				currLine.append(TimeUnit.SECONDS.toMinutes(length.longValue()));
			else
				currLine.append("-");
			currLine.append(" min ");

			// Type and genre
			String genres = CommonsJcrUtils.getMultiAsString(film, FILM_GENRES,
					", ");
			String type = CommonsJcrUtils.get(film, FILM_TYPE);

			if (CommonsJcrUtils.checkNotEmptyString(genres)
					|| CommonsJcrUtils.checkNotEmptyString(type)) {
				String sep = " ~ ";
				if (!isSmallList)
					currLine.append("<br/>");
				else
					currLine.append(sep);
				if (CommonsJcrUtils.checkNotEmptyString(type))
					currLine.append(type).append(" Film").append(sep);
				if (CommonsJcrUtils.checkNotEmptyString(genres))
					currLine.append(genres);
				else if (currLine.lastIndexOf(sep) >= 0)
					currLine = currLine
							.delete(currLine.length() - sep.length(),
									currLine.length());
			}

			if (currLine.length() > 0)
				builder.append("<i>").append(currLine.toString())
						.append("</i>").append("<br/>");

			// Close the span
			builder.append("</span>");

			String result = PeopleHtmlUtils.cleanHtmlString(builder.toString());
			return result;
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}