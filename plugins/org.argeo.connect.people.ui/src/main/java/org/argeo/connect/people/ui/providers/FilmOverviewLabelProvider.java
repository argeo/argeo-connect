package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.FilmTypes;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.PeopleService;
import org.argeo.connect.people.PeopleTypes;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * Provide a single column label provider for person lists
 */
public class FilmOverviewLabelProvider extends ColumnLabelProvider implements
		FilmNames {

	private static final long serialVersionUID = 6332600690181244737L;
	private boolean isSmallList;
	private PeopleService peopleService;

	public FilmOverviewLabelProvider(boolean isSmallList,
			PeopleService peopleService) {
		this.isSmallList = isSmallList;
		this.peopleService = peopleService;
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

			// first line
			builder.append("<b>");
			builder.append(CommonsJcrUtils.getStringValue(film, FILM_ID));
			builder.append(" ~ <big> ");
			builder.append(FilmJcrUtils.getTitleForFilm(film));
			builder.append("</big> </b> ");
			// latinTitle
			String latinTitle = CommonsJcrUtils.getStringValue(film,
					FILM_ORIG_LATIN_TITLE);
			if (latinTitle != null)
				builder.append("<i>").append(latinTitle).append("</i>");

			builder.append("<br/>");

			// english title
			String enTitle = FilmJcrUtils.getAltTitle(film, "EN");
			if (enTitle != null)
				builder.append(enTitle).append("<br/>");

			// Production
			builder.append(CommonsJcrUtils.getStringValue(film, FILM_DIRECTOR));
			builder.append(" [");
			builder.append(
					CommonsJcrUtils.getStringValue(film, FILM_PROD_COUNTRY))
					.append(", ");
			builder.append(CommonsJcrUtils.getStringValue(film, FILM_PROD_YEAR));
			builder.append("]");
			builder.append("<br/>");

			// original language & lenght
			String origLang = CommonsJcrUtils.getStringValue(film,
					FILM_ORIGINAL_LANGUAGE);

			builder.append("<i>");
			if (origLang != null)
				builder.append(origLang).append(", ");
			builder.append(CommonsJcrUtils.getStringValue(film, FILM_LENGHT));

			builder.append("</i>");

			builder.append("</span>");

			return builder.toString();

		} catch (RepositoryException re) {
			throw new PeopleException("Cannot create organizations content", re);
		}
	}
}
