package org.argeo.connect.media;

/** JCR names managed by Connect Film. */
public interface MediaNames {

	/* GENERIC CONCEPT */
	String MAP_PROJECT_ID = "map:projectId";

	// Cache some child properties to ease full text search:
	// primary title info, directors...
	// These properties are all "on parent version" ignore and are used to
	// store primary information and thus fasten fulltextsearch
	String MAP_CACHE_PTITLE = "map:cachePTitle";
	String MAP_CACHE_PTITLE_ARTICLE = "map:cachePTitleArticle";
	String MAP_CACHE_PTITLE_LATIN = "map:cachePTitleLatin";
	String MAP_CACHE_DIRECTORS = "map:cacheDirectors";
	// TODO implement this if needed.
	// String FILM_CACHE_PROD_COUNTRIES =
	// "film:cacheProdCountries";

	/* VARIOUS PARENT NODES */

	// generic map sub-concepts
	String MAP_TITLES = "map:titles";// internationalized
															// version of the
															// titles
	String MAP_TIMESTAMPS = "map:timestamps";
	String MAP_SYNOPSES = "map:synopses";

	// films specific sub-concepts
	String FILM_PRINTS = "film:prints";

	/* TITLES */
	String MAP_TITLE_VALUE = "map:titleValue";
	String MAP_TITLE_ARTICLE = "map:titleArticle";
	String MAP_TITLE_LATIN_PRONUNCIATION = "map:titleLatinPronunciation";
	String MAP_TITLE_IS_ORIG = "map:titleIsOrig";

	/* SYNOPSES */
	String MAP_LOGLINE = "map:logLine";
	String MAP_SYNOPSIS_CONTENT = "map:synopsisContent";
	String MAP_SYNOPSIS_CONTENT_SHORT = "map:synopsisContentShort";
	String MAP_SYNOPSIS_RELATED_BIO = "map:relatedBiography";

	/* TIME STAMPS & AWARDS */
	String MAP_TIMESTAMP_VALUE = "map:timestampValue";
	String MAP_AWARD_COUNTRY_ISO = "map:awardCountryIso";

	/* MAPS */
	String MAP_TYPE = "map:type";
	String MAP_CATEGORIES = "map:categories";
	String MAP_GENRES = "map:genres";
	String MAP_LENGTH = "map:length";

	String MAP_PROD_YEAR = "map:prodYear";
	String MAP_PROD_COUNTRIES = "map:prodCountries";
	String MAP_PROD_COSTS = "map:prodCosts";

	String MAP_WEBSITE = "map:website";
	String MAP_TRAILER_URL = "map:trailerUrl";
	String MAP_PREVIEW_URL = "map:previewUrl";
	String MAP_HAS_TRAILER = "map:hasTrailer";
	String MAP_ALLOW_EXCERPTS_ON_TV = "map:allowExcerptsOnTv";
	String MAP_ALLOW_EXCERPTS_ON_WEB = "map:allowExcerptsOnWeb";

	String MAP_ORIGINAL_LANGUAGES = "map:originalLanguages";
	String MAP_AVAILABLE_SUBTITLES = "map:availableSubtitles";

	String MAP_PREMIERE = "map:premiere";
	String MAP_IS_PREMIERE = "map:isPremiere";
	String MAP_IS_STUDENT_PROJECT = "map:isStudentProject";

	/* FILMS */
	String FILM_ANIMATION_TECHNIQUE = "film:animationTechnique";
	String FILM_SOUND_TYPE = "film:soundType";
	String FILM_COLOUR_TYPE = "film:colourType";

	String FILM_IS_DEBUT_FILM = "film:isDebutFilm";
	String FILM_SHOOTING_FORMAT = "film:shootingFormat";

	/* FILM PRINTS */
	// [film:print] > nt:unstructured, mix:title, people:orderable
	String FILM_PRINT_TYPE = "film:printType";// (STRING)
	String FILM_PRINT_NUMBER = "film:printNumber";// (STRING)
	// length in seconds
	String FILM_PRINT_DURATION = "film:printDuration"; // (LONG)

	// time codes
	String FILM_PRINT_TC_IN = "film:tcIn";// (STRING)
	String FILM_PRINT_TC_OUT = "film:tcOut";// (STRING)

	String FILM_PRINT_PICTURE_FORMAT = "film:printPictureFormat";// (STRING)
	String FILM_PRINT_ASPECT_RATIO = "film:printAspectRatio";// (STRING)
	String FILM_PRINT_FRAME_RATE = "film:printFrameRate";// (STRING)
	String FILM_PRINT_VIDEO_STEREOSCOPIC = "film:printVideoStereoscopic";// (STRING)
	String FILM_PRINT_VIDEO_CODEC = "film:printVideoCodec";// (STRING)
	String FILM_PRINT_HRES = "film:printHRes";// (STRING)
	String FILM_PRINT_VRES = "film:printVRes";// (STRING)
	String FILM_PRINT_AVG_BITRATE = "film:printAvgBitrate";// (STRING)

	String FILM_PRINT_AUDIO_LANGS = "film:printAudioLanguages";// (STRING)
	String FILM_PRINT_SOUND_FORMAT = "film:printSoundFormat";// (STRING)

	String FILM_PRINT_SUBTITLE_LANGS = "film:printSubtitleLanguages";// (STRING)
	String FILM_PRINT_SUBTITLE_FORMAT = "film:printSubtitleFormat";// (STRING)

	String FILM_PRINT_FEE = "film:printFee";// (STRING)
	String FILM_PRINT_FEE_INFO = "film:printFeeInfo";// (STRING)

	String FILM_PRINT_DEFAULT_SRC_CONTACT = "film:printDefaultSourceContact"; // (REFERENCE)
	String FILM_PRINT_DEFAULT_RETURN_CONTACT = "film:printDefaultReturnContact"; // (REFERENCE)

	// [film:printDcp] > film:print
	String FILM_PRINT_FILE_NAME = "film:printFileName";// (STRING)
	String FILM_PRINT_RES = "resolution";// (STRING)
	String FILM_PRINT_ENCRYPTED = "encrypted";// (BOOLEAN)
}
