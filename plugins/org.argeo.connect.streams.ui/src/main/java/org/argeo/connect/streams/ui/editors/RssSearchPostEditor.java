package org.argeo.connect.streams.ui.editors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.ui.editors.SearchEntityEditor;
import org.argeo.connect.streams.ui.RssUiPlugin;
import org.argeo.connect.streams.ui.providers.RssListLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;

/**
 * A simple list of all posts with a search
 */
public class RssSearchPostEditor extends SearchEntityEditor {
	final static Log log = LogFactory.getLog(RssSearchPostEditor.class);
	public final static String ID = RssUiPlugin.PLUGIN_ID
			+ ".rssSearchPostEditor";

	@Override
	protected ILabelProvider getCurrentLabelProvider() {
		return new RssListLabelProvider(false);
	}

	@Override
	protected int getCurrRowHeight() {
		return 60;
	}

}