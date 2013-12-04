/**
 * Copyright (c) 2013 NEC Corporation
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.vtn.manager.internal;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;

import org.apache.felix.dm.impl.ComponentImpl;
import org.junit.Test;
import org.opendaylight.controller.clustering.services.CacheConfigException;
import org.opendaylight.controller.clustering.services.CacheExistException;
import org.opendaylight.controller.clustering.services.IClusterGlobalServices;
import org.opendaylight.controller.clustering.services.IClusterServices.cacheMode;
import org.opendaylight.controller.sal.core.Node;
import org.opendaylight.controller.sal.core.NodeConnector;
import org.opendaylight.controller.sal.utils.NodeConnectorCreator;
import org.opendaylight.controller.sal.utils.NodeCreator;
import org.opendaylight.vtn.manager.MacAddressEntry;
import org.opendaylight.vtn.manager.VBridgeConfig;
import org.opendaylight.vtn.manager.VBridgeIfPath;
import org.opendaylight.vtn.manager.VBridgePath;
import org.opendaylight.vtn.manager.VTNException;
import org.opendaylight.vtn.manager.VTenantConfig;
import org.opendaylight.vtn.manager.VTenantPath;
import org.opendaylight.vtn.manager.internal.cluster.ClusterEventId;
import org.opendaylight.vtn.manager.internal.cluster.MacTableEntry;
import org.opendaylight.vtn.manager.internal.cluster.MacTableEntryId;
import org.opendaylight.vtn.manager.internal.cluster.PortVlan;

/**
 * JUnit test for {@link GlobalResourceManager}.
 */
public class GlobalResourceManagerTest extends TestBase {
    /**
     * Cluster cache name associated with
     * {@link GlobalResourceManager#vlanMaps}.
     */
    private static final String  CACHE_VLANMAP = "vtn.vlanmap";

    /**
     * Cluster cache name associated with
     * {@link GlobalResourceManager#portMaps}.
     */
    private static final String  CACHE_PORTMAP = "vtn.portmap";

    /**
     * Stub class of {@link IClusterGlobalServices}.
     * Some methods in this class always return null.
     */
    class ClusterServiceStub implements IClusterGlobalServices {
        private ConcurrentMap<String, ConcurrentMap<?, ?>> caches
            = new ConcurrentHashMap<String, ConcurrentMap<?, ?>>();

        @Override
        public ConcurrentMap<?, ?> createCache(String cacheName,
                Set<cacheMode> cMode) throws CacheExistException,
                CacheConfigException {
            ConcurrentMap<?, ?> res = this.caches.get(cacheName);
            if (res == null) {
                res = new ConcurrentHashMap<Object, Object>();
                this.caches.put(cacheName, res);
                return res;
            }
            throw new CacheExistException();
        }

        @Override
        public ConcurrentMap<?, ?> getCache(String cacheName) {
            return this.caches.get(cacheName);
        }

        @Override
        public void destroyCache(String cacheName) {
            this.caches.remove(cacheName);
        }

        @Override
        public boolean existCache(String cacheName) {
            return (this.caches.get(cacheName) != null);
        }

        @Override
        public Set<String> getCacheList() {
            return this.caches.keySet();
        }

        @Override
        public Properties getCacheProperties(String cacheName) {
            return null;
        }

        @Override
        public void tbegin() throws NotSupportedException, SystemException {

        }

        @Override
        public void tbegin(long timeout, TimeUnit unit)
            throws NotSupportedException, SystemException {
        }

        @Override
        public void tcommit() throws RollbackException,
                HeuristicMixedException, HeuristicRollbackException,
                SecurityException, IllegalStateException, SystemException {

        }

        @Override
        public void trollback() throws IllegalStateException,
                SecurityException, SystemException {
        }

        @Override
        public Transaction tgetTransaction() throws SystemException {
            return null;
        }

        @Override
        public InetAddress getCoordinatorAddress() {
            return null;
        }

        @Override
        public List<InetAddress> getClusteredControllers() {
            return null;
        }

        @Override
        public InetAddress getMyAddress() {
            return null;
        }

        @Override
        public boolean amICoordinator() {
            return true;
        }

        @Override
        public void removeContainerCaches(String containerName) {

        }
    }


