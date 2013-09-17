package org.argeo.connect.people.ui.providers;

import javax.jcr.Node;

import org.argeo.connect.film.FilmNames;
import org.argeo.connect.film.core.FilmJcrUtils;
import org.argeo.connect.people.ui.PeopleHtmlUtils;
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
		builder.append(CommonsJcrUtils.getStringValue(film, FilmNames.FILM_ID));
		builder.append(" ");
		builder.append("<b> ");
		builder.append(FilmJcrUtils.getTitleForFilm(film));
		builder.append(" </b>");
		builder.append(" [");
		builder.append(
				CommonsJcrUtils.getStringValue(film,
						FilmNames.FILM_PROD_COUNTRY)).append(", ");
		builder.append(CommonsJcrUtils.getStringValue(film,
				FilmNames.FILM_PROD_YEAR));
		builder.append("]");
		String result = PeopleHtmlUtils.cleanHtmlString(builder.toString());
		return result;
	}
}
