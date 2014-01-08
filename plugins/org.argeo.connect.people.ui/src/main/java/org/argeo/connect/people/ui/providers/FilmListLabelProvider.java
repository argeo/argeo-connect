package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.ui.utils.PeopleHtmlUtils;
import org.argeo.connect.people.utils.CommonsJcrUtils;
import org.eclipse.jface.viewers.LabelProvider;

/**
 * Provide a single column label provider for film lists
 */
public class FilmListLabelProvider extends LabelProvider {

	private static final long serialVersionUID = 1L;

	public FilmListLabelProvider() {
	}

	@Override
	public String getText(Object element) {
		Node film = (Node) element;
		StringBuilder builder = new StringBuilder();
		String idStr = CommonsJcrUtils.get(film, FilmNames.FILM_ID);
		if (CommonsJcrUtils.checkNotEmptyString(idStr))
			builder.append(idStr).append(" ");
		builder.append("<b> ");
		builder.append(FilmJcrUtils.getTitleForFilm(film));
		builder.append(" </b>");

		String prodCountry = CommonsJcrUtils.getMultiAsString(film,
				FilmNames.FILM_PROD_COUNTRY, ", ");
		String prodYear = CommonsJcrUtils.get(film, FilmNames.FILM_PROD_YEAR);
		if (CommonsJcrUtils.checkNotEmptyString(prodCountry)
				|| CommonsJcrUtils.checkNotEmptyString(prodYear)) {
			builder.append(" [");
			builder.append(prodCountry);
			if (CommonsJcrUtils.checkNotEmptyString(prodCountry)
					&& CommonsJcrUtils.checkNotEmptyString(prodYear))
				builder.append(", ");
			builder.append(prodYear);
			builder.append("]");
		}
		String result = PeopleHtmlUtils.cleanHtmlString(builder.toString());
		return result;
	}
}
