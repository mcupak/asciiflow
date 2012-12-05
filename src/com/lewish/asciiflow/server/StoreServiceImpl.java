package com.lewish.asciiflow.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.datanucleus.store.appengine.query.JDOCursorHelper;

import com.google.appengine.api.datastore.Cursor;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.lewish.asciiflow.client.StoreService;
import com.lewish.asciiflow.shared.AccessException;
import com.lewish.asciiflow.shared.BatchStoreQueryResult;
import com.lewish.asciiflow.shared.State;

/**
 * Provides server side bridge between client and datastore for simple object retrieval.
 * Also provides the basic authentication mechanism for accessing drawings.
 * 
 * @author lewis
 */
public class StoreServiceImpl extends RemoteServiceServlet implements StoreService {

	private static final long serialVersionUID = -3286308257185371845L;

	private Random random = new Random();
	private final static PersistenceManagerFactory managerFactory = JDOHelper
	.getPersistenceManagerFactory("transactions-optional");

	@Override
	public State saveState(State state) throws AccessException {

		if(!state.hasId()) {
			//TODO Check collisions or do some math.
			state.setId(generateId());
			state.setEditCode(generateEditCode());
		} else {
			State loadState = fetchState(state.getId());
			if(!loadState.getEditCode().equals(state.getEditCode())) {
				throw new AccessException(state);
			}
		}
		PersistenceManager pm = managerFactory.getPersistenceManager();
		if (state.isCompressed()) {
			try {
				state = pm.makePersistent(state);
				return state;
			} finally {
				pm.close();
			}
		} else {
			return null;
		}
	}

	private Long generateId() {
		Long id = 0l;
		while(id <= 0) {
			id = random.nextLong();
		}
		return id;
	}

	private Integer generateEditCode() {
		Integer code = 0;
		while(code <= 0) {
			code = random.nextInt();
		}
		return code;
	}

	@Override
	public State loadState(Long id, Integer editCode) {
		State state = fetchState(id);
		//Do not return the edit code unless it is valid.
		if (!editCode.equals(state.getEditCode())) {
			state.setEditCode(0);
		}
		return state;
	}

	/**
	 * Uses tokenized pagination to fetch blocks of 10 objects.
	 */
	@Override
	public BatchStoreQueryResult loadTenStates(String cursorString) {
		PersistenceManager pm = managerFactory.getPersistenceManager();
		Query query = pm.newQuery(State.class, "isPublic == true");
		query.setRange(0, 10);
		if (cursorString != null) {
			Cursor cursor = Cursor.fromWebSafeString(cursorString);
			Map<String, Object> extensionMap = new HashMap<String, Object>();
			extensionMap.put(JDOCursorHelper.CURSOR_EXTENSION, cursor);
			query.setExtensions(extensionMap);
		}

		String newCursorString;
		ArrayList<State> cleanStates;
		try {
			@SuppressWarnings("unchecked")
			List<State> states = (List<State>) query.execute();
			Cursor cursor = JDOCursorHelper.getCursor(states);
			newCursorString = cursor.toWebSafeString();
			cleanStates = new ArrayList<State>(pm.detachCopyAll(states));
		} finally {
			pm.close();
		}
		return new BatchStoreQueryResult(new ArrayList<State>(cleanStates), newCursorString);
	}

	private State fetchState(Long id) {
		PersistenceManager pm = managerFactory.getPersistenceManager();
		State state;
		try {
			state = pm.getObjectById(State.class, id);
		} catch (Exception e) {
			return null;
		} finally {
			pm.close();
		}
		return state;
	}

	@Override
	public Integer checkState(State state) throws AccessException {
		Integer a = 0;
		if(!state.hasId()) {
			//TODO Check collisions or do some math.
			state.setId(generateId());
			state.setEditCode(generateEditCode());
		} else {
			State loadState = fetchState(state.getId());
			if(!loadState.getEditCode().equals(state.getEditCode())) {
				throw new AccessException(state);
			}
			if (state.getOwner().equals(loadState.getOwner())) {
				return a;
			}
			else {
				Integer b = 1;
				return b;
			}
		}
		return a;
	}
}
