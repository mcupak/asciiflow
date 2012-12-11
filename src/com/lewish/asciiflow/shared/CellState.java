package com.lewish.asciiflow.shared;

import java.io.Serializable;

public class CellState implements Serializable, Comparable<CellState> {

	private static final long serialVersionUID = 4899352777895247892L;
	public int x;
	public int y;
	public String value;

	public CellState(int x, int y, String value) {
		this.x = x;
		this.y = y;
		this.value = value;
	}

	public static CellState fromString(String string) {
		String[] split = string.split(":", 3);
		return new CellState(Integer.parseInt(split[0]),
				Integer.parseInt(split[1]), split[2]);
	}

	@Override
	public int compareTo(CellState cellState) {
		return this.y < cellState.y ? -1 : this.y > cellState.y ? 1
				: this.x < cellState.x ? -1 : this.x > cellState.x ? 1 : 0;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + x;
		result = prime * result + y;
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
		CellState other = (CellState) obj;
		if (x != other.x)
			return false;
		if (y != other.y)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return x + ":" + y + ":" + value + ";";
	}

}
