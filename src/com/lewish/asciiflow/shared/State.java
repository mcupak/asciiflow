package com.lewish.asciiflow.shared;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import com.google.appengine.api.datastore.Blob;

/**
 * This is used as both the JDO object stored in the datastore and the DTO for GWT RPCs.
 * Represents a diagram.
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
}