    /**
     * Test method for
     * {@link GlobalResourceManager#init(org.apache.felix.dm.Component)},
     * {@link GlobalResourceManager#destroy()},
     * {@link GlobalResourceManager#getTimer()}.
     */
    @Test
    public void testInitDestroy() {
        ComponentImpl c = new ComponentImpl(null, null, null);
        GlobalResourceManager grsc = new GlobalResourceManager();
        TestStub stubObj = new TestStub(0);
        IClusterGlobalServices cs = (IClusterGlobalServices) stubObj;

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("containerName", "default");
        c.setServiceProperties(properties);

        grsc.destroy();
        grsc.init(c);

        ConcurrentMap<Short, String> vmap
            = (ConcurrentMap<Short, String>) cs.getCache(CACHE_VLANMAP);
        assertNull(vmap);

        ConcurrentMap<Short, String> pmap
            = (ConcurrentMap<Short, String>) cs.getCache(CACHE_PORTMAP);
        assertNull(pmap);

        InetAddress loopback = InetAddress.getLoopbackAddress();
        ClusterEventId evid = new  ClusterEventId();
        assertEquals(loopback, evid.getControllerAddress());

        grsc.destroy();


        List<IClusterGlobalServices> csSet = new ArrayList<IClusterGlobalServices>();

        // set TestStub to cluster global service.
        csSet.add(stubObj);

        // in case IP address of controller is not loopback address.
        TestStubCluster cls = new TestStubCluster(2);
        csSet.add(cls);

        // in case methods of cluster service return null.
        ClusterServiceStub clsnull = new ClusterServiceStub();
        csSet.add(clsnull);

        for (IClusterGlobalServices service : csSet) {
            grsc.setClusterGlobalService(service);
            grsc.init(c);

            Timer tm = grsc.getTimer();
            assertNotNull(tm);

            vmap = (ConcurrentMap<Short, String>) cs.getCache(CACHE_VLANMAP);
            assertNotNull(vmap);
            assertEquals(0, vmap.size());

            pmap = (ConcurrentMap<Short, String>) cs.getCache(CACHE_PORTMAP);
            assertNotNull(pmap);
            assertEquals(0, pmap.size());

            evid = new  ClusterEventId();
            if (service.getMyAddress() != null
                    && !service.getMyAddress().equals(loopback)) {
                assertEquals(service.getMyAddress(), evid.getControllerAddress());
            } else {
                assertEquals(loopback, evid.getControllerAddress());
            }

            grsc.destroy();
        }
    }

    /**
     * Test method for
     * {@link GlobalResourceManager#setClusterGlobalService(IClusterGlobalServices)},
     * {@link GlobalResourceManager#unsetClusterGlobalService(IClusterGlobalServices)}.
     */
    @Test
    public void testSetUnsetClusterGlobalService() {
        ComponentImpl c = new ComponentImpl(null, null, null);
        GlobalResourceManager grsc = new GlobalResourceManager();
        TestStub stubObj = new TestStub(0);
        TestStub stubObj2 = new TestStub(0);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("containerName", "default");
        c.setServiceProperties(properties);

        grsc.setClusterGlobalService(stubObj);

        grsc.unsetClusterGlobalService(stubObj2);

        grsc.unsetClusterGlobalService(stubObj);
    }

