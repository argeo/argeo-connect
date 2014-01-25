package org.argeo.connect.film;

/** JCR names managed by Connect Film. */
public interface FilmNames {

	/* FILMS */
	// specific business film ID
	public final static String FILM_ID = "film:filmId";

	public final static String FILM_PROD_YEAR = "film:productionYear";
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

	// Cache to ease full text search
	// following properties are all "on parent version" ignore and are used to
	// store primary information and thus fasten fulltextsearch
	public final static String PEOPLE_CACHE_PCITY = "people:cachePCity";
	public final static String PEOPLE_CACHE_PCOUNTRY = "people:cachePCountry";
	public final static String PEOPLE_CACHE_PORG = "people:cachePOrg";
	public final static String PEOPLE_CACHE_PPHONE = "people:cachePPhone";
	public final static String PEOPLE_CACHE_PMAIL = "people:cachePMail";
	public final static String PEOPLE_CACHE_PWeb = "people:cachePWeb";

	public final static String FILM_CACHE_OTITLE = "film:cacheOTitle";
	public final static String FILM_CACHE_OTITLE_ARTICLE = "film:cacheOTitleArticle";
	public final static String FILM_CACHE_OTITLE_LATIN = "film:cacheOTitleLatin";

	// A sub node with various titles (for various country / languages)
	public final static String FILM_TITLES = "film:titles";
	public final static String FILM_TITLE_VALUE = "film:titleValue";
	public final static String FILM_TITLE_ARTICLE = "film:titleArticle";
	public final static String FILM_TITLE_LATIN_PRONUNCIATION = "film:titleLatinPronunciation";
	public final static String FILM_TITLE_IS_ORIG = "film:titleIsOrig";

	// A sub node for time stamps and awards
	public final static String FILM_TIMESTAMPS = "film:timestamps";
	public final static String FILM_TIMESTAMP_VALUE = "film:timestampValue";
	public final static String FILM_AWARD_COUNTRY_ISO = "film:awardCountryIso";

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

	/* FILM PRINTS */
	// parent node for all film prints
	public final static String FILM_PRINTS = "film:prints";
	public final static String PRINT_TYPE = "film:printType";
	public final static String PRINT_FORMAT = "film:printFormat";
	public final static String PRINT_RATIO = "film:printRatio";
	public final static String PRINT_SOUND_FORMAT = "film:printSoundFormat";
	public final static String PRINT_LANGUAGE_VERSION = "film:printLanguageVersion";
	public final static String PRINT_FEE = "film:printFee";
	public final static String PRINT_FEE_INFO = "film:printFeeInfo";
	public final static String PRINT_SOURCE_CONTACT = "film:printSourceContact";
	public final static String PRINT_RETURN_CONTACT = "film:printReturnContact";

	/* MISCEALLENEOUS */
	// A tag to define the corresponding language(s) for a node or a property,
	// might be multiple
	@Deprecated
	public final static String FILM_LANG = "film:lang";

}
