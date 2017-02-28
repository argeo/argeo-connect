package org.argeo.tracker.internal.charts;

/** Configure chartjs objects via JSon */
@SuppressWarnings("unused")
public class JsChartData {
	private String[] labels;
	private Object[] datasets;

	public JsChartData(String[] labels, Object[] datasets) {
		this.labels = labels;
		this.datasets = datasets;
	}
}
