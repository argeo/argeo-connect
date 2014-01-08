package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
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
			String latinTitle = CommonsJcrUtils
					.get(film, FILM_ORIG_LATIN_TITLE);
			if (CommonsJcrUtils.checkNotEmptyString(latinTitle))
				builder.append("<i>").append(latinTitle).append("</i>&#160;");

			// english title
			String enTitle = FilmJcrUtils.getAltTitle(film, PeopleConstants.LANG_EN);
			if (CommonsJcrUtils.checkNotEmptyString(enTitle))
				builder.append(enTitle);

			builder.append("<br/>");

			
			// Production
			StringBuilder currLine = new StringBuilder();
		
			String director = CommonsJcrUtils.get(film, FILM_DIRECTOR); 
			currLine.append(director);
			
			String countries = CommonsJcrUtils.getMultiAsString(film, FILM_PROD_COUNTRY, ", ");
			String year = CommonsJcrUtils.get(film, FILM_PROD_YEAR);
			
			boolean hasCountries = CommonsJcrUtils.checkNotEmptyString(countries);
			boolean hasYear = CommonsJcrUtils.checkNotEmptyString(year);
			
			if (hasCountries || hasYear){
				currLine.append(" [");
				currLine.append(countries);
				if (hasCountries && hasYear)
					currLine.append(", ");
				currLine.append(year);
				currLine.append("]");
			}
	
			if (currLine.length() > 0)
				builder.append(currLine.toString()).append("<br/>");
			
			String origLang = CommonsJcrUtils.getMultiAsString(film,
					FILM_ORIGINAL_LANGUAGE, ", ");
			String length = CommonsJcrUtils.get(film, FILM_LENGTH);
			builder.append("<i>");
			if (CommonsJcrUtils.checkNotEmptyString(origLang))
				builder.append(origLang.toUpperCase());
			if (CommonsJcrUtils.checkNotEmptyString(origLang)
					&& CommonsJcrUtils.checkNotEmptyString(length))
				builder.append(", ");
			builder.append(CommonsJcrUtils.get(film, FILM_LENGTH));
			builder.append("</i>");
			builder.append("</span>");

			String result = PeopleHtmlUtils.cleanHtmlString(builder.toString());
			return result;
		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}
