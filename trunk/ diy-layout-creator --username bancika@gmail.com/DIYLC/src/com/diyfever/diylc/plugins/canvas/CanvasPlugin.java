package com.diyfever.diylc.plugins.canvas;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.util.EnumSet;

import javax.swing.SwingConstants;

import com.diyfever.diylc.common.BadPositionException;
import com.diyfever.diylc.common.EventType;
import com.diyfever.diylc.common.IPlugIn;
import com.diyfever.diylc.common.IPlugInPort;
import com.diyfever.diylc.plugins.file.ProjectDrawingProvider;
import com.diyfever.gui.miscutils.ConfigurationManager;
import com.diyfever.gui.ruler.IRulerListener;
import com.diyfever.gui.ruler.RulerScrollPane;

public class CanvasPlugin implements IPlugIn {

	// private static final Logger LOG = Logger.getLogger(CanvasPlugin.class);

	private static final String METRIC_KEY = "metric";

	private RulerScrollPane scrollPane;
	private CanvasPanel canvasPanel;
	private IPlugInPort plugInPort;

	public CanvasPlugin() {
	}

	@Override
	public void connect(IPlugInPort plugInPort) {
		this.plugInPort = plugInPort;
		canvasPanel = new CanvasPanel(plugInPort);
		try {
			plugInPort.injectGUIComponent(getScrollPane(),
					SwingConstants.CENTER);
		} catch (BadPositionException e) {
			e.printStackTrace();
		}
	}

	private RulerScrollPane getScrollPane() {
		if (scrollPane == null) {
			scrollPane = new RulerScrollPane(canvasPanel,
					new ProjectDrawingProvider(plugInPort));
			Boolean metric = (Boolean) ConfigurationManager.getInstance()
					.getConfigurationItem(METRIC_KEY);
			if (metric == null) {
				metric = true;
			}
			scrollPane.setMetric(metric);
			scrollPane.addUnitListener(new IRulerListener() {

				@Override
				public void unitsChanged(boolean isMetric) {
					ConfigurationManager.getInstance().setConfigurationItem(
							METRIC_KEY, isMetric);
				}
			});
		}
		return scrollPane;
	}

	@Override
	public EnumSet<EventType> getSubscribedEventTypes() {
		return EnumSet.of(EventType.PROJECT_LOADED, EventType.ZOOM_CHANGED,
				EventType.REPAINT);
	}

	@Override
	public void processMessage(final EventType eventType, Object... params) {
		// SwingUtilities.invokeLater(new Runnable() {
		//
		// @Override
		// public void run() {
		// LOG.debug("event: " + eventType);
		switch (eventType) {
		case PROJECT_LOADED:
			refreshSize();
			if ((Boolean) params[1]) {
				// Scroll to the center.
				Rectangle visibleRect = canvasPanel.getVisibleRect();
				visibleRect.setLocation(
						(canvasPanel.getWidth() - visibleRect.width) / 2,
						(canvasPanel.getHeight() - visibleRect.height) / 2);
				canvasPanel.scrollRectToVisible(visibleRect);
				canvasPanel.revalidate();
			}
			// canvasPanel.validate();
			// canvasPanel.repaint();
			break;
		case ZOOM_CHANGED:
			refreshSize();
			break;
		case REPAINT:
			canvasPanel.invalidateCache();
			canvasPanel.repaint();
			break;
		}
		// }
		// });
	}

	private void refreshSize() {
		Dimension d = plugInPort.getCanvasDimensions(true);
		canvasPanel.setSize(d);
		canvasPanel.setPreferredSize(d);
		scrollPane.setZoomLevel(plugInPort.getZoomLevel());
	}
}