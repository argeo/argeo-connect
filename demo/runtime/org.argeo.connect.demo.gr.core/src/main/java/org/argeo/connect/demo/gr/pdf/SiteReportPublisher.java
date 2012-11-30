/*
 * Argeo Connect - Data management and communications
 * Copyright (C) 2012 Argeo GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 *
 * Additional permission under GNU GPL version 3 section 7
 *
 * If you modify this Program, or any covered work, by linking or combining it
 * with software covered by the terms of the Eclipse Public License, the
 * licensors of this Program grant you additional permission to convey the
 * resulting work. Corresponding Source for a non-source form of such a
 * combination shall include the source code for the parts of such software
 * which are used as well as that of the covered work.
 */
package org.argeo.connect.demo.gr.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.jcr.Binary;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDSimpleFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.GrBackendImpl;
import org.argeo.connect.demo.gr.GrNames;
import org.argeo.jcr.JcrUtils;

/** Generate a PDF report about a site */
public class SiteReportPublisher implements GrNames {
	// private final static Log log =
	// LogFactory.getLog(SiteReportPublisher.class);
	private int fontSize = 12;
	// private GrBackend grBackend;
	private Repository repository;

	public SiteReportPublisher(Repository repository) {
		this.repository = repository;
	}

	public File createNewReport(String siteUid) {
		try {
			File pdfTmpFile;
			pdfTmpFile = File.createTempFile("gr-siteReport", ".pdf");
			populateSiteReport(siteUid, pdfTmpFile.getAbsolutePath());
			pdfTmpFile.deleteOnExit();
			return pdfTmpFile;
		} catch (IOException ioe) {
			throw new ArgeoException(
					"IO while generating site report for site : " + siteUid,
					ioe);
		}

	}

	private void populateSiteReport(String siteUid, String filePath) {
		Session session = null;
		Binary binary = null;
		InputStream in = null;
		try {

			session = repository.login();
			Node site = session.getNodeByIdentifier(siteUid);

			SimpleDateFormat sdf = new SimpleDateFormat(
					"EEE, d MMM yyyy, HH:mm");
			// the document
			PDDocument doc = null;
			try {
				doc = new PDDocument();

				PDPage page = new PDPage();
				doc.addPage(page);

				PDSimpleFont fontBold = PDType1Font.HELVETICA_BOLD;
				PDSimpleFont fontOblique = PDType1Font.HELVETICA_OBLIQUE;
				PDSimpleFont fontNormal = PDType1Font.HELVETICA;
				float height = fontBold.getFontDescriptor()
						.getFontBoundingBox().getHeight() / 1000;
				// calculate font height and increase by 5 percent.
				height = height * fontSize * 1.05f;

				PDPageContentStream cs = new PDPageContentStream(doc, page);
				cs.beginText();
				cs.moveTextPositionByAmount(100, 700);

				writeValue(cs, "General information", null, fontOblique, null,
						height);

				writeValue(cs, "Site name : ",
						site.getProperty(Property.JCR_TITLE).getString(),
						fontBold, fontNormal, height);
				writeValue(cs, "Site id : ", site.getProperty(GR_UUID)
						.getString(), fontBold, fontNormal, height);
				writeValue(cs, "Site type : ", site.getProperty(GR_SITE_TYPE)
						.getString(), fontBold, fontNormal, height);
				Date lastModif = site.getProperty(Property.JCR_LAST_MODIFIED)
						.getDate().getTime();
				String lastModifStr = sdf.format(lastModif);
				writeValue(cs, "Last modified on ", lastModifStr
						+ " (by "
						+ site.getProperty(Property.JCR_LAST_MODIFIED_BY)
								.getString() + ")", fontBold, fontNormal,
						height);

				writeValue(cs, null, null, null, null, height);
				writeValue(cs, "Water quality monitoring", null, fontOblique,
						null, height);

				writeValue(cs, "Water level (m) : ",
						site.getProperty(GR_WATER_LEVEL).getString(), fontBold,
						fontNormal, height);
				writeValue(cs, "E-Coli rate (count/100ml) : ", site
						.getProperty(GR_ECOLI_RATE).getString(), fontBold,
						fontNormal, height);
				writeValue(cs, "Withdrawn water (m3/day) : ",
						site.getProperty(GR_WITHDRAWN_WATER).getString(),
						fontBold, fontNormal, height);

				cs.endText();

//				String fileName = "SitePicture.jpg";
//				if (site.hasNode(fileName)) {
//					try {
//						binary = site.getNode(fileName)
//								.getNode(Node.JCR_CONTENT)
//								.getProperty(Property.JCR_DATA).getBinary();
//						in = binary.getStream();
//						PDXObjectImage ximage = null;
//						ximage = new PDJpeg(doc, in);
//						cs.drawImage(ximage, 20, 20);
//					} catch (Exception e) {
//						log.error("Cannot write image from " + site, e);
//					}
//				}

				cs.close();
				doc.save(filePath);

				// Add images if there is some
				addImagesToPdf(filePath, site);
			} finally {
				JcrUtils.closeQuietly(binary);
				IOUtils.closeQuietly(in);
				if (doc != null) {
					doc.close();
				}
			}

		} catch (RepositoryException re) {
			throw new ArgeoException("JCR Error while gathering information "
					+ "to generate site report for site : " + siteUid, re);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			JcrUtils.logoutQuietly(session);
		}
	}

	protected void writeValue(PDPageContentStream contentStream, String label,
			String value, PDSimpleFont fontBold, PDSimpleFont font, float height)
			throws IOException {
		if (label != null) {
			contentStream.setFont(fontBold, fontSize);
			contentStream.drawString(label);
		}
		if (value != null) {
			contentStream.setFont(font, fontSize);
			contentStream.drawString(value);
		}
		contentStream.moveTextPositionByAmount(0, -height);

	}

	protected void addImagesToPdf(String fileName, Node siteNode) {
		try {
			NodeIterator it = siteNode.getNodes();
			while (it.hasNext()) {
				Node curNode = it.nextNode();
				if (curNode.isNodeType(NodeType.NT_FILE)) {
					if (curNode.getName().toLowerCase().endsWith(".jpg")) {
						File tmpFile = GrBackendImpl.getFileFromNode(curNode);
						addJpgImageToPdf(fileName, tmpFile);
					}
				}
			}
		} catch (RepositoryException re) {
			throw new ArgeoException(
					"Unexepected error while adding images to pdf file", re);
		}
	}

	private void addJpgImageToPdf(String fileName, File image) {
		PDDocument doc = null;
		try {

			doc = PDDocument.load(fileName);
			// we add the image to the first page.
			PDPage page = (PDPage) doc.getDocumentCatalog().getAllPages()
					.get(0);
			PDXObjectImage ximage = null;
			ximage = new PDJpeg(doc, new FileInputStream(image));
			PDPageContentStream contentStream;
			contentStream = new PDPageContentStream(doc, page, true, true);
			contentStream.drawImage(ximage, 100, 300);
			contentStream.close();
			doc.save(fileName);
		} catch (COSVisitorException e) {
			throw new ArgeoException("pdf box error while adding an image ", e);
		} catch (IOException e) {
			throw new ArgeoException("IO error while adding an image ", e);
		}
	}
}
