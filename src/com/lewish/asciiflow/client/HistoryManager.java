package com.lewish.asciiflow.client;

import java.util.ArrayList;
import java.util.List;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.lewish.asciiflow.shared.CellState;
import com.lewish.asciiflow.shared.CellStateMap;

/**
 * Relatively straightforward history manager for undo and redo operations. This
 * is aware of {@link CellStateMap} only.
 * 
 * @author lewis
 */
@Singleton
public class HistoryManager {

	private static HistoryManager instance;

	private final Canvas canvas;

	private List<CellStateMap> undoStates = new ArrayList<CellStateMap>();
	private int maxHistory = 100;
	private int currentState = -1;
	private CellStateMap mask = new CellStateMap();

	public CellStateMap getMask() {
		return mask;
	}

	public void setMask(CellStateMap mask) {
		this.mask = mask;
	}

	@Inject
	public HistoryManager(Canvas canvas) {
		this.canvas = canvas;
		if (instance == null) {
			instance = this;
		}
	}

	public void save(CellStateMap state) {
		if (undoStates.size() >= maxHistory
				&& currentState + 1 == undoStates.size()) {
			undoStates.remove(0);
		} else {
			// Erase old states
			while (undoStates.size() > currentState + 1) {
				undoStates.remove(undoStates.size() - 1);
			}
			currentState++;
		}
		undoStates.add(state);
	}

	public void undo() {
		if (currentState >= 0) {

			// create a diff of mask and current state
			CellStateMap state = undoStates.get(currentState);
			CellStateMap maskedState = new CellStateMap();
			for (CellState s : state.getCellStates()) {
				if (mask.getCellStates().contains(s)) {
					for (CellState p : mask.getCellStates()) {
						if (p.equals(s)) {
							maskedState.add((p.value == null || p.value
									.equals("null")) ? s : p);
						}
					}
				} else {
					maskedState.add(s);
				}
			}
			canvas.drawCellStates(maskedState);
			canvas.refreshDraw();
			undoStates.set(currentState--, canvas.commitDraw());
		}
	}

	public void redo() {
		if (undoStates.size() > currentState + 1) {
			// Window.alert("Redo: " + currentState);
			canvas.drawCellStates(undoStates.get(++currentState));
			canvas.refreshDraw();
			undoStates.set(currentState, canvas.commitDraw());
		}
	}

	public static HistoryManager get() {
		return instance;
	}

	public List<CellStateMap> getUndoStates() {
		return undoStates;
	}

	public CellStateMap getCurrentState() {
		return (currentState < 0) ? (new CellStateMap()) : (undoStates
				.get(currentState));
	}

	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		for (CellStateMap s : undoStates)
			output.append(s + "\n");
		output.append(currentState);
		return output.toString();
	}

}
