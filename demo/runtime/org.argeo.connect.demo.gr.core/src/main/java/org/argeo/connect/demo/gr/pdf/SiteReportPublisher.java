package org.argeo.connect.demo.gr.pdf;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.nodetype.NodeType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.edit.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDJpeg;
import org.apache.pdfbox.pdmodel.graphics.xobject.PDXObjectImage;
import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.service.GrBackend;


public class SiteReportPublisher {
	private final static Log log = LogFactory.getLog(SiteReportPublisher.class);

	private GrBackend grBackend;

	public SiteReportPublisher(GrBackend grBackend) {
		this.grBackend = grBackend;
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
		try {

			Session session = grBackend.getCurrentSession();
			Node site = session.getNodeByIdentifier(siteUid);

			// the document
			PDDocument doc = null;
			try {
				doc = new PDDocument();

				PDPage page = new PDPage();
				doc.addPage(page);
				PDFont font = PDType1Font.HELVETICA_BOLD;

				PDPageContentStream contentStream = new PDPageContentStream(
						doc, page);
				contentStream.beginText();
				contentStream.setFont(font, 12);
				contentStream.moveTextPositionByAmount(100, 700);
				contentStream.drawString("NOM DU SITE : " + site.getName());
				contentStream.endText();
				contentStream.close();
				doc.save(filePath);

				// Add images if there is some
				addImagesToPdf(filePath, site);
			} finally {
				if (doc != null) {
					doc.close();
				}
			}

		} catch (RepositoryException re) {
			throw new ArgeoException("JCR Error while gathering information "
					+ "to generate site report for site : " + siteUid, re);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addImagesToPdf(String fileName, Node siteNode) {
		try {
			NodeIterator it = siteNode.getNodes();
			while (it.hasNext()) {
				Node curNode = it.nextNode();
				log.debug("Cur Node name " + curNode.getName());
				if (curNode.isNodeType(NodeType.NT_FILE)) {

					if (curNode.getName().toLowerCase().endsWith(".jpg")) {
						File tmpFile = grBackend.getFileFromNode(curNode);
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
			contentStream.drawImage(ximage, 20, 20);
			contentStream.close();
			doc.save(fileName);
		} catch (COSVisitorException e) {
			throw new ArgeoException("pdf box error while adding an image ", e);
		} catch (IOException e) {
			throw new ArgeoException("IO error while adding an image ", e);
		}
	}
}
