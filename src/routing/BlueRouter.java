/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

public class BlueRouter extends ActiveRouter {

	private static final String ROUTER_NAME = "router";

	private boolean enabled;

	public BlueRouter(Settings s) {
		super(s);
		this.enabled = false;
	}

	protected BlueRouter(BlueRouter r) {
		super(r);
		this.enabled = r.enabled;
	}

	@Override
	public void update() {
		super.update();
//		if (isTransferring() || !canStartTransfer()) {
//			return; // transferring, don't try other connections yet
//		}
//
//		// Try first the messages that can be delivered to final recipient
//		if (exchangeDeliverableMessages() != null) {
//			return; // started a transfer, don't try others (yet)
//		}

		// then try any/all message to any/all connection
		this.tryAllMessagesToAllConnections();
	}

	@Override
	public BlueRouter replicate() {
		return new BlueRouter(this);
	}

	@Override
	public boolean createNewMessage(Message m) {
		if (enabled) {
			if (this.getFreeBufferSize() > 0) {
				return super.createNewMessage(m);
			}
			return false;
		}

		return false;
	}

	@Override
	public Message messageTransferred(String id, DTNHost from) {
		Message incoming = removeFromIncomingBuffer(id, from);
		boolean isFinalRecipient;
		boolean isFirstDelivery;

		if (incoming == null) {
			throw new SimError("No message with ID " + id + " in the incoming " + "buffer of " + this.host);
		}

		incoming.setReceiveTime(SimClock.getTime());

		isFinalRecipient = getHost().getGroupId().startsWith(ROUTER_NAME);
		isFirstDelivery = isFinalRecipient && !isDeliveredMessage(incoming);

		if (!isFinalRecipient) {
			addToMessages(incoming, false);
		} else if (isFirstDelivery) {
			this.deliveredMessages.put(id, incoming);
		}

		if (isFinalRecipient) {
			from.getRouter().messages.remove(id);
		}

		for (MessageListener ml : this.mListeners) {
			ml.messageTransferred(incoming, from, this.host, isFirstDelivery);
		}

		return incoming;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
}
