#!/bin/sh
java -Dosgi.framework=lib/org.eclipse.osgi-3.6.2.jar \
 -jar lib/org.eclipse.equinox.launcher-1.1.1.jar \
 -console -configuration conf -data data -clean
