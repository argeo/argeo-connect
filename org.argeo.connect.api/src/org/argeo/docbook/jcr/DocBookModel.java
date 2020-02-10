package org.argeo.docbook.jcr;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.ImportUUIDBehavior;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

public class DocBookModel {
	private Session session;

	public DocBookModel(Session session) {
		super();
		this.session = session;
	}

	public void setSession(Session session) {
		this.session = session;
	}

	public void importXml(String path, InputStream in) throws RepositoryException, IOException {
//		long begin = System.currentTimeMillis();
		session.importXML(path, in, ImportUUIDBehavior.IMPORT_UUID_COLLISION_REPLACE_EXISTING);
//		long duration = System.currentTimeMillis() - begin;
//		if (log.isTraceEnabled())
//			log.trace("Imported " + path + " in " + duration + " ms");

	}

	public void exportXml(String path, OutputStream out) throws RepositoryException, IOException {
		session.exportDocumentView(path, out, true, false);
	}
}
