/*
 * Copyright (c) 2014 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.vtn.manager.internal.cluster;

import org.slf4j.Logger;

import org.opendaylight.vtn.manager.internal.VTNManagerImpl;

import org.opendaylight.controller.sal.core.UpdateType;

/**
 * {@code ContainerPathMapEvent} describes an cluster event object which
 * notifies that a container path map was added, changed, or removed.
 *
 * <p>
 *   Although this class is public to other packages, this class does not
 *   provide any API. Applications other than VTN Manager must not use this
 *   class.
 * </p>
 */
public final class ContainerPathMapEvent extends ClusterEvent {
    /**
     * Version number for serialization.
     */
    private static final long serialVersionUID = -835866538098603364L;

    /**
     * The index number of the container path map.
     */
    private final int  index;

    /**
     * Update type of this event.
     */
    private final UpdateType  updateType;

    /**
     * Generate a container path map event which indicates that the container
     * path map was added, removed, or changed.
     *
     * @param mgr   VTN Manager service.
     * @param idx   The index of the container path map.
     * @param type  Update type.
     */
    public static void raise(VTNManagerImpl mgr, int idx, UpdateType type) {
        mgr.enqueueEvent(new ContainerPathMapEvent(idx, type));
    }

    /**
     * Construct a new container path map event.
     *
     * @param idx   The index of the container path map.
     * @param type  Update type.
     */
    private ContainerPathMapEvent(int idx, UpdateType type) {
        index = idx;
        updateType = type;
    }

    /**
     * Return the index of the container path map.
     *
     * @return  The index of the container path map.
     */
    public int getIndex() {
        return index;
    }

    /**
     * Return update type of this event.
     *
     * @return  Update type.
     */
    public UpdateType getUpdateType() {
        return updateType;
    }

    /**
     * Invoked when a cluster event has been receive.
     *
     * @param mgr    VTN Manager service.
     * @param local  {@code true} if this event is generated by the local node.
     */
    @Override
    protected void eventReceived(VTNManagerImpl mgr, boolean local) {
        if (!local) {
            mgr.updateContainerPathMap(index, updateType);
        }
    }

    /**
     * Record a trace log which indicates that a cluster event has been
     * received from remote node.
     *
     * @param mgr     VTN Manager service.
     * @param logger  A logger instance.
     * @param key     A cluster event key associated with this event.
     */
    @Override
    public void traceLog(VTNManagerImpl mgr, Logger logger,
                         ClusterEventId key) {
        logger.trace("{}:{}: Received container path map event: " +
                     "index={}, type={}",
                     mgr.getContainerName(), key, index, updateType);
    }

    /**
     * Determine whether this event should be delivered on the VTN task thread
     * or not.
     *
     * @param local  {@code true} if this event is generated by the local node.
     *               {@code false} if this event is generated by remote cluster
     *               node.
     * @return  {@code true} is returned if this event should be delivered
     *          on the VTN task thread. Otherwise {@code false} is returned.
     */
    @Override
    public boolean isSingleThreaded(boolean local) {
        return true;
    }
}
