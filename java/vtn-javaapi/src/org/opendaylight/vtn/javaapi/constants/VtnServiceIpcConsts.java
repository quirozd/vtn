/*
 * Copyright (c) 2012-2013 NEC Corporation
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.vtn.javaapi.constants;


/**
 * VtnServiceIpcConsts.
 */
public final class VtnServiceIpcConsts {
	public static final String VTNKEY = "vtn_key";
	public static final String INPUTDIRECTION = "input_direction";
	public static final String FLOWFILTERKEY = "flowfilter_key";
	public static final String VUNKNOWNNAME = "vunknown_name";
	public static final String VRTKEY = "vrt_key";
	public static final String VTEP_KEY = "vtep_key";
	public static final String SERVERADDR = "server_addr";
	public static final String VROUTERNAME = "vrouter_name";
	public static final String IFNAME = "if_name";
	public static final String VTUNNELNAME = "vtunnel_name";
	public static final String VALID = "valid";
	public static final String CSROWSTATUS = "cs_row_status";
	public static final String CSATTR = "cs_attr";
	public static final String IPTYPE = "ip_type";
	public static final String MACDST="mac_dst";
	public static final String NWMNAME="nwm_name";
	public static final String MACSRC="mac_src";
	public static final String DSTIP="dst_ip";
	public static final String VBRKEY = "vbr_key";
	public static final String VBRFLOWFILTER = "flowfilter_key";
	public static final String VBRIF = "interface";
	public static final String KEYVBRIF = "if_key";
	public static final String ACTION = "action";
	public static final String REDIRECTNODE = "redirect_node";
	public static final String REDIRECTPORT = "redirect_port";
	public static final String MODIFYDSTMACADDR = "modify_dstmac";
	public static final String MODIFYSRCMACADDR = "modify_srcmac";
	public static final String DSCP = "dscp";
	public static final String PRIORITY = "priority";
	public static final String VRTDESCRIPTION = "vrt_description";
	public static final String DHCPRELAYADMINSTATUS ="dhcp_relay_admin_status";
	public static final String IPADDR = "ipaddr";
	public static final String MASK = "mask";
	public static final String MACADDR = "macaddr";
	public static final String VTNNAME = "vtn_name";
	public static final String VTEPGRPNAME = "vtep_grp_name";
	public static final String LABEL = "label";
	public static final String PORTTYPE = "port_type";
	public static final String PORTNAME = "port_name";
	public static final String HOST_ADDR = "host_addr";
	public static final String HOST_ADDR_MASK = "host_addr_mask";
	public static final String VUNKNOWNKEY = "vunknown_key";
	public static final String CONTROLLERNAME = "controller_name";
	public static final String DIRECTION = "direction";
	public static final String SEQUENCENUM = "sequence_num";
	public static final String VTUNNEL_KEY = "vtunnel_key";
	public static final String VTEPGRP_NAME ="vtepgrp_name";
	public static final String TC_OP_CONFIG_ACQUIRE_FORCE = "TC_OP_CONFIG_ACQUIRE_FORCE";
	public static final String TC_OP_CONFIG_ACQUIRE = "TC_OP_CONFIG_ACQUIRE";
	public static final String TC_OPER_SUCCESS = "TC_OPER_SUCCESS";
	public static final String TC_OP_CONFIG_RELEASE = "TC_OP_CONFIG_RELEASE";
	public static final String TC_OP_READ_ACQUIRE = "TC_OP_READ_ACQUIRE";
	public static final String TC_OP_CANDIDATE_COMMIT = "TC_OP_CANDIDATE_COMMIT";
	public static final String TC_OP_RUNNING_SAVE = "TC_OP_RUNNING_SAVE";
	public static final String TC_OP_AUTOSAVE_ENABLE = "TC_OP_AUTOSAVE_ENABLE";
	public static final String TC_OP_AUTOSAVE_DISABLE = "tc_op_autosave_disable";
	public static final String TC_OP_AUTOSAVE_GET = "TC_OP_AUTOSAVE_GET";
	public static final String TC_OP_READ_RELEASE = "TC_OP_READ_RELEASE";
	public static final String ID = "id";
	public static final String CURRENT = "current";
	public static final String DELSESS = "delsess";
	public static final String SESS_UNAME = "sess_uname";
	public static final String SESS_PASSWD= "sess_passwd";
	public static final String LOGIN_NAME ="login_name";
	public static final String SESS_TYPE = "sess_type";
	public static final String LOGIN_TIME = "login_time";
	public static final String USER_TYPE = "user_type";
	public static final String IP_ADDR = "ip_addr";
	public static final String MAC_ETH_TYPE="mac_eth_type";
	public static final String DST_IP="dst_ip";
	public static final String SRC_IP="src_ip";
	public static final String DST_IP_PREFIXLEN="dst_ip_prefixlen";
	public static final String SRC_IP_PREFIXLEN="src_ip_prefixlen";
	public static final String DST_IPV6= "dst_ipv6";
	public static final String DST_IPV6_PREFIXLEN = "dst_ipv6_prefixlen";
	public static final String SRC_IPV6= "src_ipv6";
	public static final String SRC_IPV6_PREFIXLEN = "src_ipv6_prefixlen";
	public static final String IP_PROTO = "ip_proto";
	public static final String IP_DSCP = "ip_dscp";
	public static final String L4_DST_PORT = "l4_dst_port";
	public static final String L4_DST_PORT_ENDPT = "l4_dst_port_endpt";
	public static final String L4_SRC_PORT = "l4_src_port";
	public static final String L4_SRC_PORT_ENDPT = "l4_src_port_endpt";
	public static final String ICMP_TYPE = "icmp_type";
	public static final String ICMP_CODE = "icmp_code";
	public static final String ICMPV6_TYPE = "icmpv6_type";
	public static final String ICMPV6_CODE = "icmpv6_code" ;
	public static final String SESS = "sess";
	public static final String VBRDESCRIPTION = "vbr_description";
	public static final String HOST_ADDR_PREFIXLEN = "host_addr_prefixlen";
	public static final String PORTMAP = "portmap";
	public static final int USESS_E_OK = 200;
	public static final String DETAIL = "detail";
	public static final String USESS_IPC_SESS_ID = "USESS_IPC_SESS_ID";
	public static final String VUNK_KEY = "vunk_key";
	public static final String VLAN_PRIORITY = "vlan_priority";
	public static final byte IP_TYPE_IPV4 = 0;
	public static final byte IP_TYPE_IPV6 = 1;
	public static final String PREFIXLEN = "prefixlen";
	public static final String ADMIN_STATUS = "admin_status";
	public static final String DESCRIPTION = "description";
	public static final String OPERSTATUS = "oper_status";
	public static final String CREATEDTIME = "creation_time";
	public static final String LASTUPDATETIME = "last_updated_time";
	public static final String DST_ADDR = "dst_addr";
	public static final String DST_ADDR_PREFIXLEN = "dst_addr_prefixlen";
	public static final String NWM_NAME = "nwm_name";
	public static final String NEXT_HOP_ADDR = "next_hop_addr";
	public static final String GROUP_METRIC = "group_metric";
	public static final String SWID_VALID = "swid_valid";
	public static final String TYPE = "type";
	public static final String L2DOMAINID = "l2domain_id";
	public static final String SWITCHID = "switch_id";
	public static final String VLANID = "vlan_id";
	public static final String DHCPRELAY_STATUS = "dhcprelay_status";
	public static final String VBRIDGE_NAME = "vbridge_name";
	public static final String TAGGED = "tagged";
	public static final String BYTES = "bytes";
	public static final String EXISTINGFLOW = "existingflow";
	public static final String EXPIREDFLOW = "expiredflow";
	public static final String OCTETS = "octets";
	public static final String CONTROLLER_ID = "controller_id";
	public static final String CONNECTED_VNODE_NAME = "connected_vnode_name";
	public static final String CONNECTED_IF_NAME = "connected_if_name";
	public static final String CONNECTED_VLINK_NAME = "connected_vlink_name";
	public static final String VTEPMEMBER_NAME = "vtepmember_name";
	public static final String VNODE1IFNAME = "vnode1_ifname";
	public static final String VNODE2IFNAME = "vnode2_ifname";
	public static final String VLAN_ID = "vlan_id";
	public static final String TRUE = "true";
	public static final String EXIST = "exist";
	public static final String EXPIRE = "expire";
	public static final String VTEPNAME = "vtep_name";
	public static final String CONTROLLERID="controller_id";
	public static final String NMG_NAME="nwmonitor_gr";
	public static final String STATIONID="station_id";
	public static final String MAPTYPE="maptype";
	public static final String MAPSTATUS="mapstatus";
	public static final String VBRNAME="vbr_name";
	public static final String SWITCHEID="switch_id";
	public static final String OFSCOUNT = "ofs_count";
	public static final String MAX_VLANID = "FFFF";
	public static final String VLINK_NAME = "vlink_name";
	public static final String VLAN_ID_DEFAULT_VALUE = "0xFFFF";
	public static final String BOUNDARY_NAME = "boundary_name";
	public static final String DOMAIN_NAME = "domain_name";
	public static final String CTR_KEY = "ctr_key";
	public static final String PORT_ID = "port_id";
	public static final String VAL_PORT = "port";
	public static final String TRUNK_ALLOWED_VLAN = "trunk_allowed_vlan";
	public static final String DUPLEX = "duplex";
	public static final String ALARMSTATUS = "alarmsstatus";
	public static final String LOGICAL_PORT_ID = "logical_port_id";
	public static final String SPEED = "speed";
	public static final String CONNECTED_PORT_ID = "connected_port_id";
	public static final String CONNECTED_SWITCH_ID = "connected_switch_id";
	public static final String IP_ADDRESS = "ip_address";
	public static final String USER = "user";
	public static final String ENABLE_AUDIT = "enable_audit";
	public static final String CONTROLLER_NAME1= "controller_name1";
	public static final String CONTROLLER_NAME2= "controller_name2";
	public static final String DOMAIN_NAME1= "domain_name1";
	public static final String DOMAIN_NAME2= "domain_name2";
	public static final String LOGICAL_PORT_ID1= "logical_port_id1";
	public static final String LOGICAL_PORT_ID2= "logical_port_id2";
	public static final String IPV6_ADDRESS ="ipv6_address";
	public static final String ALARM_STATUS="alarms_status";
	public static final String MAC_ADDR="mac_addr";
	public static final String MAC_ADDR_VTNSTATION="mac_addr";
	public static final String VBRIFNAME="vbrif_name";
	public static final String CREATED_TIME="created_time";
	public static final String MAP_TYPE="map_type";
	public static final String MAP_STATUS="map_status";
	public static final String VBRIFSTATUS="vbrif_status";
	public static final String IPV4_COUNT="ipv4_count";
	public static final String IPV6_COUNT="ipv6_count";
	public static final String allTxPkt="allTxPkt";
	public static final String allRxPkt="allRxPkt";
	public static final String allTxBytes="allTxBytes";
	public static final String allRxBytes="allRxBytes";
	public static final String allNWTxPkt="allNWTxPkt";
	public static final String allNWRxPkt="allNWRxPkt";
	public static final String allNWTxBytes="allNWTxBytes";
	public static final String allNWRxBytes="allNWRxBytes";
	public static final String existingTxPkt="existingTxPkt";
	public static final String existingRxPkt="existingRxPkt";
	public static final String existingTxBytes="existingTxBytes";
	public static final String existingRxBytes="existingRxBytes";
	public static final String expiredTxPkt="expiredTxPkt";
	public static final String expiredRxPkt="expiredRxPkt";
	public static final String expiredTxBytes="expiredTxBytes";
	public static final String expiredRxBytes="expiredRxBytes";
	public static final String allDropRxPkt="allDropRxPkt";
	public static final String allDropRxBytes="allDropRxBytes";
	public static final String existingDropRxPkt="existingDropRxPkt";
	public static final String existingDropRxBytes="existingDropRxBytes";
	public static final String expiredDropRxPkt="expiredDropRxPkt";
	public static final String expiredDropRxBytes="expiredDropRxBytes";
	public static final String CONTROLLER1ID="controller1_id";
	public static final String LOGICALPORT1ID="logical_port1_id";
	public static final String DOMAIN1ID="domain1_id";
	public static final String CONTROLLER2ID="controller2_id";
	public static final String LOGICALPORT2ID="logical_port2_id";
	public static final String DOMAIN2ID="domain2_id";
	public static final String SWITCH_ID1 = "switch_id1";
	public static final String SWITCH_ID2 = "switch_id2";
	public static final String PORT_ID1 = "port_id1";
	public static final String PORT_ID2 = "port_id2";
	public static final String SWITCH_VAL="switch_val";
	public static final String CONTROLLERNAME1 = "controller_name1";
	public static final String CONTROLLERNAME2 = "controller_name2";
	public static final String LOGICALPORTID1 = "logical_port_id1";
	public static final String LOGICALPORTID2 = "logical_port_id2";
	public static final String DOMAINNAME1 = "domain_name1";
	public static final String DOMAINNAME2 = "domain_name2";
	public static final String SW_KEY = "sw_key";
	public static final String OPERDOWNCRITERIA = "oper_down_criteria";
	public static final String VAL_LPORT = "val_logical_port";
	public static final String PHYPORTID = "physical_port_id";
	public static final String DOMAINID = "domain_id";
	public static final String NWM_STATUS = "nwm_status";
	public static final String TIME_STAMP = "time_stamp";
	public static final String UNCALARMIPCINFO = "unc_alarm_ipc_info";
	public static final String LINK_VAL="link";
	public static final String PORT_NUMBER = "port_number";
	public static final String HALF = "half";
	public static final String FULL = "full";
	public static final String PORT_MAC_ADDR="mac_address";
	public static final String PORT = "port";
	public static final String DOMAIN_KEY = "domain_key";
	public static final String LOGICAL_PORT = "logical_port";
	public static final String PHYSICAL_PORT_ID = "physical_port_id";
	public static final String LOGICAL_PORT_KEY = "logical_port_key";
	public static final String LPID = "lpid";
	public static final String NOLPID = "no_lpid";
	public static final String LPID_VALID ="logical_port_id_valid";
	public static final String DESTINATION = "destination";
	public static final String IFKIND = "if_kind";
	public static final String VTEPGRP_KEY = "vtepgrp_key";
	public static String NWMONITOR_GR="nwmonitor_gr";
	public static final String VTN_ALARM_STATUS = "alarm_status";
	public static int INVALID_OPEARTION_STATUS = -1;
	public static final String  USESS_USER_WEB_ADMIN = "UNC_WEB_ADMIN";
	public static final String  USESS_USER_WEB_OPER = "UNC_WEB_OPER";
	public static final String  USESS_IPC_TIMESPEC = "usess_ipc_timespec";
	public static final String  TV_SEC = "tv_sec";
}
