package org.argeo.connect.ui.crisis.editors;

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
import org.argeo.eclipse.ui.Error;
import org.argeo.gis.GisNames;
import org.argeo.gis.GisTypes;
import org.argeo.gis.ui.MapControlCreator;
import org.argeo.gis.ui.MapViewer;
import org.argeo.gis.ui.editors.MapFormPage;
import org.argeo.jcr.ArgeoNames;
import org.argeo.jcr.ArgeoTypes;
import org.argeo.jcr.JcrUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

import com.sun.syndication.feed.synd.SyndEntry;

public class LinkEditor extends FormEditor {
	private String linksBasePath = "/connect/links";

	private MapControlCreator mapControlCreator;

	private Node context;

	private LinkBrowserPage linkBrowserPage;
	private MapFormPage mapFormPage;

	public void init(IEditorSite site, IEditorInput input)
			throws PartInitException {
		super.init(site, input);
		LinkEditorInput lei = (LinkEditorInput) getEditorInput();
		setPartName(lei.getUrl());
	}

	@Override
	protected void addPages() {
		try {
			LinkEditorInput lei = (LinkEditorInput) getEditorInput();
			context = lei.getContext();
			linkBrowserPage = new LinkBrowserPage(this, "browser", "Browser",
					lei);
			addPage(linkBrowserPage);
			mapFormPage = new MapFormPage(this, "map", "Map", lei.getContext(),
					mapControlCreator);
			addPage(mapFormPage);
		} catch (PartInitException e) {
			Error.show("Cannot initialize editor", e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void doSave(IProgressMonitor monitor) {
		LinkEditorInput lei = (LinkEditorInput) getEditorInput();
		String url = linkBrowserPage.getUrl();
		try {
			Session session = context.getSession();

			Node linkNode;
			String linkPath = linksBasePath + '/' + JcrUtils.urlAsPath(url);
			if (session.itemExists(linkPath)
					&& session.getNode(linkPath)
							.getProperty(ArgeoNames.ARGEO_URI).equals(url)) {
				linkNode = session.getNode(linkPath);
			}
			// syndication entry
			else if (lei.getSyndEntry() != null) {
				SyndEntry entry = lei.getSyndEntry();
				Calendar publishedDate = new GregorianCalendar();
				publishedDate.setTime(entry.getPublishedDate());
				linkNode = JcrUtils.mkdirs(session, linkPath);
				linkNode.addMixin(ConnectTypes.CONNECT_SYND_ENTRY);
				linkNode.setProperty(ArgeoNames.ARGEO_URI, url);
				linkNode.setProperty(Property.JCR_TITLE, entry.getTitle());
				linkNode.setProperty(Property.JCR_DESCRIPTION, entry
						.getDescription().getValue());
				linkNode.setProperty(
						ConnectNames.CONNECT_AUTHOR,
						(String[]) entry.getAuthors().toArray(
								new String[entry.getAuthors().size()]));
				linkNode.setProperty(ConnectNames.CONNECT_PUBLISHED_DATE,
						publishedDate);
				linkNode.setProperty(ConnectNames.CONNECT_UPDATED_DATE,
						publishedDate);
			}
			// raw link
			else {
				linkNode = JcrUtils.mkdirs(session, linkPath);
				linkNode.addMixin(ArgeoTypes.ARGEO_LINK);
				linkNode.setProperty(ArgeoNames.ARGEO_URI, url);
			}
			linkBrowserPage.doSave(monitor);

			// geographical features
			MapViewer mapViewer = mapFormPage.getMapViewer();
			for (NodeIterator nit = mapViewer.getSelectedFeatures(); nit
					.hasNext();) {
				Node featureNode = nit.nextNode();
				Node relatedFeature;
				if (linkNode.hasNode(featureNode.getName()))
					relatedFeature = featureNode.getNode(featureNode.getName());
				else
					relatedFeature = linkNode.addNode(featureNode.getName(),
							GisTypes.GIS_RELATED_FEATURE);
				relatedFeature.setProperty(Property.JCR_PATH,
						featureNode.getPath());
				relatedFeature.setProperty(GisNames.GIS_SRS, featureNode
						.getProperty(GisNames.GIS_SRS).getValue());
				relatedFeature.setProperty(GisNames.GIS_BBOX, featureNode
						.getProperty(GisNames.GIS_BBOX).getValue());
				relatedFeature.setProperty(GisNames.GIS_CENTROID, featureNode
						.getProperty(GisNames.GIS_CENTROID).getValue());

			}
			mapFormPage.doSave(monitor);

			linkNode.getSession().save();
			context = linkNode;
			firePropertyChange(PROP_DIRTY);

		} catch (RepositoryException e) {
			throw new ArgeoException("Cannot save link " + url, e);
		}
	}

	@Override
	public void doSaveAs() {
	}

	@Override
	public boolean isSaveAsAllowed() {
		return false;
	}

	public void setMapControlCreator(MapControlCreator mapControlCreator) {
		this.mapControlCreator = mapControlCreator;
	}

}