    /**
     * Test method for
     * {@link GlobalResourceManager#registerVlanMap(String, VBridgePath, short)},
     * {@link GlobalResourceManager#unregisterVlanMap(short)}.
     */
    @Test
    public void testRegisterVlanMap() {
        List<String> containerNames = new ArrayList<String>();
        containerNames.add("default");
        containerNames.add("tenant");

        for (String containerName : containerNames) {
            TestStub stubObj = new TestStub(0);
            GlobalResourceManager grsc
                = setupGlobalResourceManager(stubObj);
            IClusterGlobalServices cs = (IClusterGlobalServices)stubObj;

            String tname = "tenant";
            String bname = "bridge";
            VBridgePath bpath = new VBridgePath(tname, bname);
            short vlan = 0;
            String reg = grsc.registerVlanMap(containerName, bpath, vlan);
            assertEquals(null, reg);
            checkMapCache(cs, "vtn.vlanmap", containerName, bpath, vlan);

            // add the same VlanMap with different bpath
            String bnamenew = "bridgenew";
            VBridgePath bpathnew = new VBridgePath(tname, bnamenew);
            reg = grsc.registerVlanMap(containerName, bpathnew, vlan);
            String required = containerName + ":" + bpath.toString();
            assertEquals(required, reg);
            checkMapCache(cs, "vtn.vlanmap", containerName, bpath, vlan);

            reg = grsc.registerVlanMap("other", bpathnew, vlan);
            assertEquals(required, reg);
            checkMapCache(cs, "vtn.vlanmap", containerName, bpath, vlan);

            // add with different VLAN ID
            reg = grsc.registerVlanMap(containerName, bpathnew, (short) 4095);
            required = containerName + ":" + bpath.toString();
            assertEquals(null, reg);
            checkMapCache(cs, "vtn.vlanmap", containerName, bpathnew, (short) 4095);

            grsc.unregisterVlanMap((short) 0);

            // check if other setting exist after unregister entry a VLAN ID == 0
            reg = grsc.registerVlanMap(containerName, bpath, (short) 4095);
            required = containerName + ":" + bpathnew.toString();
            assertEquals(required, reg);
            checkMapCache(cs, "vtn.vlanmap", containerName, bpathnew, (short) 4095);

            // set a entry vlan == 0 again
            reg = grsc.registerVlanMap(containerName, bpathnew, (short) 0);
            required = containerName + ":" + bpathnew.toString();
            assertEquals(null, reg);
            checkMapCache(cs, "vtn.vlanmap", containerName, bpathnew, (short) 0);

            // try to unregister not registered
            try {
                grsc.unregisterVlanMap((short) 1);
                fail("throwing exception was expected.");
            } catch (IllegalStateException e) {
                // expected path
            }

            grsc.unregisterVlanMap((short) 0);
            grsc.unregisterVlanMap((short) 4095);
            grsc.destroy();
        }
    }


    /**
     * Test method for
     * {@link GlobalResourceManager#registerPortMap(String, VBridgeIfPath, PortVlan)},
     * {@link GlobalResourceManager#unregisterPortMap(PortVlan)}.
     */
    @Test
    public void testRegisterPortMap() {
        short[] vlans = { 0, 10, 4095 };
        List<String> containerNames = new ArrayList<String>();
        containerNames.add("default");
        containerNames.add("tenant");

        TestStub stubObj = new TestStub(0);

        for (String containerName : containerNames) {
            GlobalResourceManager grsc = setupGlobalResourceManager(stubObj);
            IClusterGlobalServices cs = (IClusterGlobalServices)stubObj;

            for (NodeConnector nc : createNodeConnectors(3, false)) {
                for (short vlan : vlans) {
                    String emsg = "(NodeConnector)" + nc.toString()
                            + ",(vlan)" + vlan;

                    String tname = "tenant";
                    String bname = "bridge";
                    String ifname = "interface";
                    VBridgeIfPath bifpath = new VBridgeIfPath(tname, bname, ifname);
                    PortVlan pv = new PortVlan(nc, vlan);
                    String reg = grsc.registerPortMap(containerName, bifpath, pv);
                    assertEquals(emsg, null, reg);
                    checkMapCache(cs, "vtn.portmap", containerName, bifpath, pv);

                    // add conflict PortVlan
                    String bnamenew = "bridgenew";
                    String ifnamenew = "interfacenew";
                    VBridgeIfPath bifpathnew = new VBridgeIfPath(tname, bnamenew,
                                                                 ifnamenew);
                    reg = grsc.registerPortMap(containerName, bifpathnew, pv);
                    String required = containerName + ":" + bifpath.toString();
                    assertEquals(emsg, required, reg);
                    checkMapCache(cs, "vtn.portmap", containerName, bifpath, pv);

                    reg = grsc.registerPortMap("other", bifpathnew, pv);
                    assertEquals(emsg, required, reg);
                    checkMapCache(cs, "vtn.portmap", containerName, bifpath, pv);

                    // add non-conflict PortVlan
                    PortVlan pvnew = new PortVlan(nc, (short)4094);
                    reg = grsc.registerPortMap(containerName, bifpathnew, pvnew);
                    required = containerName + ":" + bifpathnew.toString();
                    assertEquals(emsg, null, reg);
                    checkMapCache(cs, "vtn.portmap", containerName, bifpathnew,
                                  pvnew);

                    grsc.unregisterPortMap(pv);

                    // check if another entry exist after calling unregisterPortMap()
                    reg = grsc.registerPortMap(containerName, bifpath, pvnew);
                    required = containerName + ":" + bifpathnew.toString();
                    assertEquals(emsg, required, reg);
                    checkMapCache(cs, "vtn.portmap", containerName, bifpathnew,
                                  pvnew);

                    reg = grsc.registerPortMap(containerName, bifpathnew, pv);
                    required = containerName + ":" + bifpathnew.toString();
                    assertEquals(emsg, null, reg);
                    checkMapCache(cs, "vtn.portmap", containerName, bifpathnew, pv);

                    try {
                        grsc.unregisterPortMap(new PortVlan(nc, (short) 1));
                        fail("throwing exception was expected.");
                    } catch (IllegalStateException e) {

                    }

                    grsc.unregisterPortMap(pvnew);
                    grsc.unregisterPortMap(pv);
                }
            }
            grsc.destroy();
        }
    }

