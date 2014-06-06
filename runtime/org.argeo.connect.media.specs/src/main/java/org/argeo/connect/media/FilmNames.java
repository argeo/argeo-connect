package org.argeo.connect.media;

/** JCR names managed by Connect Film. */
public interface FilmNames {

	/* FILMS */
	// specific business film ID
	public final static String FILM_ID = "film:filmId";
	public final static String FILM_TYPE = "film:type";

	public final static String FILM_PROD_YEAR = "film:prodYear";
	public final static String FILM_ORIGINAL_LANGUAGES = "film:originalLanguages";
	public final static String FILM_PROD_COUNTRIES = "film:prodCountries";

	public final static String FILM_WEBSITE = "film:website";
	public final static String FILM_LENGTH = "film:length";

	public final static String FILM_CATEGORIES = "film:categories";
	public final static String FILM_ANIMATION_TECHNIQUE = "film:animationTechnique";
	public final static String FILM_GENRES = "film:genres";
	public final static String FILM_SOUND_TYPE = "film:soundType";
	public final static String FILM_COLOUR_TYPE = "film:colourType";

	@Deprecated
	public final static String FILM_DIRECTORS = "film:directors";
	// The corresponding value to ease various searches
	// @Deprecated
	// Use FILM_CACHE_PROD_COUNTRIES instead.
	// public final static String FILM_PROD_COUNTRY_STRING =
	// "film:prodCountryString";

	// maybe too specific
	public final static String FILM_IS_PREMIERE = "film:isPremiere";
	public final static String FILM_IS_STUDENT_PROJECT = "film:isStudentProject";
	public final static String FILM_IS_DEBUT_FILM = "film:isDebutFilm";
	public final static String FILM_HAS_TRAILER = "film:hasTrailer";
	public final static String FILM_EXTRACTS_ON_TV_ALLOWED = "extractsOnTvAllowed";

	// Cache some children properties to ease full text search:
	// primary title info, directors...
	// These properties are all "on parent version" ignore and are used to
	// store primary information and thus fasten fulltextsearch
	public final static String FILM_CACHE_PTITLE = "film:cachePTitle";
	public final static String FILM_CACHE_PTITLE_ARTICLE = "film:cachePTitleArticle";
	public final static String FILM_CACHE_PTITLE_LATIN = "film:cachePTitleLatin";
	public final static String FILM_CACHE_DIRECTORS = "film:cacheDirectors";
	// TODO implement this if needed.
	// public final static String FILM_CACHE_PROD_COUNTRIES =
	// "film:cacheProdCountries";

	// Various parent nodes for films specific sub-concepts
	public final static String FILM_TITLES = "film:titles";// for various
															// languages
	public final static String FILM_TIMESTAMPS = "film:timestamps";
	public final static String FILM_PRINTS = "film:prints";
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
	// public final static String FILM_PRINT_TYPE = "film:printType";
	// public final static String FILM_PRINT_ = "";
	//
	// public final static String FILM_PRINT_PICTURE_FORMAT =
	// "film:printFormat";
	// public final static String FILM_PRINT_RATIO = "film:printRatio";
	// public final static String FILM_PRINT_SOUND_FORMAT =
	// "film:printSoundFormat";
	// public final static String FILM_PRINT_LANGUAGE_VERSION =
	// "film:printLanguageVersion";
	// public final static String FILM_PRINT_FEE = "film:printFee";
	// public final static String FILM_PRINT_FEE_INFO = "film:printFeeInfo";
	// public final static String FILM_PRINT_SOURCE_CONTACT =
	// "film:printSourceContact";
	// public final static String FILM_PRINT_RETURN_CONTACT =
	// "film:printReturnContact";

	// [film:print] > nt:unstructured, mix:title, people:orderable
	public final static String FILM_PRINT_TYPE = "film:printType";// (STRING)
	public final static String FILM_PRINT_NUMBER = "film:printNumber";// (STRING)
	// length in seconds
	public final static String FILM_PRINT_DURATION = "film:printDuration"; // (LONG)

	// time codes
	public final static String FILM_PRINT_TC_IN = "film:tcIn";// (STRING)
	public final static String FILM_PRINT_TC_OUT = "film:tcOut";// (STRING)

	public final static String FILM_PRINT_PICTURE_FORMAT = "film:printPictureFormat";// (STRING)
	public final static String FILM_PRINT_ASPECT_RATIO = "film:printAspectRatio";// (STRING)
	public final static String FILM_PRINT_FRAME_RATE = "film:printFrameRate";// (STRING)
	public final static String FILM_PRINT_VIDEO_STEREOSCOPIC = "film:printVideoStereoscopic";// (STRING)
	public final static String FILM_PRINT_VIDEO_CODEC = "film:printVideoCodec";// (STRING)
	public final static String FILM_PRINT_HRES = "film:printHRes";// (STRING)
	public final static String FILM_PRINT_VRES = "film:printVRes";// (STRING)
	public final static String FILM_PRINT_AVG_BITRATE = "film:printAvgBitrate";// (STRING)

	public final static String FILM_PRINT_AUDIO_LANG = "film:printAudioLang";// (STRING)
	public final static String FILM_PRINT_SOUND_FORMAT = "film:printSoundFormat";// (STRING)

	public final static String FILM_PRINT_SUBTITLE_LANG = "film:printSubtitleLang";// (STRING)
	public final static String FILM_PRINT_SUBTITLE_FORMAT = "film:printSubtitleFormat";// (STRING)
	// - film:printSoundFormat (STRING)
	// - film:printLanguageVersion (STRING)

	public final static String FILM_PRINT_FEE = "film:printFee";// (STRING)
	public final static String FILM_PRINT_FEE_INFO = "film:printFeeInfo";// (STRING)
	// public final static String FILM_PRINT_SOURCE_CONTACT =
	// "film:printSourceContact";// (REFERENCE)
	// public final static String FILM_PRINT_RETURN_CONTACT =
	// "film:printReturnContact";// (REFERENCE)

	// [film:printDcp] > film:print
	public final static String FILM_PRINT_FILE_NAME = "film:printFileName";// (STRING)
	public final static String FILM_PRINT_RES = "resolution";// (STRING)
	public final static String FILM_PRINT_ENCRYPTED = "encrypted";// (BOOLEAN)

}