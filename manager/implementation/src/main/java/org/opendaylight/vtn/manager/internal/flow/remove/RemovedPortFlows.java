/*
 * Copyright (c) 2015, 2016 NEC Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.vtn.manager.internal.flow.remove;

import java.util.List;

import org.opendaylight.vtn.manager.VTNException;

import org.opendaylight.vtn.manager.internal.TxContext;
import org.opendaylight.vtn.manager.internal.util.flow.FlowCache;
import org.opendaylight.vtn.manager.internal.util.flow.FlowUtils;
import org.opendaylight.vtn.manager.internal.util.flow.RemoveFlowRpcList;
import org.opendaylight.vtn.manager.internal.util.inventory.InventoryReader;
import org.opendaylight.vtn.manager.internal.util.inventory.SalPort;

import org.opendaylight.yang.gen.v1.urn.opendaylight.flow.service.rev130819.SalFlowService;

/**
 * {@link RemovedPortFlows} describes flow entries removed by
 * {@link PortFlowRemover}.
 */
public final class RemovedPortFlows extends RemovedDataFlows<PortFlowRemover> {
    /**
     * Construct a new instance.
     *
     * @param ctx      The MD-SAL datastore transaction context.
     * @param remover  A {@link PortFlowRemover} instance that removed data
     *                 flows.
     */
    public RemovedPortFlows(TxContext ctx, PortFlowRemover remover) {
        super(ctx, remover);
    }

    // RemovedDataFlows

    /**
     * {@inheritDoc}
     */
    @Override
    protected RemoveFlowRpcList removeFlowEntries(
        SalFlowService sfs, List<FlowCache> flows, InventoryReader reader)
        throws VTNException {
        SalPort target = getFlowRemover().getFlowPort();
        return FlowUtils.removeFlowEntries(
            getProvider(), sfs, flows, target, reader);
    }
}
