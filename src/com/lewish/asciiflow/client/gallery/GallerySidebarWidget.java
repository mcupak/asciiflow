package com.lewish.asciiflow.client.gallery;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import com.lewish.asciiflow.client.CompressedStoreServiceAsync;
import com.lewish.asciiflow.client.CompressedStoreServiceAsync.BatchedLoadCallback;
import com.lewish.asciiflow.client.resources.AsciiflowCss;
import com.lewish.asciiflow.shared.BatchStoreQueryResult;
import com.lewish.asciiflow.shared.State;

@Singleton
public class GallerySidebarWidget extends Composite {

	private final CompressedStoreServiceAsync compressedStoreService;
	private final Provider<GalleryPreviewWidget> previewWidgetProvider;
	private final List<State> states = new ArrayList<State>();
	private String cursorString = null;

	private final FlowPanel panel = new FlowPanel();
	private final Anchor getMoreLink;

	@Inject
	public GallerySidebarWidget(CompressedStoreServiceAsync compressedStoreService,
			Provider<GalleryPreviewWidget> previewWidgetProvider,
			AsciiflowCss css) {
		this.compressedStoreService = compressedStoreService;
		this.previewWidgetProvider = previewWidgetProvider;
		initWidget(panel);
		getMoreLink = new Anchor("See more Drawings");
		getMoreLink.setStyleName(css.getMoreLink());
		getMoreLink.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				fetchMore();
			}
		});
	}

	@Override
	protected void onAttach() {
		super.onAttach();
		if (cursorString == null) {
			fetchMore();
		}
	}

	private void fetchMore() {
		getMoreLink.removeFromParent();
		compressedStoreService.loadTenStates(cursorString, new BatchedLoadCallback() {
			@Override
			public void afterLoad(boolean success, BatchStoreQueryResult result) {
				GallerySidebarWidget.this.states.addAll(result.getStates());
				cursorString = result.getCursorString();
				for (State state : result.getStates()) {
					GalleryPreviewWidget widget = previewWidgetProvider.get();
					widget.setState(state);
					panel.add(widget);
				}
				panel.add(getMoreLink);
			}
		});
	}
}
