package org.argeo.connect.film;

/** JCR names managed by Connect Film. */
public interface FilmNames {

	/* FILMS */
	public final static String FILM_ID = "film:uid";
	public final static String FILM_PROD_YEAR = "film:productionYear";
	public final static String FILM_PROD_COUNTRY = "film:productionCountry";
	// The corresponding value to ease various searches
	public final static String FILM_PROD_COUNTRY_STRING = "film:prodCountryString";
	public final static String FILM_LENGHT = "film:lenght";
	public final static String FILM_DIRECTOR = "film:director";
	public final static String FILM_ORIGINAL_LANGUAGE = "film:originalLanguage";
	// Main Title Management
	public final static String FILM_ORIGINAL_TITLE = "film:origTitle";
	public final static String FILM_ORIG_TITLE_ARTICLE = "film:origTitleArticle";
	public final static String FILM_ORIG_LATIN_TITLE = "film:origLatinTitle";
	// Alternative Titles (for various country / languages)
	public final static String FILM_ALT_TITLES = "film:altTitles";
	public final static String FILM_TITLE = "film:title";
	public final static String FILM_TITLE_ARTICLE = "film:titleArticle";

	/* SYNOPSES */
	public final static String SYNOPSIS_CONTENT = "film:synopsisContent";

	/* MISCEALLENEOUS */
	// A tag to define the corresponding language(s) for a node or a property,
	// might be multiple
	public final static String FILM_LANG = "film:lang";

}
