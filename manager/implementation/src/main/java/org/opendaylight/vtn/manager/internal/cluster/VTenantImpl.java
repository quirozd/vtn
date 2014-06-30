/*
 * Copyright (c) 2013-2014 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.vtn.manager.internal.cluster;

import java.io.Serializable;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.opendaylight.vtn.manager.DataLinkHost;
import org.opendaylight.vtn.manager.IVTNManagerAware;
import org.opendaylight.vtn.manager.MacAddressEntry;
import org.opendaylight.vtn.manager.MacMap;
import org.opendaylight.vtn.manager.MacMapAclType;
import org.opendaylight.vtn.manager.MacMapConfig;
import org.opendaylight.vtn.manager.PathMap;
import org.opendaylight.vtn.manager.PortMap;
import org.opendaylight.vtn.manager.PortMapConfig;
import org.opendaylight.vtn.manager.UpdateOperation;
import org.opendaylight.vtn.manager.VBridge;
import org.opendaylight.vtn.manager.VBridgeConfig;
import org.opendaylight.vtn.manager.VBridgeIfPath;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VInterface;
import org.opendaylight.vtn.manager.VInterfaceConfig;
import org.opendaylight.vtn.manager.VNodeState;
import org.opendaylight.vtn.manager.VTNException;
import org.opendaylight.vtn.manager.VTenant;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.vtn.manager.VlanMap;
import org.opendaylight.vtn.manager.VlanMapConfig;
import org.opendaylight.vtn.manager.internal.ContainerConfig;
import org.opendaylight.vtn.manager.internal.EdgeUpdateState;
import org.opendaylight.vtn.manager.internal.IVTNResourceManager;
import org.opendaylight.vtn.manager.internal.MacAddressTable;
import org.opendaylight.vtn.manager.internal.MiscUtils;
import org.opendaylight.vtn.manager.internal.PacketContext;
import org.opendaylight.vtn.manager.internal.PortFilter;
import org.opendaylight.vtn.manager.internal.RouteResolver;
import org.opendaylight.vtn.manager.internal.VTNFlowDatabase;
import org.opendaylight.vtn.manager.internal.VTNManagerImpl;
import org.opendaylight.vtn.manager.internal.VTNThreadData;

import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.core.UpdateType;
import org.opendaylight.controller.sal.packet.PacketResult;
import org.opendaylight.controller.sal.packet.address.DataLinkAddress;
import org.opendaylight.controller.sal.utils.Status;
import org.opendaylight.controller.sal.utils.StatusCode;

/**
 * Implementation of virtual tenant.
 *
 * <p>
 *   Although this class is public to other packages, this class does not
 *   provide any API. Applications other than VTN Manager must not use this
 *   class.
 * </p>
 */
public final class VTenantImpl implements Serializable {
    /**
     * Version number for serialization.
     */
    private static final long serialVersionUID = -5029932535173882419L;

    /**
     * Logger instance.
     */
    private static final Logger  LOG =
        LoggerFactory.getLogger(VTenantImpl.class);

    /**
     * Maximum value of flow timeout value.
     */
    private static final int  MAX_FLOW_TIMEOUT = 65535;

    /**
     * Default value of {@code idle_timeout} of flow entries.
     */
    private static final int  DEFAULT_IDLE_TIMEOUT = 300;

    /**
     * Default value of {@code hard_timeout} of flow entries.
     */
    private static final int  DEFAULT_HARD_TIMEOUT = 0;

    /**
     * The name of the container to which this tenant belongs.
     */
    private final String  containerName;

    /**
     * Tenant name.
     */
    private final String  tenantName;

    /**
     * Configuration for the tenant.
     */
    private VTenantConfig  tenantConfig;

    /**
     * Virtual layer 2 bridges.
     */
    private transient Map<String, VBridgeImpl> vBridges =
        new TreeMap<String, VBridgeImpl>();

    /**
     * VTN path maps configured in this tenant.
     */
    private transient Map<Integer, VTenantPathMapImpl> pathMaps =
        new TreeMap<Integer, VTenantPathMapImpl>();

    /**
     * Read write lock to synchronize per-tenant resources.
     */
    private transient ReentrantReadWriteLock  rwLock =
        new ReentrantReadWriteLock();

    /**
     * Construct a virtual tenant instance.
     *
     * @param containerName  The name of the container to which the tenant
     *                       belongs.
     * @param tenantName     The name of the tenant.
     * @param tconf          Configuration for the tenant.
     * @throws VTNException  An error occurred.
     */
    public VTenantImpl(String containerName, String tenantName,
                       VTenantConfig tconf) throws VTNException {
        VTenantConfig cf = resolve(tconf);
        checkConfig(cf);
        this.containerName = containerName;
        this.tenantName = tenantName;
        this.tenantConfig = cf;
    }

    /**
     * Return the name of the container to which the tenant belongs.
     *
     * @return  The name of the container.
     */
    public String getContainerName() {
        return containerName;
    }

    /**
     * Return the name of the tenant.
     *
     * @return  The name of the tenant.
     */
    public String getName() {
        return tenantName;
    }

