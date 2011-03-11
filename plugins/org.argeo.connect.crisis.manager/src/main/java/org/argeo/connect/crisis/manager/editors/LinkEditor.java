package org.argeo.connect.crisis.manager.editors;

import java.util.Calendar;
import java.util.GregorianCalendar;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.argeo.ArgeoException;
import org.argeo.connect.ConnectNames;
import org.argeo.connect.ConnectTypes;
import org.argeo.eclipse.ui.dialogs.Error;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.argeo.gis.ui.editors.MapFormPage;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.ArgeoTypes;
import org.argeo.jcr.JcrUtils;
import org.argeo.jcr.gis.GisTypes;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;

public class LinkEditor extends FormEditor {
	public final static String ID = "org.argeo.connect.crisis.manager.linkEditor";

	private String url;
	private String linksBasePath = "/connect/links";
	private String feedsBasePath = "/connect/feeds";

	private MapControlCreator mapControlCreator;

	private MapFormPage mapFormPage;

	@Override
	protected void addPages() {
		try {
			LinkEditorInput lei = (LinkEditorInput) getEditorInput();
			url = lei.getUrl();
			addPage(new LinkBrowserPage(this, "browser", "Browser", lei));
			mapFormPage = new MapFormPage(this, "map", "Map", null,
					mapControlCreator);
			addPage(mapFormPage);
		} catch (PartInitException e) {
			Error.show("Cannot initialize editor", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doSave(IProgressMonitor monitor) {
		try {
			LinkEditorInput lei = (LinkEditorInput) getEditorInput();
			Session session = lei.getContext().getSession();

			Node linkNode;
			// syndication entry
			if (lei.getSyndEntry() != null) {
				SyndEntry entry = lei.getSyndEntry();
				SyndFeed feed = entry.getSource();
				String feedPath = feedsBasePath + '/'
						+ JcrUtils.urlAsPath(feed.getUri());

				Node feedNode;
				if (!session.itemExists(feedPath)) {
					feedNode = JcrUtils.mkdirs(session, feedPath,
							ConnectTypes.CONNECT_SYND_FEED);
					feedNode.setProperty(ArgeoNames.ARGEO_URI, feed.getUri());
					feedNode.setProperty(Property.JCR_TITLE, feed.getTitle());
					feedNode.setProperty(Property.JCR_DESCRIPTION,
							feed.getDescription());
				} else {
					feedNode = session.getNode(feedPath);
				}

				Calendar publishedDate = new GregorianCalendar();
				publishedDate.setTime(entry.getPublishedDate());
				String name = entry.getTitle();
				String entryPath = feedPath + '/'
						+ JcrUtils.dateAsPath(publishedDate) + '/' + name;
				if (!session.itemExists(entryPath)) {
					linkNode = JcrUtils.mkdirs(session, entryPath,
							ConnectTypes.CONNECT_SYND_ENTRY);
					linkNode.setProperty(ArgeoNames.ARGEO_URI, entry.getUri());
					linkNode.setProperty(Property.JCR_TITLE, entry.getTitle());
					linkNode.setProperty(Property.JCR_DESCRIPTION, entry
							.getDescription().getValue());
					linkNode.setProperty(
							ConnectNames.CONNECT_AUTHOR,
							(String[]) entry.getAuthors().toArray(
									new String[entry.getAuthors().size()]));
				} else {
					linkNode = session.getNode(entryPath);
				}

			} else {
				String linkPath = linksBasePath + '/' + JcrUtils.urlAsPath(url);
				if (session.itemExists(linkPath)
						&& session.getNode(linkPath)
								.getProperty(ArgeoNames.ARGEO_LINK).equals(url)) {
					linkNode = session.getNode(linkPath);
				} else {
					linkNode = JcrUtils.mkdirs(session, linkPath,
							ArgeoTypes.ARGEO_LINKS);
				}
			}

			// geographical features
			MapViewer mapViewer = mapFormPage.getMapViewer();
			for (NodeIterator nit = mapViewer.getSelectedFeatures(); nit
					.hasNext();) {
				Node featureNode = nit.nextNode();
				Node relatedFeature = linkNode.addNode(featureNode.getName(),
						GisTypes.GIS_RELATED_FEATURE);
				relatedFeature.setProperty(Property.JCR_PATH,
						featureNode.getPath());

			}
			linkNode.getSession().save();
		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot save link " + url, e);
		}
	}

	@Override
	public void doSaveAs() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isSaveAsAllowed() {
		// TODO Auto-generated method stub
		return false;
	}

	public void setMapControlCreator(MapControlCreator mapControlCreator) {
		this.mapControlCreator = mapControlCreator;
	}

}
