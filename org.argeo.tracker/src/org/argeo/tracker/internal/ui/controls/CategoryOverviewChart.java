package org.argeo.tracker.internal.ui.controls;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.argeo.cms.util.CmsUtils;
import org.argeo.tracker.TrackerException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressEvent;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

/** Simple composite widget to display a gauge value as chart */
public class CategoryOverviewChart extends Composite {
	private static final long serialVersionUID = -2758463753509540538L;
	private final Log log = LogFactory.getLog(CategoryOverviewChart.class);

	private String options = "options: { legend: { display: false, position: 'bottom' } , "
			+ " title: { display: true, position: 'bottom',  fontStyle: 'none', text: customisedTitle } , "
			+ "scales: { xAxes: [{ display: false, ticks: { beginAtZero: true }}], "
			+ "yAxes: [{ barThickness: 10, display: false, stacked: true }]}}";

	public CategoryOverviewChart(Composite parent, int style) {
		super(parent, style);
		// parent.setLayout(EclipseUiUtils.noSpaceGridLayout());
	}

	public void setInput(String title, int closedNb, int totalNb) {
		Composite parent = this;
		CmsUtils.clear(parent);

		final String jscData = "{labels: ['" + title + "'], datasets: ["
				+ "{ label: 'Open', backgroundColor: \"rgb(141,192,66) \", data: [" + closedNb + "], },"
				+ " { label: 'All', data: [" + totalNb + "]} ]}";

		parent.setLayout(new GridLayout());
		final Browser browser = new Browser(parent, SWT.NONE);
		browser.setLayoutData(new GridData(300, 80));
		String html;
		InputStream in = null;
		try {
			in = getClass().getResourceAsStream("/org/argeo/tracker/internal/charts/ChartJs.html");
			html = IOUtils.toString(in);
		} catch (IOException e) {
			throw new TrackerException("Cannot read HTML", e);
		} finally {
			IOUtils.closeQuietly(in);
		}
		browser.addProgressListener(new ProgressListener() {
			private static final long serialVersionUID = 2852043674756868097L;

			@Override
			public void completed(ProgressEvent event) {
				try {
					String type = "horizontalBar";
					StringBuilder js = new StringBuilder();
					double a = (double) closedNb, b = (double) totalNb;
					double indic = a / b*100;
					StringBuilder lb = new StringBuilder();
					lb.append(String.format("%.0f", indic)).append("% ");
					lb.append("(").append(closedNb).append("/").append(totalNb).append(" done)");

					js.append("var customisedTitle=").append("'").append(lb.toString()).append("'").append(";\n");
					js.append("var chartData=").append(jscData).append(";\n");
					js.append("new Chart(").append("document.getElementById('chart').getContext('2d'),").append("{")
							.append("type:'").append(type).append("',").append("data:chartData,\n").append(options)
							.append("});");
					try {
						if (log.isTraceEnabled())
							log.debug(js.toString());
						browser.evaluate(js.toString());
					} catch (SWTException e) {
						log.error(e.getMessage() + "\n" + js + "\n");
					}
				} catch (Exception e) {
					new TrackerException("Unable to prepare chart", e);
				}
			}

			@Override
			public void changed(ProgressEvent event) {
			}
		});
		browser.setText(html);
		parent.layout(true, true);
	}

	@Override
	public boolean setFocus() {
		return true;
	}
}
