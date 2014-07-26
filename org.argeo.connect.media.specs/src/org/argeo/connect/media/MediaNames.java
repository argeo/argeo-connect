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

	// Various parent nodes for films specific sub-concepts
	public final static String MAP_TITLES = "map:titles";// various langs
	public final static String MAP_TIMESTAMPS = "map:timestamps";
	public final static String MAP_SYNOPSES = "map:synopses";

	/* TITLES */
	public final static String MAP_TITLE_VALUE = "map:titleValue";
	public final static String MAP_TITLE_ARTICLE = "map:titleArticle";
	public final static String MAP_TITLE_LATIN_PRONUNCIATION = "map:titleLatinPronunciation";
	public final static String MAP_TITLE_IS_ORIG = "map:titleIsOrig";

	/* SYNOPSES */
	public final static String MAP_LOG_LINE = "map:logLine";
	public final static String MAP_SYNOPSIS_CONTENT = "map:synopsisContent";
	public final static String MAP_SYNOPSIS_CONTENT_SHORT = "map:synopsisContentShort";

	/* TIME STAMPS & AWARDS */
	public final static String MAP_TIMESTAMP_VALUE = "map:timestampValue";
	public final static String MAP_AWARD_COUNTRY_ISO = "map:awardCountryIso";

	/* FILMS */
	// specific business film ID
	public final static String FILM_TYPE = "film:type";

	public final static String FILM_PROD_YEAR = "film:prodYear";
	public final static String FILM_PROD_COUNTRIES = "film:prodCountries";
	public final static String FILM_PROD_COSTS = "film:prodCosts";
	public final static String FILM_ORIGINAL_LANGUAGES = "film:originalLanguages";
	public final static String FILM_AVAILABLE_SUBTITLES = "film:availableSubtitles";

	public final static String FILM_PREMIERE = "film:premiere";
	public final static String FILM_WEBSITE = "film:website";
	public final static String FILM_LENGTH = "film:length";

	public final static String FILM_CATEGORIES = "film:categories";
	public final static String FILM_ANIMATION_TECHNIQUE = "film:animationTechnique";
	public final static String FILM_GENRES = "film:genres";
	public final static String FILM_SOUND_TYPE = "film:soundType";
	public final static String FILM_COLOUR_TYPE = "film:colourType";

	// TODO implement corresponding concept
	// this property is not yet in the cnd, just there as a reminder.
	public final static String FILM_ASSIGNED_EDITION = "film:assignedEdition";

	// Maybe too specific - should be moved to client project
	public final static String FILM_IS_PREMIERE = "film:isPremiere";
	public final static String FILM_IS_STUDENT_PROJECT = "film:isStudentProject";
	public final static String FILM_IS_DEBUT_FILM = "film:isDebutFilm";
	public final static String FILM_HAS_TRAILER = "film:hasTrailer";
	public final static String FILM_ALLOW_EXCERPTS_ON_TV = "film:allowExcerptsOnTv";
	public final static String FILM_ALLOW_EXCERPTS_ON_WEB = "film:allowExcerptsOnWeb";
	public final static String FILM_SUBMITTED_FOR = "film:submittedFor";
	public final static String FILM_SHOOTING_FORMAT = "film:shootingFormat";

	public final static String FILM_PRINTS = "film:prints";

	/* FILM PRINTS */// [film:print] > nt:unstructured, mix:title,
						// people:orderable
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

	// [film:printDcp] > film:print
	public final static String FILM_PRINT_FILE_NAME = "film:printFileName";// (STRING)
	public final static String FILM_PRINT_RES = "resolution";// (STRING)
	public final static String FILM_PRINT_ENCRYPTED = "encrypted";// (BOOLEAN)
}