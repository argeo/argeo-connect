package org.argeo.connect.people.rap.exports.calc;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

import javax.jcr.PropertyType;
import javax.jcr.query.Row;

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
import org.argeo.connect.people.rap.PeopleRapConstants;
import org.argeo.connect.people.rap.exports.PeopleColumnDefinition;

public class RowsToCalcWriter {

	private final static Log log = LogFactory.getLog(RowsToCalcWriter.class);

	// private final static String SEPARATOR = "; ";

	// Must be set first
	private List<PeopleColumnDefinition> columnDefs;

	// private java.text.DateFormat formatter = new SimpleDateFormat(
	// PeopleUiConstants.DEFAULT_DATE_FORMAT);

	private WritableCellFormat tableHeaderFormat;
	private WritableCellFormat tableBodyStringFormat;
	private WritableCellFormat tableBodyDateFormat;
	private WritableCellFormat tableBodyIntFormat;
	private WritableCellFormat tableBodyFloatFormat;
	private WorkbookSettings wSettings;

	// public void setHeaderList(List<String> headerList) {
	// this.headerList = headerList;
	// }

	// private static final int maxNumberOfRowsPerSheet = 65536;
	public RowsToCalcWriter() {
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
					PeopleRapConstants.DEFAULT_DATE_FORMAT);
			tableBodyDateFormat = new WritableCellFormat(new WritableFont(
					WritableFont.ARIAL, 9), currDF);
			tableBodyDateFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

			tableBodyIntFormat = new WritableCellFormat(new WritableFont(
					WritableFont.ARIAL, 9), NumberFormats.INTEGER);
			tableBodyIntFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

			tableBodyFloatFormat = new WritableCellFormat(new WritableFont(
					WritableFont.ARIAL, 9), new NumberFormat(
					PeopleRapConstants.DEFAULT_NUMBER_FORMAT));
			tableBodyFloatFormat.setBorder(Border.ALL, BorderLineStyle.THIN);

		} catch (Exception e) {
			throw new PeopleException("Error preparing spreadsheet export.", e);
		}
	}

	/**
	 * Build excel file from the passed {@code Row} array.
	 */
	public void writeTableFromRows(File outputFile, Row[] rows,
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
			// WritableSheet sheet = workbook.getSheet("Main");

			this.columnDefs = columnDefs;

			// Fill the sheet
			writeHeader(sheet);
			writeBody(sheet, rows);

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
			e.printStackTrace();
			throw new PeopleException("Could not write header.", e);
		}
	}

	private void writeBody(WritableSheet sheet, Row[] rows) {
		int currentRow = 1;
		for (Row row : rows) {
			// while (iterator.hasNext()) {
			// Row row = iterator.nextRow();
			int i = 0;
			for (PeopleColumnDefinition currCol : columnDefs) {
				i = updateCell(currentRow, i, sheet, row, currCol);
			}
			currentRow++;
		}
		resizeColumns(sheet);

	}

	// Specific behaviour
	private int updateCell(int currRowIndex, int currColIndex,
			WritableSheet sheet, Row row, PeopleColumnDefinition currCol) {
		try {
			if (PropertyType.LONG == currCol.getPropertyType()) {
				sheet.addCell(new jxl.write.Number(
						currColIndex,
						currRowIndex,
						new Long(currCol.getColumnLabelProvider().getText(row)),
						tableBodyIntFormat));
				return currColIndex + 1;
			} else if (PropertyType.DECIMAL == currCol.getPropertyType()
					|| PropertyType.DOUBLE == currCol.getPropertyType()) {
				sheet.addCell(new jxl.write.Number(currColIndex, currRowIndex,
						new Double(currCol.getColumnLabelProvider()
								.getText(row)), tableBodyFloatFormat));
				return currColIndex + 1;

				// TODO re-implement Date management
				// } else if (PropertyType.DATE == currCol.getPropertyType()) {
				// sheet.addCell(new DateTime(currColIndex, currRowIndex, prop
				// .getDate().getTime(), tableBodyDateFormat));
				// return currColIndex + 1;
			} else {
				sheet.addCell(new Label(currColIndex, currRowIndex, currCol
						.getColumnLabelProvider().getText(row),
						tableBodyStringFormat));
				return currColIndex + 1;
			}
			// Node node = row.getNode(currCol.getSelectorName());
			// if (node.hasProperty(currCol.getPropertyName())) {
			// Property prop = node.getProperty(currCol.getPropertyName());
			// // String rawValueStr = prop.getString();
			//
			// if (prop.isMultiple()) {
			// // best effort
			// StringBuilder builder = new StringBuilder();
			// for (Value value : prop.getValues()) {
			// builder.append(value.getString()).append(SEPARATOR);
			// }
			// if (builder.lastIndexOf(SEPARATOR) > 0) {
			// builder.delete(builder.length() - 2, builder.length());
			// }
			// sheet.addCell(new Label(currColIndex, currRowIndex, builder
			// .toString(), tableBodyStringFormat));
			// return currColIndex + 1;
			// } else if (PropertyType.LONG == currCol.getPropertyType()) {
			// sheet.addCell(new jxl.write.Number(currColIndex,
			// currRowIndex, prop.getLong(), tableBodyIntFormat));
			// return currColIndex + 1;
			// } else if (PropertyType.DECIMAL == currCol.getPropertyType()
			// || PropertyType.DOUBLE == currCol.getPropertyType()) {
			// sheet.addCell(new jxl.write.Number(currColIndex,
			// currRowIndex, prop.getDouble(),
			// tableBodyFloatFormat));
			// return currColIndex + 1;
			// } else if (PropertyType.DATE == currCol.getPropertyType()) {
			// sheet.addCell(new DateTime(currColIndex, currRowIndex, prop
			// .getDate().getTime(), tableBodyDateFormat));
			// return currColIndex + 1;
			// } else {
			// sheet.addCell(new Label(currColIndex, currRowIndex, prop
			// .getString(), tableBodyStringFormat));
			// return currColIndex + 1;
			// }
			// }
			// return currColIndex + 1;

			// } catch (RepositoryException e) {
			// throw new PeopleException("Unable to get value for label: "
			// + currCol.getHeaderLabel(), e);
		} catch (RowsExceededException e) {
			throw new PeopleException("Two many rows", e);
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

	/**
	 * Initialises calc generation, to be called prior to any extract generation
	 */
	// public void setColumnDefinition(List<ColumnDefinition> columnDefinition)
	// {
	// this.columnDefs = columnDefinition;
	// }
}
