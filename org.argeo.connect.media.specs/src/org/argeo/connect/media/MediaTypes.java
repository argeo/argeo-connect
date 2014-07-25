package org.argeo.connect.media;

/** JCR node types managed by Connect Media Apps */
public interface MediaTypes {

	/* GENERIC CONCEPT */
	public final static String MAP_PROJECT = "map:project";

	public final static String MAP_SYNOPSIS = "map:synopsis";
	public final static String MAP_TITLE = "map:title";
	public final static String MAP_TIMESTAMP = "map:timestamp";
	public final static String MAP_AWARD = "map:award";

	/* FILMS */
	public final static String FILM_FILM = "film:film";
	public final static String FILM_PRINT = "film:print";
	public final static String FILM_PRINT_DCP = "film:printDcp";
}