    /**
     * Test case for {@link GlobalResourceManager#getApiVersion()} and
     * {@link GlobalResourceManager#getBundleVersion()}.
     */
    @Test
    public void testGetVersion() {
        GlobalResourceManager grsc = new GlobalResourceManager();
        assertEquals(GlobalResourceManager.API_VERSION, grsc.getApiVersion());

        // This should return null because the VTN Manager is not loaded by
        // a OSGi bundle class loader.
        assertNull(grsc.getBundleVersion());
    }

    /**
     * Test method for
     * {@link GlobalResourceManager#cleanUp(String)}.
     */
    @Test
    public void testCleanUp() {
        List<String> containerNames = new ArrayList<String>();
        containerNames.add("default");
        containerNames.add("tenant");

        for (String containerName : containerNames) {

            TestStub stubObj = new TestStub(0);

            GlobalResourceManager grsc = setupGlobalResourceManager(stubObj);
            IClusterGlobalServices cs = (IClusterGlobalServices)stubObj;

            String tname = "tenant";
            String bname = "bridge";
            VBridgePath bpath = new VBridgePath(tname, bname);
            short vlan = 0;
            String reg = grsc.registerVlanMap(containerName, bpath, vlan);
            assertEquals(null, reg);
            checkMapCache(cs, "vtn.vlanmap", containerName, bpath, vlan);

            Node node = NodeCreator.createOFNode(Long.valueOf("0"));
            NodeConnector nc = NodeConnectorCreator
                    .createOFNodeConnector(Short.valueOf((short) 10), node);
            String ifname = "interface";
            VBridgeIfPath bifpath = new VBridgeIfPath(tname, bname, ifname);
            PortVlan pv = new PortVlan(nc, vlan);
            reg = grsc.registerPortMap(containerName, bifpath, pv);
            assertEquals(null, reg);
            checkMapCache(cs, "vtn.portmap", containerName, bifpath, pv);

            grsc.cleanUp("clean");

            ConcurrentMap<Short, String> vmap
                = (ConcurrentMap<Short, String>) cs.getCache(CACHE_VLANMAP);
            assertEquals(1, vmap.size());
            ConcurrentMap<PortVlan, String> pmap
                = (ConcurrentMap<PortVlan, String>) cs.getCache(CACHE_PORTMAP);
            assertEquals(1, pmap.size());

            grsc.cleanUp(containerName);

            // after clean up there isn't entry.
            vmap = (ConcurrentMap<Short, String>) cs.getCache(CACHE_VLANMAP);
            assertEquals(0, vmap.size());
            pmap = (ConcurrentMap<PortVlan, String>) cs.getCache(CACHE_PORTMAP);
            assertEquals(0, pmap.size());

            grsc.destroy();
        }
    }

    /**
     * Test case for {@link GlobalResourceManager#executeAsync(Runnable)}.
     */
    @Test
    public void testExecuteAsync() {
        /**
         * task Thread class used for test.
         * this task wait until a latch is counted down to zero.
         */
        class WaitThreadTask implements Runnable {
            protected long waitTime = 0;
            protected CountDownLatch latch = null;

            WaitThreadTask(long timeout, CountDownLatch latch) {
                this.waitTime = timeout;
                this.latch = latch;
            }

            @Override
            synchronized public void run() {
                latch.countDown();
                await(waitTime, TimeUnit.SECONDS);
            }

            protected boolean await(long timeout, TimeUnit unit) {
                try {
                    return latch.await(timeout, unit);
                } catch (InterruptedException e) {
                    return false;
                }
            }
        }

        GlobalResourceManager grsc = new GlobalResourceManager();
        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("containerName", "default");
        ComponentImpl c = new ComponentImpl(null, null, null);
        c.setServiceProperties(properties);
        grsc.init(c);

        CountDownLatch latch = new CountDownLatch(2);

        WaitThreadTask task1 = new WaitThreadTask(1000L, latch);
        grsc.executeAsync(task1);
        assertFalse(task1.await(10L, TimeUnit.MILLISECONDS));

        WaitThreadTask task2 = new WaitThreadTask(1000L, latch);
        grsc.executeAsync(task2);
        assertTrue(task1.await(1000L, TimeUnit.MILLISECONDS));
        assertTrue(task2.await(1000L, TimeUnit.MILLISECONDS));

        grsc.destroy();
    }

