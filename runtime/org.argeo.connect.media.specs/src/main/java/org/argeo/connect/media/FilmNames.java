package org.argeo.connect.media;

/** JCR names managed by Connect Film. */
public interface FilmNames {

	/* FILMS */
	// specific business film ID
	public final static String FILM_ID = "film:filmId";
	public final static String FILM_TYPE = "film:type";

	public final static String FILM_PROD_YEAR = "film:prodYear";
	public final static String FILM_ORIGINAL_LANGUAGE = "film:originalLanguage";
	public final static String FILM_PROD_COUNTRY = "film:prodCountry";
	// The corresponding value to ease various searches
	public final static String FILM_PROD_COUNTRY_STRING = "film:prodCountryString";

	public final static String FILM_WEBSITE = "film:website";

	// Should be multiple ??
	public final static String FILM_DIRECTOR = "film:director";
	public final static String FILM_LENGTH = "film:length";

	public final static String FILM_CATEGORIES = "film:categories";
	public final static String FILM_ANIMATION_TECHNIQUE = "film:animationTechnique";
	public final static String FILM_GENRES = "film:genres";

	// Cache primary title info to ease full text search
	// following properties are all "on parent version" ignore and are used to
	// store primary information and thus fasten fulltextsearch
	public final static String FILM_CACHE_PTITLE = "film:cachePTitle";
	public final static String FILM_CACHE_PTITLE_ARTICLE = "film:cachePTitleArticle";
	public final static String FILM_CACHE_PTITLE_LATIN = "film:cachePTitleLatin";

	// Various parent nodes for films specific sub-concepts
	// titles (for various country / languages)
	public final static String FILM_TITLES = "film:titles";
	// time stamps and awards
	public final static String FILM_TIMESTAMPS = "film:timestamps";
	// film prints
	public final static String FILM_PRINTS = "film:prints";
	// synopses
	public final static String FILM_SYNOPSES = "film:synopses";

	/* TITLES */
	public final static String FILM_TITLE_VALUE = "film:titleValue";
	public final static String FILM_TITLE_ARTICLE = "film:titleArticle";
	public final static String FILM_TITLE_LATIN_PRONUNCIATION = "film:titleLatinPronunciation";
	public final static String FILM_TITLE_IS_ORIG = "film:titleIsOrig";

	/* SYNOPSES */
	public final static String FILM_LOG_LINE = "film:logLine";
	public final static String FILM_SYNOPSIS_CONTENT = "film:synopsisContent";
	public final static String FILM_SYNOPSIS_CONTENT_SHORT = "film:synopsisContentShort";

	/* TIME STAMPS & AWARDS */
	public final static String FILM_TIMESTAMP_VALUE = "film:timestampValue";
	public final static String FILM_AWARD_COUNTRY_ISO = "film:awardCountryIso";

	/* FILM PRINTS */
	public final static String FILM_PRINT_TYPE = "film:printType";
	public final static String FILM_PRINT_FORMAT = "film:printFormat";
	public final static String FILM_PRINT_RATIO = "film:printRatio";
	public final static String FILM_PRINT_SOUND_FORMAT = "film:printSoundFormat";
	public final static String FILM_PRINT_LANGUAGE_VERSION = "film:printLanguageVersion";
	public final static String FILM_PRINT_FEE = "film:printFee";
	public final static String FILM_PRINT_FEE_INFO = "film:printFeeInfo";
	public final static String FILM_PRINT_SOURCE_CONTACT = "film:printSourceContact";
	public final static String FILM_PRINT_RETURN_CONTACT = "film:printReturnContact";

	// maybe too specific
	public final static String FILM_IS_PREMIERE = "film:isPremiere";
	public final static String FILM_IS_STUDENT_PROJECT = "film:isStudentProject";
	public final static String FILM_IS_DEBUT_FILM = "film:isDebutFilm";
	public final static String FILM_HAS_TRAILER = "film:hasTrailer";
	public final static String FILM_EXTRACTS_ON_TV_ALLOWED = "extractsOnTvAllowed";

	/* MISCEALLENEOUS */
	// A tag to define the corresponding language(s) for a node or a property,
	// might be multiple
	// @Deprecated
	// public final static String FILM_LANG = "film:lang";
}