    /**
     * Return information about the tenant.
     *
     * @return  Information about the tenant.
     */
    public VTenant getVTenant() {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            return new VTenant(tenantName, tenantConfig);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return tenant configuration.
     *
     * @return  Configuration for the tenant.
     */
    public VTenantConfig getVTenantConfig() {
        // Return without holding any lock.
        return tenantConfig;
    }

    /**
     * Set tenant configuration.
     *
     * @param mgr    VTN Manager service.
     * @param path   Path to the virtual tenant.
     * @param tconf  Tenant configuration
     * @param all    If {@code true} is specified, all attributes of the
     *               tenant are modified. In this case, {@code null} in
     *               {@code tconf} is interpreted as default value.
     *               If {@code false} is specified, an attribute is not
     *               modified if its value in {@code tconf} is {@code null}.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status setVTenantConfig(VTNManagerImpl mgr, VTenantPath path,
                                   VTenantConfig tconf, boolean all)
        throws VTNException {
        VTenantConfig cf;
        Lock wrlock = rwLock.writeLock();
        wrlock.lock();
        try {
            if (all) {
                cf = resolve(tconf);
            } else {
                cf = merge(tconf);
            }
            if (cf.equals(tenantConfig)) {
                return new Status(StatusCode.SUCCESS, "Not modified");
            }

            checkConfig(cf);
            tenantConfig = cf;
            VTenant vtenant = new VTenant(tenantName, cf);
            mgr.enqueueEvent(path, vtenant, UpdateType.CHANGED);

            mgr.export(this);
            return saveConfigImpl(null);
        } finally {
            wrlock.unlock();
        }
    }

    /**
     * Add a new virtual L2 bridge to this tenant.
     *
     * @param mgr    VTN Manager service.
     * @param path   Path to the bridge.
     * @param bconf  Bridge configuration.
     * @throws VTNException  An error occurred.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     */
    public Status addBridge(VTNManagerImpl mgr, VBridgePath path,
                          VBridgeConfig bconf) throws VTNException {
        // Ensure the given bridge name is valid.
        String bridgeName = path.getBridgeName();
        MiscUtils.checkName("Bridge", bridgeName);

        if (bconf == null) {
            Status status = MiscUtils.argumentIsNull("Bridge configuration");
            throw new VTNException(status);
        }

        VBridgeImpl vbr = new VBridgeImpl(this, bridgeName, bconf);
        Lock wrlock = rwLock.writeLock();
        wrlock.lock();
        try {
            VBridgeImpl old = vBridges.put(bridgeName, vbr);
            if (old != null) {
                vBridges.put(bridgeName, old);
                String msg = bridgeName + ": Bridge name already exists";
                throw new VTNException(StatusCode.CONFLICT, msg);
            }

            VBridge vbridge = vbr.getVBridge(mgr);
            VBridgeEvent.added(mgr, path, vbridge);

            // Create a MAC address table for this bridge.
            vbr.initMacTableAging(mgr);

            mgr.export(this);
            return saveConfigImpl(null);
        } finally {
            wrlock.unlock();
        }
    }

    /**
     * Change configuration of existing virtual L2 bridge.
     *
     * @param mgr    VTN Manager service.
     * @param path   Path to the virtual bridge.
     * @param bconf  Bridge configuration.
     * @param all    If {@code true} is specified, all attributes of the
     *               bridge are modified. In this case, {@code null} in
     *               {@code bconf} is interpreted as default value.
     *               If {@code false} is specified, an attribute is not
     *               modified if its value in {@code bconf} is {@code null}.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status modifyBridge(VTNManagerImpl mgr, VBridgePath path,
                               VBridgeConfig bconf, boolean all)
        throws VTNException {
        if (bconf == null) {
            Status status = MiscUtils.argumentIsNull("Bridge configuration");
            throw new VTNException(status);
        }

        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            if (vbr.setVBridgeConfig(mgr, bconf, all)) {
                mgr.export(this);
                return saveConfigImpl(null);
            }

            return new Status(StatusCode.SUCCESS, "Not modified");
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Remove the specified virtual L2 bridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the virtual bridge.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status removeBridge(VTNManagerImpl mgr, VBridgePath path)
        throws VTNException {
        Lock wrlock = rwLock.writeLock();
        wrlock.lock();
        try {
            String bridgeName = path.getBridgeName();
            if (bridgeName == null) {
                Status status = MiscUtils.argumentIsNull("Bridge name");
                throw new VTNException(status);
            }

            VBridgeImpl vbr = vBridges.remove(bridgeName);
            if (vbr == null) {
                Status status = bridgeNotFound(bridgeName);
                throw new VTNException(status);
            }

            vbr.destroy(mgr, true);

            mgr.export(this);
            return saveConfigImpl(null);
        } finally {
            wrlock.unlock();
        }
    }

    /**
     * Return a list of virtual bridge information.
     *
     * @param mgr  VTN Manager service.
     * @return  A list of vbridge information.
     */
    public List<VBridge> getBridges(VTNManagerImpl mgr) {
        ArrayList<VBridge> list = new ArrayList<VBridge>();
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            for (VBridgeImpl vbr: vBridges.values()) {
                list.add(vbr.getVBridge(mgr));
            }
            list.trimToSize();
        } finally {
            rdlock.unlock();
        }

        return list;
    }