    /**
     * test case for
     * {@link GlobalResourceManager#getRemoteClusterSize()} and
     * {@link GlobalResourceManager#isRemoteClusterAddress(InetAddress)}.
     */
    @Test
    public void testGetRemoteClusterSize() {
        InetAddress loopbackAddr = InetAddress.getLoopbackAddress();
        InetAddress otherAddr
            = getInetAddressFromAddress(new byte[] {(byte) 10, (byte) 0,
                                                    (byte) 1, (byte) 99});

        ComponentImpl c = new ComponentImpl(null, null, null);
        GlobalResourceManager grsc = new GlobalResourceManager();
        TestStub stubObj = new TestStub(0);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("containerName", "default");
        c.setServiceProperties(properties);

        grsc.destroy();

        // 1 controller.
        grsc.setClusterGlobalService(stubObj);
        grsc.init(c);

        int numCont = grsc.getRemoteClusterSize();
        assertEquals(0, numCont);

        assertFalse(grsc.isRemoteClusterAddress(loopbackAddr));
        assertFalse(grsc.isRemoteClusterAddress(otherAddr));

        grsc.destroy();

        // 2 controllers.
        TestStubCluster csStub = new TestStubCluster(2);
        grsc.setClusterGlobalService(csStub);
        grsc.init(c);

        numCont = grsc.getRemoteClusterSize();
        assertEquals(1, numCont);

        for (InetAddress addr : csStub.getClusteredControllers()) {
            if (csStub.getMyAddress().equals(addr)) {
                assertFalse(grsc.isRemoteClusterAddress(addr));
            } else {
                assertTrue(grsc.isRemoteClusterAddress(addr));
            }
        }
        assertFalse(grsc.isRemoteClusterAddress(loopbackAddr));
        assertFalse(grsc.isRemoteClusterAddress(otherAddr));

        grsc.destroy();

        // if cluster service return null.
        ClusterServiceStub clsnull = new ClusterServiceStub();
        grsc.setClusterGlobalService(clsnull);
        grsc.init(c);

        assertEquals(0, grsc.getRemoteClusterSize());
        assertFalse(grsc.isRemoteClusterAddress(loopbackAddr));
        assertFalse(grsc.isRemoteClusterAddress(otherAddr));


        // no cluster service.
        grsc.unsetClusterGlobalService(csStub);
        grsc.init(c);

        assertEquals(0, grsc.getRemoteClusterSize());
        assertFalse(grsc.isRemoteClusterAddress(loopbackAddr));
        assertFalse(grsc.isRemoteClusterAddress(otherAddr));

        grsc.destroy();
    }

