package com.lewish.asciiflow.shared;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;

import com.google.appengine.api.datastore.Text;

public class CellStateMap implements Serializable {

	private static final long serialVersionUID = -3792565912715855738L;
	private HashMap<String, CellState> maps = new HashMap<String, CellState>();

	public void add(CellState cellState) {
		String key = cellState.x + ":" + cellState.y;
		maps.put(key, cellState);
	}
	
	public void update(CellState cellState) {
		remove(cellState);
		add(cellState);
	}

	public void remove(CellState cellState) {
		String key = cellState.x + ":" + cellState.y;
		maps.remove(key);
	}

	public Collection<CellState> getCellStates() {
		return maps.values();
	}

	public HashMap<String, CellState> getMap() {
		return maps;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((maps == null) ? 0 : maps.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CellStateMap other = (CellStateMap) obj;
		if (maps == null) {
			if (other.maps != null)
				return false;
		} else if (!maps.equals(other.maps))
			return false;
		return true;
	}

	public static CellStateMap deserializeCellStateMap(Text input) {
		if (input == null || input.getValue().isEmpty())
			return null;

		String[] cells = input.getValue().split(";");
		CellStateMap cellStates = new CellStateMap();
		for (String cell : cells) {
			String[] properties = cell.split(":");
			if (properties.length != 3)
				throw new IllegalArgumentException(input.toString());
			CellState cellState = new CellState(
					Integer.parseInt(properties[0]),
					Integer.parseInt(properties[1]), properties[2]);
			cellStates.add(cellState);
		}
		return cellStates;
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		for (CellState s : maps.values())
			output.append(s);
		return output.toString();
	}

}
