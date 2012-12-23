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
package org.argeo.gis.ui.rcp.swing;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import javax.swing.SwingUtilities;
import javax.swing.event.MouseInputAdapter;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.swing.JMapPane;
import org.geotools.swing.event.MapMouseEvent;
import org.geotools.swing.tool.AbstractZoomTool;

public class VersatileZoomTool extends AbstractZoomTool {
	// private Log log = LogFactory.getLog(VersatileZoomTool.class);

	// Cursors
	private Cursor zoomInCursor;
	private Cursor panCursor;
	private Cursor defaultCursor;

	// Variable values
	private Point2D startDragPos;
	private Point panePos;
	private boolean computingZoomBox;
	private boolean panning;

	private Point2D fieldPosition;

	/**
	 * Constructor
	 */
	public VersatileZoomTool() {
		zoomInCursor = new Cursor(Cursor.SE_RESIZE_CURSOR);
		panCursor = new Cursor(Cursor.HAND_CURSOR);
		defaultCursor = new Cursor(Cursor.CROSSHAIR_CURSOR);

		startDragPos = new DirectPosition2D();
		computingZoomBox = false;
		panning = false;

	}

	/**
	 * Zoom in by the currently set increment, with the map centred at the
	 * location (in world coords) of the mouse click
	 * 
	 * @param e
	 *            map mapPane mouse event
	 */
	@Override
	public void onMouseClicked(MapMouseEvent e) {
		if (SwingUtilities.isLeftMouseButton(e))
			centerMapToEvent(e, getZoom());
		else if (SwingUtilities.isRightMouseButton(e))
			centerMapToEvent(e, 1 / getZoom());
		else if (SwingUtilities.isMiddleMouseButton(e)) {
			if (fieldPosition != null) {
				Envelope2D env = new Envelope2D();
				final double increment = 0.1d;
				env.setFrameFromDiagonal(fieldPosition.getX() - increment,
						fieldPosition.getY() - increment, fieldPosition.getX()
								+ increment, fieldPosition.getY() + increment);
				getMapPane().setDisplayArea(env);
			}
		}
	}

	protected void centerMapToEvent(MapMouseEvent e, Double zoomArg) {
		Rectangle paneArea = getMapPane().getVisibleRect();
		DirectPosition2D mapPos = e.getMapPosition();

		double scale = getMapPane().getWorldToScreenTransform().getScaleX();
		double newScale = scale * zoomArg;

		DirectPosition2D corner = new DirectPosition2D(mapPos.getX() - 0.5d
				* paneArea.getWidth() / newScale, mapPos.getY() + 0.5d
				* paneArea.getHeight() / newScale);

		Envelope2D newMapArea = new Envelope2D();
		newMapArea.setFrameFromCenter(mapPos, corner);
		getMapPane().setDisplayArea(newMapArea);
	}

	/**
	 * Records the map position of the mouse event in case this button press is
	 * the beginning of a mouse drag
	 * 
	 * @param ev
	 *            the mouse event
	 */
	@Override
	public void onMousePressed(MapMouseEvent ev) {
		if (SwingUtilities.isLeftMouseButton(ev)) {
			startDragPos = new DirectPosition2D();
			startDragPos.setLocation(ev.getMapPosition());
		} else if (SwingUtilities.isMiddleMouseButton(ev)
				|| SwingUtilities.isRightMouseButton(ev)) {
			panePos = ev.getPoint();
			panning = true;
		}
	}

	/**
	 * Records that the mouse is being dragged
	 * 
	 * @param ev
	 *            the mouse event
	 */
	@Override
	public void onMouseDragged(MapMouseEvent ev) {
		if (SwingUtilities.isLeftMouseButton(ev)) {
			computingZoomBox = true;
		} else if (panning) {
			Point pos = ev.getPoint();
			if (!pos.equals(panePos)) {
				getMapPane().moveImage(pos.x - panePos.x, pos.y - panePos.y);
				panePos = pos;
			}
		}
		getMapPane().setCursor(getCursor());
	}