    /**
     * Test case for {@link GlobalResourceManager#coordinatorChanged()}.
     */
    @Test
    public void testCoordinatorChanged() {
        setupStartupDir();

        VTNManagerImpl vtnMgr = new VTNManagerImpl();
        GlobalResourceManager resMgr = new GlobalResourceManager();
        ComponentImpl c = new ComponentImpl(null, null, null);
        TestStub stubObj = new TestStub(0);

        Hashtable<String, String> properties = new Hashtable<String, String>();
        properties.put("containerName", "default");
        c.setServiceProperties(properties);

        resMgr.setClusterGlobalService(stubObj);
        resMgr.init(c);
        vtnMgr.setResourceManager(resMgr);
        vtnMgr.setClusterContainerService(stubObj);
        vtnMgr.init(c);
        vtnMgr.clearDisabledNode();

        // add entries.
        VTenantPath tpath = new VTenantPath("tenant");
        VBridgePath bpath = new VBridgePath(tpath.getTenantName(), "bridge");

        vtnMgr.addTenant(tpath, new VTenantConfig(null));
        vtnMgr.addBridge(bpath, new VBridgeConfig(null));

        long mac = 1L;
        MacTableEntryId eid = new MacTableEntryId(bpath, mac);

        Node node = NodeCreator.createOFNode(0L);
        NodeConnector nc = NodeConnectorCreator
                .createOFNodeConnector(Short.valueOf("10"), node);
        InetAddress loopback = InetAddress.getLoopbackAddress();
        MacTableEntry entLoopback = new MacTableEntry(eid, nc, (short) 0, loopback);

        vtnMgr.putMacTableEntry(entLoopback);

        List<MacAddressEntry> entries = getMacEntries(vtnMgr, bpath);
        assertEquals(0, entries.size());

        InetAddress ipaddr
            = getInetAddressFromAddress(new byte[] {(byte) 192, (byte) 168,
                                                    (byte) 0, (byte) 2});
        mac = 2L;
        MacTableEntryId eidRemote = new MacTableEntryId(ipaddr, 2L, bpath, mac);
        MacTableEntry entRemote = new MacTableEntry(eidRemote, nc, (short) 0, ipaddr);

        ipaddr = getInetAddressFromAddress(new byte[] { (byte) 192, (byte) 168,
                                                        (byte) 0, (byte) 3});
        mac = 3L;
        MacTableEntryId eidRemote2 = new MacTableEntryId(ipaddr, 3L, bpath, mac);
        MacTableEntry entRemote2 = new MacTableEntry(eidRemote2, nc, (short) 0,
                                                     ipaddr);

        vtnMgr.putMacTableEntry(entRemote);
        vtnMgr.putMacTableEntry(entRemote2);

        resMgr.coordinatorChanged();

        entries = getMacEntries(vtnMgr, bpath);
        assertEquals(0, entries.size());

        // add remote controller.
        TestStubCluster csStub = new TestStubCluster(2);
        vtnMgr.stop();
        resMgr.setClusterGlobalService(csStub);
        resMgr.init(c);
        vtnMgr.init(c);
        vtnMgr.clearDisabledNode();

        vtnMgr.putMacTableEntry(entRemote);
        vtnMgr.entryUpdated(eidRemote, entRemote, VTNManagerImpl.CACHE_MAC, false);
        vtnMgr.putMacTableEntry(entRemote2);
        vtnMgr.entryUpdated(eidRemote2, entRemote2, VTNManagerImpl.CACHE_MAC, false);
        flushTasks(vtnMgr);
        entries = getMacEntries(vtnMgr, bpath);
        assertEquals(2, entries.size());

        // no change
        resMgr.coordinatorChanged();

        entries = getMacEntries(vtnMgr, bpath);
        assertEquals(2, entries.size());

        // remove remote controller.
        resMgr.setClusterGlobalService(stubObj);

        resMgr.coordinatorChanged();
        flushTasks(vtnMgr);

        entries = getMacEntries(vtnMgr, bpath);
        assertEquals(1, entries.size());

        vtnMgr.stopping();
        vtnMgr.stop();
        vtnMgr.destroy();

        cleanupStartupDir();
    }

    /**
     * Get MAC address entries
     *
     * @param vtnMgr    VTNManager service.
     * @param bpath     A {@link VBridgePath}.
     * @return  A list of {@link MacAddressEntry}.
     */
    private List<MacAddressEntry> getMacEntries(VTNManagerImpl vtnMgr,
                                                VBridgePath bpath) {
        List<MacAddressEntry> entries = null;
        try {
            entries = vtnMgr.getMacEntries(bpath);
        } catch (VTNException e) {
            unexpected(e);
        }

        return entries;
    }

    /**
     * Setup {@link GlobalResourceManager}.
     * @param stubObj       A {@link TestStub} object.
     * @return {@link GlobalResourceManager}.
     */
    private GlobalResourceManager setupGlobalResourceManager (TestStub stubObj) {
        ComponentImpl c = new ComponentImpl(null, null, null);
        GlobalResourceManager grsc = new GlobalResourceManager();

        Hashtable<String, String> properties = new Hashtable<String, String>();
        c.setServiceProperties(properties);

        grsc.setClusterGlobalService(stubObj);
        grsc.init(c);

        return grsc;
    }

    /**
     * Check Map cache.
     * @param cs            A {@link IClusterGlobalServices} object.
     * @param cacheName     A cache name.
     * @param containerName A container name.
     * @param path          {@link VBridgePath} or {@link VBridgeIfPath}.
     * @param key   key value
     */
    private <T, S> void checkMapCache (IClusterGlobalServices cs, String cacheName,
            String containerName, T path, S key) {
        ConcurrentMap<S, String> map
                = (ConcurrentMap<S, String>) cs.getCache(cacheName);
        assertNotNull(map);
        String required = containerName + ":" + path.toString();
        String value = map.get(key);
        assertEquals(required, value);
    }
}
