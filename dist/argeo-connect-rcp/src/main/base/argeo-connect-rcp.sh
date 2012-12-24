#!/bin/sh
java -Dosgi.framework=lib/org.eclipse.osgi-3.6.2.jar \
 -jar lib/org.eclipse.equinox.launcher-1.1.1.R36x_v20101122_1400.jar \
 -console -configuration conf -data data -clean
