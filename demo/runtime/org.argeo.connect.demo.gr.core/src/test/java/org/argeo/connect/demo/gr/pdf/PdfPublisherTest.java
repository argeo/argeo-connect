/*
 * Copyright (C) 2012 argeo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.argeo.connect.demo.gr.pdf;

import java.io.File;
import java.util.ArrayList;
import java.util.ListIterator;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageNode;

/**
 * 
 * Sandbox to test some functionalities of pdfBox framework.
 */
public class PdfPublisherTest extends TestCase {
	private final static Log log = LogFactory.getLog(PdfPublisherTest.class);

	public void testPdfBox() throws Exception {
		// uncomment usefull methods below
		// reverseOrder();
	}

	@SuppressWarnings("unused")
	private void reverseOrder() throws Exception {
		// the document

		String workingDir = "/home/bsinou/dev/stock/";
		PDDocument oldDoc = null;
		PDDocument newDoc = null;
		try {
			oldDoc = PDDocument.load(new File(workingDir + "scan.pdf"));
			int pageNb = oldDoc.getNumberOfPages();

			if (log.isDebugEnabled())
				log.debug("Got a doc with : " + pageNb + " pages.");
			PDPageNode rootPages = oldDoc.getDocumentCatalog().getPages();
			ListIterator<?> li = rootPages.getKids().listIterator();
			ArrayList<PDPage> tmp = new ArrayList<PDPage>();
			while (li.hasNext()) {
				Object obj = li.next();
				if (obj instanceof PDPage)
					tmp.add((PDPage) obj);
			}
			if (log.isDebugEnabled())
				log.debug("retrieved : " + tmp.size() + " PDPages.");

			Object[] tmpArr = tmp.toArray();
			newDoc = new PDDocument();

			for (int i = tmp.size() - 1; i >= 0; i--) {
				newDoc.addPage((PDPage) tmpArr[i]);
			}
			newDoc.save(workingDir + "reverseOrder.pdf");
		} finally {
			if (oldDoc != null) {
				oldDoc.close();
			}
			if (newDoc != null) {
				newDoc.close();
			}
		}

	}
}
