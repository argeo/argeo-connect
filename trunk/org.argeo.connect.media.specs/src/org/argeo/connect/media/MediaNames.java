package org.argeo.connect.media;

/** JCR names managed by Connect Film. */
public interface MediaNames {

	/* GENERIC CONCEPT */
	public final static String MAP_PROJECT_ID = "map:projectId";

	// Cache some children properties to ease full text search:
	// primary title info, directors...
	// These properties are all "on parent version" ignore and are used to
	// store primary information and thus fasten fulltextsearch
	public final static String MAP_CACHE_PTITLE = "map:cachePTitle";
	public final static String MAP_CACHE_PTITLE_ARTICLE = "map:cachePTitleArticle";
	public final static String MAP_CACHE_PTITLE_LATIN = "map:cachePTitleLatin";
	public final static String MAP_CACHE_DIRECTORS = "map:cacheDirectors";
	// TODO implement this if needed.
	// public final static String FILM_CACHE_PROD_COUNTRIES =
	// "film:cacheProdCountries";

	/* VARIOUS PARENT NODES */

	// generic map sub-concepts
	public final static String MAP_TITLES = "map:titles";// internationalized
															// version of the
															// titles
	public final static String MAP_TIMESTAMPS = "map:timestamps";
	public final static String MAP_SYNOPSES = "map:synopses";

	// films specific sub-concepts
	public final static String FILM_PRINTS = "film:prints";

	/* TITLES */
	public final static String MAP_TITLE_VALUE = "map:titleValue";
	public final static String MAP_TITLE_ARTICLE = "map:titleArticle";
	public final static String MAP_TITLE_LATIN_PRONUNCIATION = "map:titleLatinPronunciation";
	public final static String MAP_TITLE_IS_ORIG = "map:titleIsOrig";

	/* SYNOPSES */
	public final static String MAP_LOGLINE = "map:logLine";
	public final static String MAP_SYNOPSIS_CONTENT = "map:synopsisContent";
	public final static String MAP_SYNOPSIS_CONTENT_SHORT = "map:synopsisContentShort";
	public final static String MAP_SYNOPSIS_RELATED_BIO = "map:relatedBiography";

	/* TIME STAMPS & AWARDS */
	public final static String MAP_TIMESTAMP_VALUE = "map:timestampValue";
	public final static String MAP_AWARD_COUNTRY_ISO = "map:awardCountryIso";

	/* MAPS */
	public final static String MAP_TYPE = "map:type";
	public final static String MAP_CATEGORIES = "map:categories";
	public final static String MAP_GENRES = "map:genres";
	public final static String MAP_LENGTH = "map:length";

	public final static String MAP_PROD_YEAR = "map:prodYear";
	public final static String MAP_PROD_COUNTRIES = "map:prodCountries";
	public final static String MAP_PROD_COSTS = "map:prodCosts";

	public final static String MAP_WEBSITE = "map:website";
	public final static String MAP_TRAILER_URL = "map:trailerUrl";
	public final static String MAP_PREVIEW_URL = "map:previewUrl";
	public final static String MAP_HAS_TRAILER = "map:hasTrailer";
	public final static String MAP_ALLOW_EXCERPTS_ON_TV = "map:allowExcerptsOnTv";
	public final static String MAP_ALLOW_EXCERPTS_ON_WEB = "map:allowExcerptsOnWeb";

	public final static String MAP_ORIGINAL_LANGUAGES = "map:originalLanguages";
	public final static String MAP_AVAILABLE_SUBTITLES = "map:availableSubtitles";

	public final static String MAP_PREMIERE = "map:premiere";
	public final static String MAP_IS_PREMIERE = "map:isPremiere";
	public final static String MAP_IS_STUDENT_PROJECT = "map:isStudentProject";

	/* FILMS */
	public final static String FILM_ANIMATION_TECHNIQUE = "film:animationTechnique";
	public final static String FILM_SOUND_TYPE = "film:soundType";
	public final static String FILM_COLOUR_TYPE = "film:colourType";

	public final static String FILM_IS_DEBUT_FILM = "film:isDebutFilm";
	public final static String FILM_SHOOTING_FORMAT = "film:shootingFormat";

	/* FILM PRINTS */
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

	public final static String FILM_PRINT_AUDIO_LANGS = "film:printAudioLanguages";// (STRING)
	public final static String FILM_PRINT_SOUND_FORMAT = "film:printSoundFormat";// (STRING)

	public final static String FILM_PRINT_SUBTITLE_LANGS = "film:printSubtitleLanguages";// (STRING)
	public final static String FILM_PRINT_SUBTITLE_FORMAT = "film:printSubtitleFormat";// (STRING)

	public final static String FILM_PRINT_FEE = "film:printFee";// (STRING)
	public final static String FILM_PRINT_FEE_INFO = "film:printFeeInfo";// (STRING)

	public final static String FILM_PRINT_DEFAULT_SRC_CONTACT = "film:printDefaultSourceContact"; // (REFERENCE)
	public final static String FILM_PRINT_DEFAULT_RETURN_CONTACT = "film:printDefaultReturnContact"; // (REFERENCE)

	// [film:printDcp] > film:print
	public final static String FILM_PRINT_FILE_NAME = "film:printFileName";// (STRING)
	public final static String FILM_PRINT_RES = "resolution";// (STRING)
	public final static String FILM_PRINT_ENCRYPTED = "encrypted";// (BOOLEAN)
}