	/**
	 * If the mouse was dragged, determines the bounds of the box that the user
	 * defined and passes this to the mapPane's
	 * {@link org.geotools.swing.JMapPane#setDisplayArea(org.opengis.geometry.Envelope) }
	 * method
	 * 
	 * @param ev
	 *            the mouse event
	 */
	@Override
	public void onMouseReleased(MapMouseEvent ev) {
		if (computingZoomBox && !ev.getPoint().equals(startDragPos)) {
			Envelope2D env = new Envelope2D();
			env.setFrameFromDiagonal(startDragPos, ev.getMapPosition());
			computingZoomBox = false;
			getMapPane().setDisplayArea(env);
		} else if (panning) {
			panning = false;
			getMapPane().repaint();
		}
		getMapPane().setCursor(getCursor());
	}

	/**
	 * Get the mouse cursor for this tool
	 */
	@Override
	public Cursor getCursor() {
		if (computingZoomBox)
			return zoomInCursor;
		else if (panning)
			return panCursor;
		else
			return defaultCursor;
	}

	/**
	 * We use a custom drag box
	 */
	@Override
	public boolean drawDragBox() {
		return false;
	}

	@Override
	public void setMapPane(JMapPane pane) {
		super.setMapPane(pane);
		VariableDragBox dragBox = new VariableDragBox();
		getMapPane().addMouseListener(dragBox);
		getMapPane().addMouseMotionListener(dragBox);
		getMapPane().addMouseWheelListener(new MouseWheelListener() {
			private double clickToZoom = 0.2; // 1 wheel click is 20% zoom

			public void mouseWheelMoved(MouseWheelEvent ev) {
				Point mouseLocation = ev.getPoint();
				Point2D mouseCoor = getMapPane().getScreenToWorldTransform()
						.transform(mouseLocation, null);

				int clicks = ev.getWheelRotation();
				// -ve means wheel moved up, +ve means down
				int sign = (clicks < 0 ? -1 : 1);

				ReferencedEnvelope env = getMapPane().getDisplayArea();
				if (env == null)
					return;
				double width = env.getWidth();
				double delta = width * clickToZoom * sign;

				env.expandBy(delta);
				getMapPane().setDisplayArea(env);
				Point2D newMouseCoor = getMapPane().getScreenToWorldTransform()
						.transform(mouseLocation, null);
				// move envelope so that mouse always at the same geographical
				// coordinate
				env.translate(mouseCoor.getX() - newMouseCoor.getX(),
						mouseCoor.getY() - newMouseCoor.getY());
				getMapPane().setDisplayArea(env);
				getMapPane().repaint();
			}
		});
	}

	public void setFieldPosition(Point2D fieldPosition) {
		this.fieldPosition = fieldPosition;
	}

	/**
	 * Custom drag box (hacked from JMapPane) so that we can change the behavior
	 * depending on whether we pan or zoom.
	 */
	private class VariableDragBox extends MouseInputAdapter {

		private Point startPos;
		private Rectangle rect;
		private boolean dragged;

		VariableDragBox() {
			rect = new Rectangle();
			dragged = false;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			startPos = new Point(e.getPoint());
		}

		@Override
		public void mouseDragged(MouseEvent e) {
			if (computingZoomBox) {
				Graphics2D g2D = (Graphics2D) getMapPane().getGraphics();
				g2D.setColor(Color.WHITE);
				g2D.setXORMode(Color.RED);
				if (dragged) {
					g2D.drawRect(rect.x, rect.y, rect.width, rect.height);
				}

				rect.setFrameFromDiagonal(startPos, e.getPoint());
				g2D.drawRect(rect.x, rect.y, rect.width, rect.height);

				dragged = true;
			}
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			if (dragged) {
				Graphics2D g2D = (Graphics2D) getMapPane().getGraphics();
				g2D.setColor(Color.WHITE);
				g2D.setXORMode(Color.RED);
				g2D.drawRect(rect.x, rect.y, rect.width, rect.height);
				dragged = false;
			}
		}
	}

}