    /**
     * Return the virtual L2 bridge information associated with the given name.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the bridge.
     * @return  The virtual L2 bridge information associated with the given
     *          name.
     * @throws VTNException  An error occurred.
     */
    public VBridge getBridge(VTNManagerImpl mgr, VBridgePath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getVBridge(mgr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Add a new virtual interface to the specified L2 bridge.
     *
     * @param mgr    VTN Manager service.
     * @param path   Path to the interface to be added.
     * @param iconf  Interface configuration.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status addBridgeInterface(VTNManagerImpl mgr, VBridgeIfPath path,
                                     VInterfaceConfig iconf)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            vbr.addInterface(mgr, path, iconf);

            mgr.export(this);
            return saveConfigImpl(null);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Change configuration of existing virtual bridge interface.
     *
     * @param mgr    VTN Manager service.
     * @param path   Path to the interface.
     * @param iconf  Interface configuration.
     * @param all    If {@code true} is specified, all attributes of the
     *               interface are modified. In this case, {@code null} in
     *               {@code iconf} is interpreted as default value.
     *               If {@code false} is specified, an attribute is not
     *               modified if its value in {@code iconf} is {@code null}.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status modifyBridgeInterface(VTNManagerImpl mgr, VBridgeIfPath path,
                                        VInterfaceConfig iconf, boolean all)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            if (vbr.modifyInterface(mgr, path, iconf, all)) {
                mgr.export(this);
                return saveConfigImpl(null);
            }

            return new Status(StatusCode.SUCCESS, "Not modified");
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Remove the specified virtual interface from the bridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the interface.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status removeBridgeInterface(VTNManagerImpl mgr, VBridgeIfPath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            vbr.removeInterface(mgr, path);

            mgr.export(this);
            return saveConfigImpl(null);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return a list of virtual interface information in the specified
     * virtual bridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the bridge.
     * @return  A list of bridge interface information.
     * @throws VTNException  An error occurred.
     */
    public List<VInterface> getBridgeInterfaces(VTNManagerImpl mgr,
                                                VBridgePath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getInterfaces(mgr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return information about the specified virtual bridge interface.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the interface.
     * @return  The virtual interface information associated with the given
     *          name.
     * @throws VTNException  An error occurred.
     */
    public VInterface getBridgeInterface(VTNManagerImpl mgr, VBridgeIfPath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getInterface(mgr, path);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Add a new VLAN mapping to to the specified L2 bridge.
     *
     * @param mgr     VTN Manager service.
     * @param path    Path to the bridge.
     * @param vlconf  VLAN mapping configuration.
     * @return  Information about the added VLAN mapping is returned.
     * @throws VTNException  An error occurred.
     */
    public VlanMap addVlanMap(VTNManagerImpl mgr, VBridgePath path,
                              VlanMapConfig vlconf) throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            VlanMap vlmap = vbr.addVlanMap(mgr, vlconf);

            mgr.export(this);
            Status status = saveConfigImpl(null);
            if (!status.isSuccess()) {
                throw new VTNException(status);
            }

            return vlmap;
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Remove the specified VLAN mapping from from the bridge.
     *
     * @param mgr    VTN Manager service.
     * @param path   Path to the bridge.
     * @param mapId  The identifier of the VLAN mapping.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status removeVlanMap(VTNManagerImpl mgr, VBridgePath path,
                                String mapId) throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            vbr.removeVlanMap(mgr, mapId);

            mgr.export(this);
            return saveConfigImpl(null);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return a list of virtual interface information in the specified
     * virtual bridge.
     *
     * @param path  Path to the bridge.
     * @return  A list of bridge interface information.
     * @throws VTNException  An error occurred.
     */
    public List<VlanMap> getVlanMaps(VBridgePath path) throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getVlanMaps();
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return information about the specified VLAN mapping in the virtual
     * bridge.
     *
     * @param path   Path to the bridge.
     * @param mapId  The identifier of the VLAN mapping.
     * @return  VLAN mapping information associated with the given ID.
     * @throws VTNException  An error occurred.
     */
    public VlanMap getVlanMap(VBridgePath path, String mapId)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getVlanMap(mapId);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return information about the VLAN mapping which matches the specified
     * VLAN mapping configuration in the specified virtual L2 bridge.
     *
     * @param path    Path to the bridge.
     * @param vlconf  VLAN mapping configuration.
     * @return  Information about the VLAN mapping which matches the specified
     *          VLAN mapping information.
     * @throws VTNException  An error occurred.
     */
    public VlanMap getVlanMap(VBridgePath path, VlanMapConfig vlconf)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getVlanMap(vlconf);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return the port mapping configuration applied to the specified virtual
     * bridge interface.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the bridge interface.
     * @return  Port mapping information.
     * @throws VTNException  An error occurred.
     */
    public PortMap getPortMap(VTNManagerImpl mgr, VBridgeIfPath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getPortMap(mgr, path);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Create or destroy mapping between the physical switch port and the
     * virtual bridge interface.
     *
     * @param mgr     VTN Manager service.
     * @param path    Path to the bridge interface.
     * @param pmconf  Port mapping configuration to be set.
     *                If {@code null} is specified, port mapping on the
     *                specified interface is destroyed.
     * @return  A {@link Status} instance which indicates the result of the
     *          operation.
     * @throws VTNException  An error occurred.
     */
    public Status setPortMap(VTNManagerImpl mgr, VBridgeIfPath path,
                             PortMapConfig pmconf) throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            vbr.setPortMap(mgr, path, pmconf);

            mgr.export(this);
            return saveConfigImpl(null);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return information about the MAC mapping configured in the specified
     * vBridge.
     *
     * @param mgr   VTN Manager service.
     * @param path   Path to the bridge.
     * @return  A {@link MacMap} object which represents information about
     *          the MAC mapping specified by {@code path}.
     *          {@code null} is returned if the MAC mapping is not configured
     *          in the specified vBridge.
     * @throws VTNException  An error occurred.
     */
    public MacMap getMacMap(VTNManagerImpl mgr, VBridgePath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getMacMap(mgr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return configuration information about MAC mapping in the specified
     * vBridge.
     *
     * @param path     Path to the vBridge.
     * @param aclType  The type of access control list.
     * @return  A set of {@link DataLinkHost} instances which contains host
     *          information in the specified access control list is returned.
     *          {@code null} is returned if MAC mapping is not configured in
     *          the specified vBridge.
     * @throws VTNException  An error occurred.
     */
    public Set<DataLinkHost> getMacMapConfig(VBridgePath path,
                                             MacMapAclType aclType)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getMacMapConfig(aclType);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return a list of {@link MacAddressEntry} instances corresponding to
     * all the MAC address information actually mapped by MAC mapping
     * configured in the specified vBridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the vBridge.
     * @return  A list of {@link MacAddressEntry} instances corresponding to
     *          all the MAC address information actually mapped to the vBridge
     *          specified by {@code path}.
     *          {@code null} is returned if MAC mapping is not configured
     *          in the specified vBridge.
     * @throws VTNException  An error occurred.
     */
    public List<MacAddressEntry> getMacMappedHosts(VTNManagerImpl mgr,
                                                   VBridgePath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getMacMappedHosts(mgr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Determine whether the host specified by the MAC address is actually
     * mapped by MAC mapping configured in the specified vBridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the vBridge.
     * @param addr  A {@link DataLinkAddress} instance which represents the
     *              MAC address.
     * @return  A {@link MacAddressEntry} instancw which represents information
     *          about the host corresponding to {@code addr} is returned
     *          if it is actually mapped to the specified vBridge by MAC
     *          mapping.
     *          {@code null} is returned if the MAC address specified by
     *          {@code addr} is not mapped by MAC mapping, or MAC mapping is
     *          not configured in the specified vBridge.
     * @throws VTNException  An error occurred.
     */
    public MacAddressEntry getMacMappedHost(VTNManagerImpl mgr,
                                            VBridgePath path,
                                            DataLinkAddress addr)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            return vbr.getMacMappedHost(mgr, addr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Change MAC mapping configuration as specified by {@link MacMapConfig}
     * instance.
     *
     * @param mgr     VTN Manager service.
     * @param path    A {@link VBridgePath} object that specifies the position
     *                of the vBridge.
     * @param op      A {@link UpdateOperation} instance which indicates
     *                how to change the MAC mapping configuration.
     * @param mcconf  A {@link MacMapConfig} instance which contains the MAC
     *                mapping configuration information.
     * @return        A {@link UpdateType} object which represents the result
     *                of the operation is returned.
     *                {@code null} is returned if the configuration was not
     *                changed.
     * @throws VTNException  An error occurred.
     */
    public UpdateType setMacMap(VTNManagerImpl mgr, VBridgePath path,
                                UpdateOperation op, MacMapConfig mcconf)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            UpdateType result = vbr.setMacMap(mgr, op, mcconf);
            if (result != null) {
                mgr.export(this);
                Status status = saveConfigImpl(null);
                if (!status.isSuccess()) {
                    throw new VTNException(status);
                }
            }

            return result;
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Change the access controll list for the specified MAC mapping.
     *
     * @param mgr       VTN Manager service.
     * @param path      A {@link VBridgePath} object that specifies the
     *                  position of the vBridge.
     * @param op        A {@link UpdateOperation} instance which indicates
     *                  how to change the MAC mapping configuration.
     * @param aclType   The type of access control list.
     * @param dlhosts   A set of {@link DataLinkHost} instances.
     * @return          A {@link UpdateType} object which represents the result
     *                  of the operation is returned.
     *                  {@code null} is returned if the configuration was not
     *                  changed.
     * @throws VTNException  An error occurred.
     */
    public UpdateType setMacMap(VTNManagerImpl mgr, VBridgePath path,
                                UpdateOperation op, MacMapAclType aclType,
                                Set<? extends DataLinkHost> dlhosts)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            UpdateType result = vbr.setMacMap(mgr, op, aclType, dlhosts);
            if (result != null) {
                mgr.export(this);
                Status status = saveConfigImpl(null);
                if (!status.isSuccess()) {
                    throw new VTNException(status);
                }
            }

            return result;
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return a list of MAC address entries learned by the specified virtual
     * L2 bridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the bridge.
     * @return  A list of MAC address entries.
     * @throws VTNException  An error occurred.
     */
    public List<MacAddressEntry> getMacEntries(VTNManagerImpl mgr,
                                               VBridgePath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            MacAddressTable table = getMacAddressTable(mgr, path);
            return table.getEntries();
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Search for a MAC address entry from the MAC address table in the
     * specified virtual L2 bridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the bridge.
     * @param addr  MAC address.
     * @return  A MAC address entry associated with the specified MAC address.
     *          {@code null} is returned if not found.
     * @throws VTNException  An error occurred.
     */
    public MacAddressEntry getMacEntry(VTNManagerImpl mgr, VBridgePath path,
                                       DataLinkAddress addr)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            MacAddressTable table = getMacAddressTable(mgr, path);
            return table.getEntry(addr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Remove a MAC address entry from the MAC address table in the virtual L2
     * bridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the bridge.
     * @param addr  MAC address.
     * @return  A MAC address entry actually removed is returned.
     *          {@code null} is returned if not found.
     * @throws VTNException  An error occurred.
     */
    public MacAddressEntry removeMacEntry(VTNManagerImpl mgr, VBridgePath path,
                                          DataLinkAddress addr)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            MacAddressTable table = getMacAddressTable(mgr, path);
            return table.removeEntry(addr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Flush all MAC address table entries in the specified virtual L2 bridge.
     *
     * @param mgr   VTN Manager service.
     * @param path  Path to the bridge.
     * @throws VTNException  An error occurred.
     */
    public void flushMacEntries(VTNManagerImpl mgr, VBridgePath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            MacAddressTable table = getMacAddressTable(mgr, path);
            table.flush();
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Save tenant configuration to the configuration file.
     *
     * @param mgr  VTN Manager service.
     *             If a non-{@code null} value is specified, this method checks
     *             whether the current configuration is applied correctly.
     * @return  "Success" or failure reason.
     */
    public Status saveConfig(VTNManagerImpl mgr) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            return saveConfigImpl(mgr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Resume the virtual tenant.
     *
     * <p>
     *   This method is called just after this tenant is instantiated from
     *   the configuration file.
     * </p>
     *
     * @param mgr  VTN Manager service.
     */
    public void resume(VTNManagerImpl mgr) {
        Lock wrlock = rwLock.writeLock();
        wrlock.lock();
        try {
            // Create flow database for this tenant.
            mgr.createTenantFlowDB(tenantName);

            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.resume(mgr);
            }
        } finally {
            wrlock.unlock();
        }

        LOG.trace("{}:{}: Tenant was resumed", containerName, tenantName);
    }

    /**
     * Invoked when a node is added, removed, or changed.
     *
     * @param mgr   VTN Manager service.
     * @param node  Node being updated.
     * @param type  Type of update.
     */
    public void notifyNode(VTNManagerImpl mgr, Node node, UpdateType type) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.notifyNode(mgr, node, type);
            }
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * This method is called when some properties of a node connector are
     * added/deleted/changed.
     *
     * @param mgr     VTN Manager service.
     * @param nc      Node connector being updated.
     * @param pstate  The state of the node connector.
     * @param type    Type of update.
     */
    public void notifyNodeConnector(VTNManagerImpl mgr, NodeConnector nc,
                                    VNodeState pstate, UpdateType type) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.notifyNodeConnector(mgr, nc, pstate, type);
            }
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * This method is called when topology graph is changed.
     *
     * @param mgr     VTN Manager service.
     * @param estate  A {@link EdgeUpdateState} instance which contains
     *                information reported by the controller.
     */
    public void edgeUpdate(VTNManagerImpl mgr, EdgeUpdateState estate) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.edgeUpdate(mgr, estate);
            }
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Notify the listener of current configuration.
     *
     * @param mgr       VTN Manager service.
     * @param listener  VTN manager listener service.
     */
    public void notifyConfiguration(VTNManagerImpl mgr,
                                    IVTNManagerAware listener) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.notifyConfiguration(mgr, listener);
            }
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Initiate the discovery of a host base on its IP address.
     *
     * @param mgr   VTN manager service.
     * @param pctx  The context of the ARP packet to send.
     * @param path  Path to the target bridge.
     * @throws VTNException  An error occurred.
     */
    public void findHost(VTNManagerImpl mgr, PacketContext pctx,
                         VBridgePath path) throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            vbr.findHost(mgr, pctx);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Initiate the discovery of a host base on its IP address.
     *
     * @param mgr   VTN manager service.
     * @param pctx  The context of the ARP packet to send.
     */
    public void findHost(VTNManagerImpl mgr, PacketContext pctx) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.findHost(mgr, pctx);
            }
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Send a unicast ARP request to the specified host.
     *
     * @param mgr   VTN manager service.
     * @param ref   Reference to the virtual network mapping.
     *              A virtual node pointed by {@code ref} must be contained
     *              in this tenant.
     * @param pctx  The context of the ARP packet to send.
     * @return  {@code true} is returned if an ARP request was actually sent
     *          to the network, Otherwise {@code false} is returned.
     * @throws VTNException  An error occurred.
     */
    public boolean probeHost(VTNManagerImpl mgr, MapReference ref,
                             PacketContext pctx) throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(ref.getPath());
            return vbr.probeHost(mgr, ref, pctx);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Handler for receiving the packet.
     *
     * @param mgr   VTN manager service.
     * @param ref   Reference to the virtual network mapping.
     *              A virtual node pointed by {@code ref} must be contained
     *              in this tenant.
     * @param pctx  The context of the received packet.
     * @return  A {@code PacketResult} which indicates the result.
     * @throws VTNException  An error occurred.
     */
    public PacketResult receive(VTNManagerImpl mgr, MapReference ref,
                                PacketContext pctx) throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(ref.getPath());

            // Evaluate path maps.
            RouteResolver rr = evalPathMap(mgr, pctx);
            pctx.setRouteResolver(rr);

            PacketResult res = vbr.receive(mgr, ref, pctx);
            if (res != PacketResult.IGNORED) {
                pctx.purgeObsoleteFlow(mgr, tenantName);
            }

            return res;
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Invoked when the recalculation of the all shortest path tree is done.
     *
     * @param mgr  VTN manager service.
     */
    public void recalculateDone(VTNManagerImpl mgr) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.recalculateDone(mgr);
            }
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Destroy the virtual tenant.
     *
     * @param mgr  VTN manager service.
     */
    public void destroy(VTNManagerImpl mgr) {
        ContainerConfig cfg = new ContainerConfig(containerName);
        cfg.delete(ContainerConfig.Type.TENANT, tenantName);

        Lock wrlock = rwLock.writeLock();
        wrlock.lock();
        try {
            // Destroy all bridges.
            for (Iterator<VBridgeImpl> it = vBridges.values().iterator();
                 it.hasNext();) {
                VBridgeImpl vbr = it.next();
                vbr.destroy(mgr, false);
                it.remove();
            }
        } finally {
            wrlock.unlock();
        }

        // Purge global timer task queue.
        IVTNResourceManager resMgr = mgr.getResourceManager();
        Timer timer = resMgr.getTimer();
        timer.purge();
    }

    /**
     * Update the state of the specified vBridge in this tenant.
     *
     * @param mgr    VTN Manager service.
     * @param path   Path to the target bridge.
     * @throws VTNException  The specified vBridge does not exist.
     */
    public void updateBridgeState(VTNManagerImpl mgr, VBridgePath path)
        throws VTNException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VBridgeImpl vbr = getBridgeImpl(path);
            vbr.update(mgr);
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return a list of VTN path maps configured in this VTN.
     *
     * @return  A list of {@link PathMap} instances.
     */
    public List<PathMap> getPathMaps() {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            List<PathMap> list = new ArrayList<PathMap>(pathMaps.size());
            for (VTenantPathMapImpl vpm: pathMaps.values()) {
                list.add(vpm.getPathMap());
            }

            return list;
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return the VTN path map associated with the specified index number
     * in this VTN.
     *
     * @param index  The index number of the VTN path map.
     * @return  A {@link PathMap} instance if found.
     *          {@code null} if not found.
     */
    public PathMap getPathMap(int index) {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            VTenantPathMapImpl vpm = pathMaps.get(index);
            return (vpm == null) ? null : vpm.getPathMap();
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Create or modify the VTN path map specified by the index number.
     *
     * @param mgr    VTN Manager service.
     * @param index  The index number of the VTN path map.
     * @param pmap   A {@link PathMap} instance which specifies the
     *               configuration of the path map.
     * @return  A {@link UpdateType} object which represents the result of the
     *          operation is returned.
     * @throws VTNException  An error occurred.
     */
    public UpdateType setPathMap(VTNManagerImpl mgr, int index, PathMap pmap)
        throws VTNException {
        VTenantPathMapImpl vpm = new VTenantPathMapImpl(this, index, pmap);
        Integer key = Integer.valueOf(index);

        Lock wrlock = rwLock.writeLock();
        wrlock.lock();
        try {
            UpdateType result;
            VTenantPathMapImpl oldvpm = pathMaps.put(key, vpm);
            if (oldvpm == null) {
                result = UpdateType.ADDED;
            } else if (oldvpm.equals(vpm)) {
                // No change was made to path map.
                return null;
            } else {
                result = UpdateType.CHANGED;
            }

            // REVISIT: Select flow entries affected by the change.
            VTNFlowDatabase fdb = mgr.getTenantFlowDB(tenantName);
            if (fdb != null) {
                VTNThreadData.removeFlows(mgr, fdb);
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("{}:{}.{}: VTN path map was {}: {}",
                          containerName, tenantName, key, result.getName(),
                          pmap);
            } else {
                LOG.info("{}:{}.{}: VTN path map was {}.",
                         containerName, tenantName, key, result.getName());
            }
            VTenantPathMapEvent.raise(mgr, tenantName, index, result);

            mgr.export(this);
            Status status = saveConfig(null);
            if (!status.isSuccess()) {
                throw new VTNException(status);
            }

            return result;
        } finally {
            wrlock.unlock();
        }
    }

    /**
     * Remove the VTN path map specified by the index number.
     *
     * @param mgr    VTN Manager service.
     * @param index  The index number of the VTN path map.
     * @return  A {@link Status} object which represents the result of the
     *          operation is returned.
     */
    public Status removePathMap(VTNManagerImpl mgr, int index) {
        Integer key = Integer.valueOf(index);

        Lock wrlock = rwLock.writeLock();
        wrlock.lock();
        try {
            VTenantPathMapImpl vpm = pathMaps.remove(key);
            if (vpm == null) {
                return null;
            }

            // REVISIT: Select flow entries affected by the change.
            VTNFlowDatabase fdb = mgr.getTenantFlowDB(tenantName);
            if (fdb != null) {
                VTNThreadData.removeFlows(mgr, fdb);
            }

            if (LOG.isTraceEnabled()) {
                LOG.trace("{}:{}.{}: VTN path map was removed: {}",
                          containerName, tenantName, key, vpm.getPathMap());
            } else {
                LOG.info("{}:{}.{}: Container path map was removed.",
                         containerName, tenantName, key);
            }
            VTenantPathMapEvent.raise(mgr, tenantName, index,
                                      UpdateType.REMOVED);

            mgr.export(this);
            return saveConfig(null);
        } finally {
            wrlock.unlock();
        }
    }

    /**
     * Determine whether the given object is identical to this object.
     *
     * @param o  An object to be compared.
     * @return   {@code true} if identical. Otherwise {@code false}.
     */
    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof VTenantImpl)) {
            return false;
        }

        VTenantImpl vtn = (VTenantImpl)o;
        if (!containerName.equals(vtn.containerName)) {
            return false;
        }
        if (!tenantName.equals(vtn.tenantName)) {
            return false;
        }

        VTenantConfig tconf = getVTenantConfig();
        VTenantConfig otherTconf = vtn.getVTenantConfig();
        if (!tconf.equals(otherTconf)) {
            return false;
        }

        // Use copy of bridge map in order to avoid deadlock.
        Map<String, VBridgeImpl> otherBridges;
        Map<Integer, VTenantPathMapImpl> otherPathMaps;
        Lock rdlock = vtn.rwLock.readLock();
        rdlock.lock();
        try {
            otherBridges = (Map<String, VBridgeImpl>)
                ((TreeMap<String, VBridgeImpl>)vtn.vBridges).clone();
            otherPathMaps = (Map<Integer, VTenantPathMapImpl>)
                ((TreeMap<Integer, VTenantPathMapImpl>)vtn.pathMaps).clone();
        } finally {
            rdlock.unlock();
        }

        rdlock = rwLock.readLock();
        try {
            return (vBridges.equals(otherBridges) &&
                    pathMaps.equals(otherPathMaps));
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return the hash code of this object.
     *
     * @return  The hash code.
     */
    @Override
    public int hashCode() {
        int h = containerName.hashCode() ^ tenantName.hashCode() ^
            getVTenantConfig().hashCode();

        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            h += (vBridges.hashCode() * 17) + (pathMaps.hashCode() * 31);
        } finally {
            rdlock.unlock();
        }

        return h;
    }

    /**
     * Remove MAC address table entries relevant to the specified pair of
     * switch port and VLAN ID from all existing MAC address tables.
     *
     * <p>
     *   This method must be called with holding the tenant lock.
     * </p>
     *
     * @param mgr   VTN Manager service.
     * @param port  A node connector associated with a switch port.
     * @param vlan  A VLAN ID.
     */
    void removeMacTableEntries(VTNManagerImpl mgr, NodeConnector port,
                               short vlan) {
        for (VBridgeImpl vbr: vBridges.values()) {
            VBridgePath path = vbr.getPath();
            MacAddressTable table = mgr.getMacAddressTable(path);
            table.flush(port, vlan);
        }
    }

    /**
     * Remove MAC address table entries relevant to the specified network
     * from all existing MAC address tables.
     *
     * <p>
     *   The method removes all MAC address table entries which meet all
     *   the following conditions.
     * </p>
     * <ul>
     *   <li>
     *     The specified VLAN ID is configured in the entry.
     *   </li>
     *   <li>
     *     A {@link NodeConnector} instance in the entry is accepted by
     *     {@link PortFilter} instance passed to {@code filter}.
     *   </li>
     * </ul>
     * <p>
     *   This method must be called with holding the tenant lock.
     * </p>
     *
     * @param mgr     VTN Manager service.
     * @param filter  A {@link PortFilter} instance which selects switch ports.
     * @param vlan    A VLAN ID.
     */
    void removeMacTableEntries(VTNManagerImpl mgr, PortFilter filter,
                               short vlan) {
        for (VBridgeImpl vbr: vBridges.values()) {
            VBridgePath path = vbr.getPath();
            MacAddressTable table = mgr.getMacAddressTable(path);
            table.flush(filter, vlan);
        }
    }

    /**
     * Merge the given VTN configuration to the current configuration.
     *
     * <p>
     *   If at least one field in {@code tconf} keeps a valid value, this
     *   method creates a shallow copy of the current configuration, and set
     *   valid values in {@code tconf} to the copy.
     * </p>
     *
     * @param tconf  Configuration to be merged.
     * @return  A merged {@code VTenantConfig} object.
     */
    private VTenantConfig merge(VTenantConfig tconf) {
        String desc = tconf.getDescription();
        int idle = tconf.getIdleTimeout();
        int hard = tconf.getHardTimeout();
        if (desc == null && idle < 0 && hard < 0) {
            return tenantConfig;
        }

        if (desc == null) {
            desc = tenantConfig.getDescription();
        }
        if (idle < 0) {
            idle = tenantConfig.getIdleTimeout();
        }
        if (hard < 0) {
            hard = tenantConfig.getHardTimeout();
        }

        return new VTenantConfig(desc, idle, hard);
    }

    /**
     * Resolve undefined attributes in the specified tenant configuration.
     *
     * @param tconf  The tenant configuration.
     * @return       {@code VTenantConfig} to be applied.
     */
    private VTenantConfig resolve(VTenantConfig tconf) {
        int idle = tconf.getIdleTimeout();
        int hard = tconf.getHardTimeout();
        if (idle < 0) {
            idle = DEFAULT_IDLE_TIMEOUT;
            if (hard < 0) {
                hard = DEFAULT_HARD_TIMEOUT;
            }
        } else if (hard < 0) {
            hard = DEFAULT_HARD_TIMEOUT;
        } else {
            return tconf;
        }

        return new VTenantConfig(tconf.getDescription(), idle, hard);
    }

    /**
     * Ensure that the specified tenant configuration is valid.
     *
     * @param tconf  The tenant configuration to be tested.
     * @throws VTNException  An error occurred.
     */
    private void checkConfig(VTenantConfig tconf) throws VTNException {
        int idle = tconf.getIdleTimeout();
        int hard = tconf.getHardTimeout();
        if (idle > MAX_FLOW_TIMEOUT) {
            throw new VTNException(StatusCode.BADREQUEST,
                                   "Invalid idle timeout");
        }
        if (hard > MAX_FLOW_TIMEOUT) {
            throw new VTNException(StatusCode.BADREQUEST,
                                   "Invalid hard timeout");
        }
        if (idle != 0 && hard != 0 && idle >= hard) {
            String msg = "Idle timeout must be less than hard timeout";
            throw new VTNException(StatusCode.BADREQUEST, msg);
        }
    }

    /**
     * Read data from the given input stream and deserialize.
     *
     * @param in  An input stream.
     * @throws IOException
     *    An I/O error occurred.
     * @throws ClassNotFoundException
     *    At least one necessary class was not found.
     */
    @SuppressWarnings("unused")
    private void readObject(ObjectInputStream in)
        throws IOException, ClassNotFoundException {
        // Read serialized fields.
        // Note that this lock needs to be acquired here because this instance
        // is not yet visible.
        in.defaultReadObject();

        // Reset the lock.
        rwLock = new ReentrantReadWriteLock();

        // Read the number of vBridges.
        int size = in.readInt();

        // Read vBridges.
        vBridges = new TreeMap<String, VBridgeImpl>();
        for (int i = 0; i < size; i++) {
            String name = (String)in.readObject();
            VBridgeImpl vbr = (VBridgeImpl)in.readObject();

            // Set this tenant as parent of this vBridge.
            vbr.setPath(this, name);
            vBridges.put(name, vbr);
        }

        // Read the number of vTN path maps.
        size = in.readInt();

        // Read VTN path maps.
        pathMaps = new TreeMap<Integer, VTenantPathMapImpl>();
        for (int i = 0; i < size; i++) {
            VTenantPathMapImpl vpm = (VTenantPathMapImpl)in.readObject();
            Integer key = Integer.valueOf(vpm.getIndex());

            // Set this tenant as parent of this path map.
            vpm.setVTenant(this);
            pathMaps.put(key, vpm);
        }
    }

    /**
     * Serialize this object and write it to the given output stream.
     *
     * @param out  An output stream.
     * @throws IOException
     *    An I/O error occurred.
     */
    @SuppressWarnings("unused")
    private void writeObject(ObjectOutputStream out) throws IOException {
        Lock rdlock = rwLock.readLock();
        rdlock.lock();
        try {
            // Write serialized fields.
            out.defaultWriteObject();

            // Write the number of vBridges.
            out.writeInt(vBridges.size());

            // Write vBridges.
            for (Map.Entry<String, VBridgeImpl> entry: vBridges.entrySet()) {
                out.writeObject(entry.getKey());
                out.writeObject(entry.getValue());
            }

            // Write the number of VTN path maps.
            out.writeInt(pathMaps.size());

            // Write VTN path maps.
            for (VTenantPathMapImpl vpm: pathMaps.values()) {
                out.writeObject(vpm);
            }
        } finally {
            rdlock.unlock();
        }
    }

    /**
     * Return a failure status that indicates the specified bridge does not
     * exist.
     *
     * @param bridgeName  The name of the bridge.
     * @return  A failure status.
     */
    private Status bridgeNotFound(String bridgeName) {
        String msg = bridgeName + ": Bridge does not exist";
        return new Status(StatusCode.NOTFOUND, msg);
    }

    /**
     * Return the virtual bridge instance associated with the given name.
     *
     * <p>
     *   This method must be called with holding the tenant lock.
     * </p>
     *
     * @param path  Path to the bridge.
     * @return  Virtual bridge instance is returned.
     * @throws VTNException  An error occurred.
     * @throws NullPointerException  {@code path} is {@code null}.
     */
    private VBridgeImpl getBridgeImpl(VBridgePath path) throws VTNException {
        String bridgeName = path.getBridgeName();
        if (bridgeName == null) {
            Status status = MiscUtils.argumentIsNull("Bridge name");
            throw new VTNException(status);
        }

        VBridgeImpl vbr = vBridges.get(bridgeName);
        if (vbr == null) {
            Status status = bridgeNotFound(bridgeName);
            throw new VTNException(status);
        }

        return vbr;
    }

    /**
     * Return the MAC address table for the specified virtual bridge.
     *
     * <p>
     *   This method must be called with holding the tenant lock.
     * </p>
     *
     * @param mgr   VTN manager service.
     * @param path  Path to the bridge.
     * @return  MAC address table for the specified bridge.
     * @throws VTNException  An error occurred.
     * @throws NullPointerException  {@code path} is {@code null}.
     */
    private MacAddressTable getMacAddressTable(VTNManagerImpl mgr,
                                               VBridgePath path)
        throws VTNException {
        MacAddressTable table = mgr.getMacAddressTable(path);
        if (table != null) {
            return table;
        }

        String bridgeName = path.getBridgeName();
        Status status = (bridgeName == null)
            ? MiscUtils.argumentIsNull("Bridge name")
            : bridgeNotFound(bridgeName);

        throw new VTNException(status);
    }


    /**
     * Save tenant configuration to the configuration file.
     *
     * <p>
     *   This method must be called with holding the tenant lock.
     * </p>
     *
     * @param mgr  VTN Manager service.
     *             If a non-{@code null} value is specified, this method checks
     *             whether the current configuration is applied correctly.
     * @return  "Success" or failure reason.
     */
    private Status saveConfigImpl(VTNManagerImpl mgr) {
        ContainerConfig cfg = new ContainerConfig(containerName);
        if (mgr != null) {
            // Adjust interval of MAC address table aging.
            for (VBridgeImpl vbr: vBridges.values()) {
                vbr.initMacTableAging(mgr);
            }
        }

        Status status = cfg.save(ContainerConfig.Type.TENANT, tenantName,
                                 this);
        if (status.isSuccess()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("{}:{}: Tenant was saved",
                          containerName, tenantName);
            }
            return status;
        }

        String msg = "Failed to save tenant configuration";
        LOG.error("{}:{}: {}: {}", containerName, tenantName, msg, status);
        return new Status(StatusCode.INTERNALERROR, msg);
    }

    /**
     * Evaluate the VTN path map list against the specified packet.
     *
     * <p>
     *   This method must be called with holding the VTN Manager lock and
     *   the tenant lock in order.
     * </p>
     *
     * @param mgr   VTN Manager service.
     * @param pctx  The context of the ARP packet to send.
     * @return  A {@link RouteResolver} instance is returned if a path map in
     *          the container path map list matched the packet.
     *          The default route resolver is returned if no container path
     *          map patched the packet.
     */
    private RouteResolver evalPathMap(VTNManagerImpl mgr, PacketContext pctx) {
        // Evaluate VTN path map list.
        for (VTenantPathMapImpl vpm: pathMaps.values()) {
            RouteResolver rr = vpm.evaluate(mgr, pctx);
            if (rr != null) {
                return rr;
            }
        }

        // Evaluate container path map list.
        return mgr.evalPathMap(pctx, tenantConfig);
    }
}
