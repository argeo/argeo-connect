package org.argeo.connect.film;

/** JCR names managed by Connect Film. */
public interface FilmNames {

	/* FILMS */
	// specific business film ID
	public final static String FILM_ID = "film:filmId";
	
	public final static String FILM_PROD_YEAR = "film:productionYear";
	// Might be multiple
	public final static String FILM_ORIGINAL_LANGUAGE = "film:originalLanguage";
	public final static String FILM_PROD_COUNTRY = "film:productionCountry";
	// The corresponding value to ease various searches
	public final static String FILM_PROD_COUNTRY_STRING = "film:prodCountryString";
	
	// Should be multiple ??
	public final static String FILM_DIRECTOR = "film:director";
	public final static String FILM_LENGTH = "film:length";
	public final static String FILM_LENGTH_IN_MIN = "film:lengthInMinutes";
	
	public final static String FILM_CATEGORY = "film:category";
	public final static String FILM_ANIMATION_TECHNIQUE = "film:animationTechnique";
	public final static String FILM_GENRE = "film:genre";
	
	// Main Title Management
	public final static String FILM_ORIGINAL_TITLE = "film:origTitle";
	public final static String FILM_ORIG_TITLE_ARTICLE = "film:origTitleArticle";
	public final static String FILM_ORIG_LATIN_TITLE = "film:origLatinTitle";
	
	// A sub node with alternative titles (for various country / languages)
	public final static String FILM_ALT_TITLES = "film:altTitles";
	public final static String FILM_TITLE = "film:title";
	public final static String FILM_TITLE_ARTICLE = "film:titleArticle";

	// maybe too specific
	public final static String FILM_IS_FEATURE = "film:isFeature";
	public final static String FILM_IS_PREMIERE = "film:isPremiere";
	public final static String FILM_IS_STUDENT_PROJECT = "film:isStudentProject";
	public final static String FILM_IS_DEBUT_FILM = "film:isDebutFilm";
	public final static String FILM_WEBSITE = "film:website";

	/* SYNOPSES */
	// parent node for all synopses
	public final static String FILM_SYNOPSES = "film:synopses";
	public final static String SYNOPSIS_CONTENT = "film:synopsisContent";
	public final static String SYNOPSIS_CONTENT_SHORT = "film:synopsisContentShort";

	/* MISCEALLENEOUS */
	// A tag to define the corresponding language(s) for a node or a property,
	// might be multiple
	public final static String FILM_LANG = "film:lang";
}
