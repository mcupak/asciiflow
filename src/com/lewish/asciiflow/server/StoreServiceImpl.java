package com.lewish.asciiflow.server;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import javax.jdo.JDOHelper;
import javax.jdo.PersistenceManager;
import javax.jdo.PersistenceManagerFactory;
import javax.jdo.Query;

import org.datanucleus.store.appengine.query.JDOCursorHelper;

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.Text;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.lewish.asciiflow.client.StoreService;
import com.lewish.asciiflow.shared.AccessException;
import com.lewish.asciiflow.shared.BatchStoreQueryResult;
import com.lewish.asciiflow.shared.CellState;
import com.lewish.asciiflow.shared.CellStateMap;
import com.lewish.asciiflow.shared.Compressor;
import com.lewish.asciiflow.shared.State;
import com.lewish.asciiflow.shared.Compressor.Callback;

/**
 * Provides server side bridge between client and datastore for simple object
 * retrieval. Also provides the basic authentication mechanism for accessing
 * drawings.
 * 
 * @author lewis
 */
public class StoreServiceImpl extends RemoteServiceServlet implements
		StoreService {

	private static final long serialVersionUID = -3286308257185371845L;

	// needed for state construction from log
	private final Compressor compressor = new ServerCompressor();

	private Random random = new Random();
	private final static PersistenceManagerFactory managerFactory = JDOHelper
			.getPersistenceManagerFactory("transactions-optional");
	private static final List<Entry<String, CellStateMap>> operations = new LinkedList<Entry<String, CellStateMap>>();

	private CellStateMap findLatestCellStateMap(final CellStateMap c) {
		String client = this.getThreadLocalRequest().getSession().getId();

		CellStateMap mask = new CellStateMap();
		ListIterator<Entry<String, CellStateMap>> li = operations
				.listIterator(operations.size());
		// Iterate in reverse.
		while (li.hasPrevious()) {
			Entry<String, CellStateMap> entry = li.previous();
			for (CellState s : entry.getValue().getCellStates()) {
				// map, don't update with the latest value, equals when the
				// coordinates equal
				if ((!mask.getCellStates().contains(s))
						&& c.getCellStates().contains(s)) {
					mask.add((client.equals(entry.getKey())) ? (new CellState(
							s.x, s.y, null)) : s);
				}
				if (mask.getCellStates().size() >= c.getCellStates().size())
					return mask;
			}
		}

		return mask;
	}

	@Override
	public State saveState(State state) throws AccessException {

		if (!state.hasId()) {
			// TODO Check collisions or do some math.
			state.setId(generateId());
			state.setEditCode(generateEditCode());
		} else {
			State loadState = fetchState(state.getId());
			if (!loadState.getEditCode().equals(state.getEditCode())) {
				throw new AccessException(state);
			}
		}
		
		// construction of the state from the log of operations
		// TODO: far from ideal, but does the trick - should be changed
		final PersistenceManager pm = managerFactory.getPersistenceManager();
		if (state.isCompressed()) {
			final State uncompressed = state;
			final String clientId = this.getThreadLocalRequest().getSession()
					.getId();
			compressor.uncompress(state, new Callback() {

				@Override
				public void onFinish(boolean success) {
					AbstractMap.SimpleEntry<String, CellStateMap> entry = new AbstractMap.SimpleEntry<String, CellStateMap>(
							clientId, CellStateMap
									.deserializeCellStateMap(uncompressed
											.getOperation()));
					operations.add(entry);
					for (CellState s:entry.getValue().getCellStates()) {
						uncompressed.getCellStateMap().update(s);
					}

					compressor.compress(uncompressed, new Callback() {

						@Override
						public void onFinish(boolean success) {
							try {
								pm.makePersistent(uncompressed);
							} finally {
								pm.close();
							}
						}
					});
				}
			});
			return uncompressed;
		} else {
			return null;
		}
	}

	private Long generateId() {
		Long id = 0l;
		while (id <= 0) {
			id = random.nextLong();
		}
		return id;
	}

	private Integer generateEditCode() {
		Integer code = 0;
		while (code <= 0) {
			code = random.nextInt();
		}
		return code;
	}

	@Override
	public State loadState(Long id, Integer editCode, String operation) {
		State state = fetchState(id);

		// Do not return the edit code unless it is valid.
		if (!editCode.equals(state.getEditCode())) {
			state.setEditCode(0);
		}

		// update operation in case of undo, return mask
		if (!(operation == null || operation.isEmpty())) {
			state.setOperation(new Text(findLatestCellStateMap(
					CellStateMap.deserializeCellStateMap(new Text(operation
							.toString()))).toString()));
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
		return new BatchStoreQueryResult(new ArrayList<State>(cleanStates),
				newCursorString);
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
		if (!state.hasId()) {
			// TODO Check collisions or do some math.
			state.setId(generateId());
			state.setEditCode(generateEditCode());
		} else {
			State loadState = fetchState(state.getId());
			if (!loadState.getEditCode().equals(state.getEditCode())) {
				throw new AccessException(state);
			}
			if (state.getOwner().equals(loadState.getOwner())) {
				return a;
			} else {
				Integer b = 1;
				return b;
			}
		}
		return a;
	}
}
