package org.argeo.connect.demo.gr.ui.exports;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import javax.jcr.Node;

import jxl.SheetSettings;
import jxl.Workbook;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.argeo.ArgeoException;
import org.argeo.connect.demo.gr.ui.providers.IJcrPropertyLabelProvider;

public class JcrToCalcWriter {
	// private final static Log log = LogFactory
	// .getLog(QueryResultCalcWriter.class);

	/* Must be set by calling command */
	private List<String> headerList;
	private List<String> headerLblList;
	private IJcrPropertyLabelProvider propLblProvider;

	private WritableCellFormat tableHeaderFormat;
	private WritableCellFormat tableBodyValueFormat;

	// private static final int maxNumberOfRowsPerSheet = 65536;
	public JcrToCalcWriter(ICalcExtractProvider provider, String extractId) {
		this.headerList = provider.getHeaderList(extractId);
		this.headerLblList = provider.getHeaderLblList(extractId);
		this.propLblProvider = provider.getLabelProvider(extractId);
		initializeSheetStyle();
	}

	/**
	 * Build a spreadsheet from the passed List<Node>. we do this way to enable
	 * the direct use of the jcr API rather than queries to build results
	 * (especially in the case of history request)
	 * 
	 */
	public void writeSpreadSheet(File outputFile, List<Node> nodes) {
		try {
			WritableWorkbook workbook = null;
			try {
				workbook = Workbook.createWorkbook(outputFile);
			} catch (FileNotFoundException fnfe) {
				throw new ArgeoException("Cannot create workbook", fnfe);
			}

			workbook.createSheet("Main", 0);
			WritableSheet sheet = workbook.getSheet("Main");

			// Fill the sheet
			writeHeader(sheet);
			writeBody(sheet, nodes);

			// Add some formatting
			SheetSettings ss = new SheetSettings(sheet);
			ss.setHorizontalFreeze(2);

			// finalize
			workbook.write();
			workbook.close();
		} catch (Exception e) {
			throw new ArgeoException("Could not write spreadsheet to file '"
					+ outputFile + "'.", e);
		}
	}

	private void writeHeader(WritableSheet sheet) {
		try {
			int currentRow = 0;
			int i = 0;
			for (String headerLbl : headerLblList) {
				sheet.addCell(new Label(i++, currentRow, headerLbl,
						tableHeaderFormat));
			}
		} catch (Exception e) {
			throw new ArgeoException("Could not write header.", e);
		}
	}

	private void writeBody(WritableSheet sheet, List<Node> nodes) {
		int currentRow = 1;
		try {
			for (Node node : nodes) {
				int i = 0;
				String curLabel;
				for (String headerLbl : headerList) {
					curLabel = propLblProvider.getFormattedPropertyValue(node,
							headerLbl);
					sheet.addCell(new Label(i++, currentRow, curLabel,
							tableBodyValueFormat));
				}
				currentRow++;
			}
			resizeColumns(sheet);
		} catch (RowsExceededException e) {
			throw new ArgeoException("Two many rows", e);
		} catch (WriteException e) {
			throw new ArgeoException(
					"Error while generating the body of the excel extract", e);
		}
	}

	protected void resizeColumns(WritableSheet sheet) {
		for (int i = 0; i < headerList.size(); i++) {
			int maxWidth = 0;
			for (int j = 0; j < 50; j++) {
				String currentContent = sheet.getCell(i, j).getContents();
				int currentWidth = currentContent.length();
				if (currentWidth > maxWidth)
					maxWidth = currentWidth;
			}
			sheet.setColumnView(i, maxWidth + 1);
		}
	}

	protected void initializeSheetStyle() {
		try {
			tableHeaderFormat = new WritableCellFormat();
			tableHeaderFormat.setBackground(Colour.BLUE_GREY);
			tableHeaderFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
			WritableFont fr = new WritableFont(WritableFont.ARIAL, 10);
			fr.setColour(Colour.WHITE);
			tableHeaderFormat.setFont(fr);
			tableBodyValueFormat = new WritableCellFormat();
			tableBodyValueFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
			tableBodyValueFormat
					.setFont(new WritableFont(WritableFont.ARIAL, 8));
			tableBodyValueFormat.setWrap(true);
		} catch (Exception e) {
			throw new ArgeoException("Error preparing spreadsheet export.", e);
		}
	}
}
