package com.lewish.asciiflow.client;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Random;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lewish.asciiflow.client.CompressedStoreServiceAsync.CheckCallback;
import com.lewish.asciiflow.client.CompressedStoreServiceAsync.LoadCallback;
import com.lewish.asciiflow.client.CompressedStoreServiceAsync.SaveCallback;
import com.lewish.asciiflow.shared.State;

/**
 * The model holding the current state of a diagram save or load operation. The
 * actual model data is infact stored in the {@link Canvas}. This should
 * probably change.
 * 
 * @author lewis
 */
@Singleton
public class StoreModel {

	private static final int LOADING_INTERVAL = 3000;
	Timer loadTimer;

	public static interface ModelChangeHandler extends EventHandler {
		public void onModelChange(ModelChangeEvent event);
	}

	public static class ModelChangeEvent extends GwtEvent<ModelChangeHandler> {

		public static enum ModelChangeState {
			LOADED, SAVED, CLEARED, ;
		}

		public static final Type<ModelChangeHandler> TYPE = new Type<ModelChangeHandler>();
		public static final ModelChangeEvent LOADED = new ModelChangeEvent(
				ModelChangeState.LOADED);
		public static final ModelChangeEvent SAVED = new ModelChangeEvent(
				ModelChangeState.SAVED);
		public static final ModelChangeEvent CLEARED = new ModelChangeEvent(
				ModelChangeState.CLEARED);

		private final ModelChangeState state;

		public ModelChangeEvent(ModelChangeState state) {
			this.state = state;
		}

		@Override
		public Type<ModelChangeHandler> getAssociatedType() {
			return TYPE;
		}

		@Override
		protected void dispatch(ModelChangeHandler handler) {
			handler.onModelChange(this);
		}

		public ModelChangeState getState() {
			return state;
		}
	}

	private final HandlerManager handlerManager = new HandlerManager(this);

	private final CompressedStoreServiceAsync service;
	private final Canvas canvas;
	private final LoadingWidget loadingWidget;

	private State currentState;
	private Uri uri = null;
	private int owner;

	public Uri getUri() {
		return uri;
	}

	public void setUri(Uri uri) {
		this.uri = uri;
	}

	@Inject
	public StoreModel(CompressedStoreServiceAsync service, Canvas canvas,
			LoadingWidget loadingWidget) {
		this.service = service;
		this.canvas = canvas;
		this.loadingWidget = loadingWidget;

		owner = Random.nextInt();
		currentState = new State();
		currentState.setOwner(owner);

		// start periodic loading
		/*
		loadTimer = new Timer() {

			@Override
			public void run() {
				loadFromUri();
			}
		};
		loadTimer.scheduleRepeating(LOADING_INTERVAL);
		*/
	}

	public void load(final Long id, final Integer editCode, final boolean fromCheck, final HistoryManager historyManager) {
		loadingWidget.show();
		service.loadState(id, editCode, new LoadCallback() {
			@Override
			public void afterLoad(boolean success, State state) {
				loadingWidget.hide();
				currentState = state;
				currentState.setOwner(owner);
				fireEvent(ModelChangeEvent.LOADED);
				if (fromCheck == true) {
					historyManager.redo();//redo (for undo operation, undo)
					save();
				}
			}
		});
	}

	public void loadFromUri() {
		if (!uri.hasId()) {
			return;
		}
		Long id = uri.getId();
		Integer editCode = uri.getEditCode();
		// reload at every cost, even if nothing changed in the matter of
		// collaboration
		// if (id.equals(storeModel.getCurrentState().getId())
		// && editCode.equals(storeModel.getCurrentState().getEditCode())) {
		// return;
		// }
		load(id, editCode, false, null);
	}
	
	public void check(final HistoryManager historyManager) {
		loadingWidget.show();
		currentState.setCellStateMap(canvas.getCellStates());
		service.checkState(currentState, new CheckCallback() {
			@Override
			public void afterCheck(boolean success, State state) {
				loadingWidget.hide();
				if (success == false){
					//need to pass history manager in here
					historyManager.undo();//undo (for undo operation, redo)
					load(uri.getId(), uri.getEditCode(), true, historyManager);
				}
				else {
					save();
				}
			}
		});
	}

	public void save() {
		loadingWidget.show();
		currentState.setCellStateMap(canvas.getCellStates());
		service.saveState(currentState, new SaveCallback() {
			@Override
			public void afterSave(boolean success, State state) {
				loadingWidget.hide();
				currentState.setId(state.getId());
				currentState.setEditCode(state.getEditCode());
				fireEvent(ModelChangeEvent.SAVED);
			}
		});
	}

	public void clearState() {
		currentState = new State();
		fireEvent(ModelChangeEvent.CLEARED);
	}

	public State getCurrentState() {
		return currentState;
	}

	private void fireEvent(ModelChangeEvent event) {
		handlerManager.fireEvent(event);
	}

	public HandlerRegistration addModelChangeHandler(ModelChangeHandler handler) {
		return handlerManager.addHandler(ModelChangeEvent.TYPE, handler);
	}
}
