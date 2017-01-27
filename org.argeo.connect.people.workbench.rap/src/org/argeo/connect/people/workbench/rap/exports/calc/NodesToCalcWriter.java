package org.argeo.connect.people.workbench.rap.exports.calc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

import javax.jcr.Node;
import javax.jcr.PropertyType;

import jxl.Cell;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.CountryCode;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.write.DateFormat;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.NumberFormats;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.connect.people.PeopleConstants;
import org.argeo.connect.people.PeopleException;
import org.argeo.connect.people.ui.PeopleColumnDefinition;
import org.argeo.connect.ui.ConnectUiConstants;

/** Generate a spreadsheet from a Node array using jxl */
public class NodesToCalcWriter {
	private final static Log log = LogFactory.getLog(NodesToCalcWriter.class);

	// Must be set first
	private List<PeopleColumnDefinition> columnDefs;

	private WritableCellFormat tableHeaderFormat;
	private WritableCellFormat tableBodyStringFormat;
	private WritableCellFormat tableBodyDateFormat;
	private WritableCellFormat tableBodyIntFormat;
	private WritableCellFormat tableBodyFloatFormat;
	private WorkbookSettings wSettings;

	public NodesToCalcWriter() {
		try {
			/* General preferences */
			wSettings = new WorkbookSettings();
			wSettings.setLocale(new Locale(PeopleConstants.LANG_EN));

			String excelRegionalSettings = CountryCode.UK.getCode();
			wSettings.setExcelDisplayLanguage(excelRegionalSettings);
			wSettings.setExcelRegionalSettings(excelRegionalSettings);

			/* Initialize sheets style */
			tableHeaderFormat = new WritableCellFormat();
			tableHeaderFormat.setBackground(Colour.GRAY_50);
			tableHeaderFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
			WritableFont fr = new WritableFont(WritableFont.ARIAL, 11);
			fr.setColour(Colour.WHITE);
			tableHeaderFormat.setFont(fr);

			// Body fonts
			tableBodyStringFormat = new WritableCellFormat();
			tableBodyStringFormat.setBorder(Border.ALL, BorderLineStyle.THIN);
			tableBodyStringFormat.setFont(new WritableFont(WritableFont.ARIAL,
					9));
			tableBodyStringFormat.setWrap(true);

			DateFormat currDF = new DateFormat(
					ConnectUiConstants.DEFAULT_DATE_FORMAT);
			tableBodyDateFormat = new WritableCellFormat(new WritableFont(
					WritableFont.ARIAL, 9), currDF);
			tableBodyDateFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

			tableBodyIntFormat = new WritableCellFormat(new WritableFont(
					WritableFont.ARIAL, 9), NumberFormats.INTEGER);
			tableBodyIntFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

			tableBodyFloatFormat = new WritableCellFormat(new WritableFont(
					WritableFont.ARIAL, 9), new NumberFormat(
					ConnectUiConstants.DEFAULT_NUMBER_FORMAT));
			tableBodyFloatFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

		} catch (Exception e) {
			throw new PeopleException("Error preparing spreadsheet export.", e);
		}
	}

	/** Write a calc file from the passed {@code Node} array */
	public void writeTableFromNodes(File outputFile, Node[] nodes,
			List<PeopleColumnDefinition> columnDefs) {
		try {
			WritableWorkbook workbook = null;
			try {
				workbook = Workbook.createWorkbook(outputFile, wSettings);
				if (log.isTraceEnabled())
					log.trace("Workbook " + outputFile.getName() + " created");
			} catch (FileNotFoundException fnfe) {
				throw new PeopleException("Cannot create workbook", fnfe);
			}

			WritableSheet sheet = workbook.createSheet("Main", 0);

			this.columnDefs = columnDefs;
			// Fill the sheet
			writeHeader(sheet);
			if (nodes != null && nodes.length > 0)
				writeBody(sheet, nodes);

			// Add some formatting
			SheetSettings ss = sheet.getSettings();
			ss.setVerticalFreeze(1);

			// finalize
			workbook.write();
			workbook.close();

			if (log.isDebugEnabled())
				log.debug("Workbook generated in file " + outputFile.getName());
		} catch (Exception e) {
			throw new PeopleException("Could not write spreadsheet to file '"
					+ outputFile + "'.", e);
		}
	}

	private void writeHeader(WritableSheet sheet) {
		try {
			int currentRow = 0;
			int i = 0;
			for (PeopleColumnDefinition currColDef : columnDefs) {
				sheet.addCell(new Label(i++, currentRow, currColDef
						.getHeaderLabel(), tableHeaderFormat));
			}
		} catch (Exception e) {
			throw new PeopleException("Could not write header.", e);
		}
	}

	private void writeBody(WritableSheet sheet, Node[] nodes) {
		int currentRow = 1;
		for (Node node : nodes) {
			int i = 0;
			for (PeopleColumnDefinition currCol : columnDefs) {
				i = updateCell(currentRow, i, sheet, node, currCol);
			}
			currentRow++;
		}
		resizeColumns(sheet);
		// TODO does not work yet.
		// resizeRows(sheet);
	}

	// Specific behaviour
	private int updateCell(int currRowIndex, int currColIndex,
			WritableSheet sheet, Node node, PeopleColumnDefinition currCol) {
		try {
			if (PropertyType.LONG == currCol.getPropertyType()) {
				sheet.addCell(new jxl.write.Number(
						currColIndex,
						currRowIndex,
						new Long(currCol.getColumnLabelProvider().getText(node)),
						tableBodyIntFormat));
				return currColIndex + 1;
			} else if (PropertyType.DECIMAL == currCol.getPropertyType()
					|| PropertyType.DOUBLE == currCol.getPropertyType()) {
				sheet.addCell(new jxl.write.Number(currColIndex, currRowIndex,
						new Double(currCol.getColumnLabelProvider().getText(
								node)), tableBodyFloatFormat));
				return currColIndex + 1;
			} else {
				sheet.addCell(new Label(currColIndex, currRowIndex, currCol
						.getColumnLabelProvider().getText(node),
						tableBodyStringFormat));
				return currColIndex + 1;
			}
		} catch (RowsExceededException e) {
			throw new PeopleException("Too many rows", e);
		} catch (WriteException e) {
			throw new PeopleException(
					"Error while generating the body of the extract", e);
		}
	}

	protected void resizeColumns(WritableSheet sheet) {
		for (int i = 0; i < columnDefs.size(); i++) {
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

	protected void resizeRows(WritableSheet sheet) {
		boolean hasContent = true;
		int j = 1;
		while (hasContent) {
			hasContent = false;
			int maxHeigth = 0;
			loop: for (int i = 0; i < columnDefs.size(); i++) {
				Cell currCell = sheet.getCell(i, j);

				// We only manage height for label for the time being
				if (!(currCell instanceof Label))
					continue loop;
				Label currLabel = (Label) currCell;
				String currentContent = currCell.getContents();
				if ("".equals(currentContent.trim()))
					continue loop;
				else
					hasContent = true;
				String[] lines = currentContent.split("\n");
				int size = currLabel.getCellFormat().getFont().getPointSize();
				int currentHeigth = lines.length * size;

				if (currentHeigth > maxHeigth)
					maxHeigth = currentHeigth;
			}
			try {
				sheet.setRowView(j, maxHeigth + 1);
			} catch (RowsExceededException re) {
				throw new PeopleException("unable to resize row " + j, re);
			}
			j++;
		}
	}
}