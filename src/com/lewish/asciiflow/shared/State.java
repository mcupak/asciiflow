package com.lewish.asciiflow.shared;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.Text;

/**
 * This is used as both the JDO object stored in the datastore and the DTO for
 * GWT RPCs. Represents a diagram.
 * 
 * @author lewis
 */
@PersistenceCapable
public class State implements Serializable {

	private static final long serialVersionUID = 8847057226414076746L;

	private transient CellStateMap cellStates = new CellStateMap();

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;

	@Persistent
	private Blob compressedBlob;

	@Persistent
	private String title = "Untitled";

	@Persistent
	private Integer editCode = 0;

	@Persistent
	private Boolean isPublic = false;

	@Persistent
	private Integer owner = 0;

	@Persistent
	private Integer canvasWidth = 0;
	@Persistent
	private Integer canvasHeight = 0;

	@Persistent
	private Text operation;

	public Long getId() {
		return id;
	}

	public boolean isCompressed() {
		return compressedBlob != null;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public boolean hasId() {
		return id != null && id != 0l;
	}

	public void setEditCode(Integer editCode) {
		this.editCode = editCode;
	}

	public Integer getEditCode() {
		return editCode;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getTitle() {
		return title;
	}

	public boolean isEditable() {
		return !hasId() || (editCode != null && editCode != 0);
	}

	public CellStateMap getCellStateMap() {
		return cellStates;
	}

	public void setCellStateMap(CellStateMap map) {
		cellStates = map;
	}

	public void setPublic(Boolean isPublic) {
		this.isPublic = isPublic;
	}

	public Boolean isPublic() {
		return isPublic;
	}

	public void setCompressedState(byte[] compressedState) {
		this.compressedBlob = new Blob(compressedState);
	}

	public byte[] getCompressedState() {
		return compressedBlob.getBytes();
	}

	public void setOwner(int owner) {
		this.owner = owner;
	}

	public Integer getOwner() {
		return owner;
	}

	public Text getOperation() {
		return operation;
	}

	public void setOperation(Text operation) {
		this.operation = operation;
	}

	public Integer getCanvasWidth() {
		return canvasWidth;
	}

	public void setCanvasWidth(Integer canvasWidth) {
		this.canvasWidth = canvasWidth;
	}

	public Integer getCanvasHeight() {
		return canvasHeight;
	}

	public void setCanvasHeight(Integer canvasHeight) {
		this.canvasHeight = canvasHeight;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((canvasHeight == null) ? 0 : canvasHeight.hashCode());
		result = prime * result
				+ ((canvasWidth == null) ? 0 : canvasWidth.hashCode());
		result = prime * result
				+ ((compressedBlob == null) ? 0 : compressedBlob.hashCode());
		result = prime * result
				+ ((editCode == null) ? 0 : editCode.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((isPublic == null) ? 0 : isPublic.hashCode());
		result = prime * result
				+ ((operation == null) ? 0 : operation.hashCode());
		result = prime * result + ((owner == null) ? 0 : owner.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
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
		State other = (State) obj;
		if (canvasHeight == null) {
			if (other.canvasHeight != null)
				return false;
		} else if (!canvasHeight.equals(other.canvasHeight))
			return false;
		if (canvasWidth == null) {
			if (other.canvasWidth != null)
				return false;
		} else if (!canvasWidth.equals(other.canvasWidth))
			return false;
		if (compressedBlob == null) {
			if (other.compressedBlob != null)
				return false;
		} else if (!compressedBlob.equals(other.compressedBlob))
			return false;
		if (editCode == null) {
			if (other.editCode != null)
				return false;
		} else if (!editCode.equals(other.editCode))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (isPublic == null) {
			if (other.isPublic != null)
				return false;
		} else if (!isPublic.equals(other.isPublic))
			return false;
		if (operation == null) {
			if (other.operation != null)
				return false;
		} else if (!operation.equals(other.operation))
			return false;
		if (owner == null) {
			if (other.owner != null)
				return false;
		} else if (!owner.equals(other.owner))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "State [id=" + id + ", compressedBlob=" + compressedBlob
				+ ", title=" + title + ", editCode=" + editCode + ", isPublic="
				+ isPublic + ", owner=" + owner + ", canvasWidth="
				+ canvasWidth + ", canvasHeight=" + canvasHeight
				+ ", operation=" + operation + "]";
	}

}
