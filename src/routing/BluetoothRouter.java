/*
 * Copyright 2010 Aalto University, ComNet
 * Released under GPLv3. See LICENSE.txt for details.
 */
package routing;

import core.*;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class BluetoothRouter extends ActiveRouter {

	public static final String GATEWAY_NAME = "gateway";
	public static final Set<String> SHARED_DELIVERIES = new HashSet<>();

	private boolean enabled;

	public BluetoothRouter(Settings s) {
		super(s);
		this.enabled = true;
	}

	protected BluetoothRouter(BluetoothRouter r) {
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
	public BluetoothRouter replicate() {
		return new BluetoothRouter(this);
	}

	@Override
	public boolean createNewMessage(Message m) {
		if (enabled) {
			if (this.getFreeBufferSize() <= 0) {
				this.messages.remove(this.messages.entrySet().stream().min(Comparator.comparingDouble(m1 -> m1.getValue().getCreationTime())).get().getValue().getId());
			}
			return super.createNewMessage(m);
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

		isFinalRecipient = getHost().getGroupId().startsWith(GATEWAY_NAME);
		isFirstDelivery = isFinalRecipient && !SHARED_DELIVERIES.contains(id);

		if (!isFinalRecipient) {
			addToMessages(incoming, false);
		} else if (isFirstDelivery) {
			//this.deliveredMessages.put(id, incoming);
			SHARED_DELIVERIES.add(id);
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
