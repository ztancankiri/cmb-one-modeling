/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

/**
 * Epidemic message router with drop-oldest buffer and only single transferring
 * connections at a time.
 */
public class BlueRouter extends ActiveRouter {

	private static final String ROUTER_NAME = "router";

	/**
	 * Constructor. Creates a new message router based on the settings in
	 * the given Settings object.
	 * @param s The settings object
	 */
	public BlueRouter(Settings s) {
		super(s);
		//TODO: read&use epidemic router specific settings (if any)
	}

	/**
	 * Copy constructor.
	 * @param r The router prototype where setting values are copied from
	 */
	protected BlueRouter(BlueRouter r) {
		super(r);
		//TODO: copy epidemic settings here (if any)
	}

	@Override
	public void update() {
		super.update();
		if (isTransferring() || !canStartTransfer()) {
			return; // transferring, don't try other connections yet
		}

		// Try first the messages that can be delivered to final recipient
		if (exchangeDeliverableMessages() != null) {
			return; // started a transfer, don't try others (yet)
		}

		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}

	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message m = super.messageTransferred(id, from);

		/**
		 *  N.B. With application support the following if-block
		 *  becomes obsolete, and the response size should be configured
		 *  to zero.
		 */
		// check if msg was for this host and a response was requested
//		if (m.getTo() == getHost() && m.getResponseSize() > 0) {
//			// generate a response message
//			Message res = new Message(this.getHost(),m.getFrom(),
//					RESPONSE_PREFIX+m.getId(), m.getResponseSize());
//			this.createNewMessage(res);
//			this.getMessage(RESPONSE_PREFIX+m.getId()).setRequest(m);
//		}
		if (getHost().getGroupId().equals(ROUTER_NAME) && m.getResponseSize() > 0){
			Message res = new Message(this.getHost(),m.getFrom(),
					RESPONSE_PREFIX+m.getId(), m.getResponseSize());
			this.createNewMessage(res);
			this.getMessage(RESPONSE_PREFIX+m.getId()).setRequest(m);
		}
		return m;
	}

	@Override
	public BlueRouter replicate() {
		return new BlueRouter(this);
	}

}
