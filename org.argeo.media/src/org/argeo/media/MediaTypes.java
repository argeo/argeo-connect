package org.argeo.media;

/** JCR node types managed by Connect Media Apps */
public interface MediaTypes {

	/* MAIN TYPES */
	String MAP_PROJECT = "map:project";
	String MAP_FILM = "map:film";

	/* GENERIC MAP TYPES */
	String MAP_SYNOPSIS = "map:synopsis";
	String MAP_TITLE = "map:title";
	String MAP_TIMESTAMP = "map:timestamp";
	String MAP_AWARD = "map:award";

	/* FILM SPECIFIC TYPES */
	String FILM_PRINT = "film:print";
	String FILM_PRINT_DCP = "film:printDcp";
}
