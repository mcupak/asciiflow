package com.lewish.asciiflow.client;

/**
 * Class determining whether a client operation should result into save on the
 * server side.
 * 
 * @author mcupak
 * 
 */
public class SaveManager {
/*
	public static void saveCanvas(Canvas canvas) {
		canvas.getStoreModel().save();
	}
*/
	public static void checkSave(Canvas canvas, HistoryManager historyManager) {
		canvas.getStoreModel().check(historyManager);
	}

}
