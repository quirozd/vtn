/*
 * Copyright (c) 2012-2013 NEC Corporation
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.vtn.javaapi.resources;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.opendaylight.vtn.core.ipc.ClientSession;
import org.opendaylight.vtn.core.ipc.IpcException;
import org.opendaylight.vtn.core.ipc.IpcStruct;
import org.opendaylight.vtn.core.util.Logger;
import org.opendaylight.vtn.javaapi.annotation.UNCVtnService;
import org.opendaylight.vtn.javaapi.constants.VtnServiceConsts;
import org.opendaylight.vtn.javaapi.constants.VtnServiceIpcConsts;
import org.opendaylight.vtn.javaapi.constants.VtnServiceJsonConsts;
import org.opendaylight.vtn.javaapi.exception.VtnServiceException;
import org.opendaylight.vtn.javaapi.ipc.conversion.IpcDataUnitWrapper;
import org.opendaylight.vtn.javaapi.ipc.enums.UncCommonEnum;
import org.opendaylight.vtn.javaapi.ipc.enums.UncCommonEnum.UncResultCode;
import org.opendaylight.vtn.javaapi.ipc.enums.UncIpcErrorCode;
import org.opendaylight.vtn.javaapi.ipc.enums.UncJavaAPIErrorCode;
import org.opendaylight.vtn.javaapi.ipc.enums.UncSessionEnums;
import org.opendaylight.vtn.javaapi.ipc.enums.UncStructEnum;
import org.opendaylight.vtn.javaapi.validation.SessionResourceValidator;

/**
 * The Class SessionsResource implements post and get methods of Session API.
 * 
 */

@UNCVtnService(path = "/sessions")
public class SessionsResource extends AbstractResource {

	private static final Logger LOG = Logger.getLogger(SessionsResource.class
			.getName());

	/**
	 * Instantiates a new sessions resource.
	 */
	public SessionsResource() {
		super();
		LOG.trace("Start SessionsResource#SessionsResource()");
		setValidator(new SessionResourceValidator(this));
		LOG.trace("Complete SessionsResource#SessionsResource()");
	}

	/**
	 * Implementation of post method of session API
	 * 
	 * @param requestBody
	 *            the request JsonObject
	 * 
	 * @return Error code
	 * @throws VtnServiceException
	 *             the vtn service exception
	 */
	@Override
	public int post(final JsonObject requestBody) throws VtnServiceException {
		LOG.trace("Starts SessionsResource#post()");
		ClientSession session = null;
		int status = ClientSession.RESP_FATAL;
		try {
			LOG.debug("Start Ipc framework call");
			session = getConnPool().getSession(
					UncSessionEnums.UNCD_IPC_CHANNEL,
					UncSessionEnums.UNCD_IPC_SERVICE,
					UncSessionEnums.ServiceID.kUsessSessAdd.ordinal(),
					getExceptionHandler());
			LOG.info("Session created successfully");
			// create request packet for IPC call based on API key and
			// JsonObject
			final IpcStruct usessIpcReqSessAdd = new IpcStruct(
					UncStructEnum.UsessIpcReqSessAdd.getValue());
			if (requestBody != null
					&& requestBody.has(VtnServiceJsonConsts.SESSION)) {
				final JsonObject sessionJson = requestBody
						.getAsJsonObject(VtnServiceJsonConsts.SESSION);
				// add user name to usess_ipc_req_sess_add structure
				if (sessionJson.has(VtnServiceJsonConsts.USERNAME)
						&& sessionJson
								.getAsJsonPrimitive(
										VtnServiceJsonConsts.USERNAME)
								.getAsString()
								.equalsIgnoreCase(VtnServiceJsonConsts.ADMIN)) {
					usessIpcReqSessAdd
							.set(VtnServiceIpcConsts.SESS_UNAME,
									IpcDataUnitWrapper
											.setIpcUint8ArrayValue(VtnServiceIpcConsts.USESS_USER_WEB_ADMIN));
					LOG.debug("Login from admin user"
							+ sessionJson.getAsJsonPrimitive(
									VtnServiceJsonConsts.USERNAME).toString());
				} else {
					usessIpcReqSessAdd
							.set(VtnServiceIpcConsts.SESS_UNAME,
									IpcDataUnitWrapper
											.setIpcUint8ArrayValue(VtnServiceIpcConsts.USESS_USER_WEB_OPER));
					LOG.debug("Login from oper user"
							+ sessionJson.getAsJsonPrimitive(
									VtnServiceJsonConsts.USERNAME).toString());
				}
				LOG.debug("sess_uname: "
						+ IpcDataUnitWrapper.getIpcStructUint8ArrayValue(
								usessIpcReqSessAdd,
								VtnServiceIpcConsts.SESS_UNAME));
				// add password to usess_ipc_req_sess_add structure
				usessIpcReqSessAdd.set(
						VtnServiceIpcConsts.SESS_PASSWD,
						IpcDataUnitWrapper.setIpcUint8ArrayValue(sessionJson
								.getAsJsonPrimitive(
										VtnServiceJsonConsts.PASSWORD)
								.getAsString().trim()));

				// add login name to usess_ipc_req_sess_add structure
				if (sessionJson.has(VtnServiceJsonConsts.LOGINNAME)
						&& sessionJson.getAsJsonPrimitive(
								VtnServiceJsonConsts.LOGINNAME).getAsString() != null) {
					usessIpcReqSessAdd
							.set(VtnServiceIpcConsts.LOGIN_NAME,
									IpcDataUnitWrapper
											.setIpcUint8ArrayValue(sessionJson
													.getAsJsonPrimitive(
															VtnServiceJsonConsts.LOGINNAME)
													.getAsString().trim()));
				} else {
					usessIpcReqSessAdd
							.set(VtnServiceIpcConsts.LOGIN_NAME,
									IpcDataUnitWrapper
											.setIpcUint8ArrayValue(VtnServiceJsonConsts.NONE));
				}
				// add type to usess_ipc_req_sess_add structure
				if (sessionJson.has(VtnServiceJsonConsts.TYPE)
						&& sessionJson.getAsJsonPrimitive(
								VtnServiceJsonConsts.TYPE).getAsString() != null) {
					final String type = sessionJson
							.getAsJsonPrimitive(VtnServiceJsonConsts.TYPE)
							.getAsString().trim();
					if (VtnServiceJsonConsts.WEBAPI.equalsIgnoreCase(type)) {
						usessIpcReqSessAdd
								.set(VtnServiceIpcConsts.SESS_TYPE,
										IpcDataUnitWrapper
												.setIpcInt32Value(UncSessionEnums.UsessTypeE.USESS_TYPE_WEB_API
														.ordinal()));
					} else {
						usessIpcReqSessAdd
								.set(VtnServiceIpcConsts.SESS_TYPE,
										IpcDataUnitWrapper
												.setIpcInt32Value(UncSessionEnums.UsessTypeE.USESS_TYPE_WEB_UI
														.ordinal()));
					}
				} else {
					usessIpcReqSessAdd
							.set(VtnServiceIpcConsts.SESS_TYPE,
									IpcDataUnitWrapper
											.setIpcInt32Value(UncSessionEnums.UsessTypeE.USESS_TYPE_WEB_UI
													.ordinal()));
				}

				// add ip address to usess_ipc_req_sess_add structure
				usessIpcReqSessAdd.set(VtnServiceJsonConsts.IPADDR,
						IpcDataUnitWrapper
								.setIpcInet4AddressValue(sessionJson
										.getAsJsonPrimitive(
												VtnServiceJsonConsts.IPADDR)
										.getAsString()));

				// add info to usess_ipc_req_sess_add structure
				if (sessionJson.has(VtnServiceJsonConsts.INFO)
						&& sessionJson.getAsJsonPrimitive(
								VtnServiceJsonConsts.INFO).getAsString() != null) {
					usessIpcReqSessAdd.set(VtnServiceJsonConsts.INFO,
							IpcDataUnitWrapper
									.setIpcUint8ArrayValue(sessionJson
											.getAsJsonPrimitive(
													VtnServiceJsonConsts.INFO)
											.getAsString().trim()));
				} else {
					usessIpcReqSessAdd
							.set(VtnServiceJsonConsts.INFO,
									IpcDataUnitWrapper
											.setIpcUint8ArrayValue(VtnServiceJsonConsts.NONE));
				}
				session.addOutput(usessIpcReqSessAdd);
				LOG.info("Request packet created successfully");
				status = session.invoke();
				LOG.info("Request packet processed with status:"+status);
				if (status != UncSessionEnums.UsessIpcErrE.USESS_E_OK.ordinal()) {
					LOG.info("Error occurred while performing operation");
					createErrorInfo(
							UncCommonEnum.UncResultCode.UNC_SERVER_ERROR
									.getValue(),
							UncIpcErrorCode.getSessionCodes(status));
					status = UncResultCode.UNC_SERVER_ERROR.getValue();
				} else {
					LOG.info("Opeartion successfully performed");
					final JsonObject response = new JsonObject();
					final IpcStruct responseStruct = (IpcStruct) session
							.getResponse(0);
					final JsonObject sessionInfo = new JsonObject();
					sessionInfo.addProperty(
							VtnServiceJsonConsts.SESSIONID,
							IpcDataUnitWrapper.getIpcStructUint32Value(
									responseStruct, VtnServiceIpcConsts.ID)
									.toString());
					response.add(VtnServiceJsonConsts.SESSION, sessionInfo);
					setInfo(response);
					status = UncResultCode.UNC_SUCCESS.getValue();
				}
			}
			LOG.debug("Complete Ipc framework call");
		} catch (final VtnServiceException e) {
			getExceptionHandler()
					.raise(Thread.currentThread().getStackTrace()[1]
							.getClassName()
							+ VtnServiceConsts.HYPHEN
							+ Thread.currentThread().getStackTrace()[1]
									.getMethodName(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR.getErrorCode(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR
									.getErrorMessage(), e);
			throw e;
		} catch (final IpcException e) {
			LOG.info("Error occured while performing addOutput operation");
			getExceptionHandler()
					.raise(Thread.currentThread().getStackTrace()[1]
							.getClassName()
							+ VtnServiceConsts.HYPHEN
							+ Thread.currentThread().getStackTrace()[1]
									.getMethodName(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR.getErrorCode(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR
									.getErrorMessage(), e);

		} finally {
			if (status == ClientSession.RESP_FATAL) {
				createErrorInfo(UncCommonEnum.UncResultCode.UNC_SERVER_ERROR
						.getValue());
				status = UncResultCode.UNC_SERVER_ERROR.getValue();
			}
			// destroy session by common handler
			getConnPool().destroySession(session);
		}
		LOG.trace("Completed SessionsResource#post()");
		return status;
	}

	/**
	 * Implementation of Get method of session API
	 * 
	 * @param requestBody
	 *            the request JsonObject
	 * 
	 * @return Error code
	 * @throws VtnServiceException
	 *             the vtn service exception
	 */
	@Override
	public int get(final JsonObject requestBody) throws VtnServiceException {
		LOG.trace("starts SessionsResource#get()");
		ClientSession session = null;
		int status = ClientSession.RESP_FATAL;
		try {
			LOG.debug("Start Ipc framework call");
			if (requestBody != null
					&& requestBody.getAsJsonPrimitive(VtnServiceJsonConsts.OP)
							.getAsString()
							.equalsIgnoreCase(VtnServiceJsonConsts.COUNT)) {
				session = getConnPool().getSession(
						UncSessionEnums.UNCD_IPC_CHANNEL,
						UncSessionEnums.UNCD_IPC_SERVICE,
						UncSessionEnums.ServiceID.kUsessSessCount.ordinal(),
						getExceptionHandler());
			} else {
				session = getConnPool().getSession(
						UncSessionEnums.UNCD_IPC_CHANNEL,
						UncSessionEnums.UNCD_IPC_SERVICE,
						UncSessionEnums.ServiceID.kUsessSessList.ordinal(),
						getExceptionHandler());
			}
			LOG.info("Session created successfully");
			// create request packet for IPC call based on API key and
			// JsonObject
			final IpcStruct usessIpcReqSessId = new IpcStruct(
					UncStructEnum.UsessIpcSessId.getValue());
			usessIpcReqSessId.set(VtnServiceJsonConsts.ID, IpcDataUnitWrapper
					.setIpcUint32Value(String.valueOf(getSessionID())));
			session.addOutput(usessIpcReqSessId);
			LOG.info("Request packet created successfully");
			status = session.invoke();
			LOG.info("Request packet processed with status:"+status);
			if (status != UncSessionEnums.UsessIpcErrE.USESS_E_OK.ordinal()) {
				LOG.info("Error occurred while performing operation");
				createErrorInfo(
						UncCommonEnum.UncResultCode.UNC_SERVER_ERROR.getValue(),
						UncIpcErrorCode.getSessionCodes(status));
				status = UncResultCode.UNC_SERVER_ERROR.getValue();
			} else {
				LOG.info("Opeartion successfully performed");
				final JsonObject response = new JsonObject();

				String opType = VtnServiceJsonConsts.NORMAL;
				if (requestBody.has(VtnServiceJsonConsts.OP)) {
					opType = requestBody.get(VtnServiceJsonConsts.OP)
							.getAsString();
				}
				if (opType.equalsIgnoreCase(VtnServiceJsonConsts.COUNT)) {
					/*
					 * Create Json for Count
					 */
					JsonObject sessJson = null;
					sessJson = new JsonObject();
					sessJson.addProperty(VtnServiceJsonConsts.COUNT,
							IpcDataUnitWrapper.getIpcDataUnitValue(session
									.getResponse(0)));
					response.add(VtnServiceJsonConsts.SESSIONS, sessJson);
				} else {
					createGetResponse(session, response, opType);
				}
				setInfo(response);
				status = UncResultCode.UNC_SUCCESS.getValue();
			}
			LOG.debug("Complete Ipc framework call");
		} catch (final VtnServiceException e) {
			getExceptionHandler()
					.raise(Thread.currentThread().getStackTrace()[1]
							.getClassName()
							+ VtnServiceConsts.HYPHEN
							+ Thread.currentThread().getStackTrace()[1]
									.getMethodName(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR.getErrorCode(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR
									.getErrorMessage(), e);
			throw e;
		} catch (final IpcException e) {
			LOG.info("Error occured while performing addOutput operation");
			getExceptionHandler()
					.raise(Thread.currentThread().getStackTrace()[1]
							.getClassName()
							+ VtnServiceConsts.HYPHEN
							+ Thread.currentThread().getStackTrace()[1]
									.getMethodName(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR.getErrorCode(),
							UncJavaAPIErrorCode.IPC_SERVER_ERROR
									.getErrorMessage(), e);

		} finally {
			if (status == ClientSession.RESP_FATAL) {
				createErrorInfo(UncCommonEnum.UncResultCode.UNC_SERVER_ERROR
						.getValue());
				status = UncResultCode.UNC_SERVER_ERROR.getValue();
			}
			// destroy session by common handler
			getConnPool().destroySession(session);
		}
		LOG.trace("Completed SessionsResource#delete()");
		return status;
	}

	/**
	 * Creates the response Json for API user
	 * 
	 * @param session
	 *            session object created
	 * @param response
	 *            response Json Object
	 * @param opType
	 *            operation type
	 * 
	 * @throws IpcException
	 */
	private void createGetResponse(final ClientSession session,
			final JsonObject response, final String opType) throws IpcException {
		LOG.trace("Starts SessionsResource#createGetResponse()");
		final JsonArray sessArray = new JsonArray();
		final int count = session.getResponseCount();
		LOG.debug("Operation:" + opType);
		for (int i = VtnServiceJsonConsts.VAL_0; i < count; i++) {
			final IpcStruct responseStruct = (IpcStruct) session.getResponse(i);
			LOG.info("Response:" + responseStruct.toString());
			JsonObject sessJson = null;
			sessJson = new JsonObject();
			final IpcStruct ipcResponseStructId = responseStruct
					.getInner(VtnServiceIpcConsts.SESS);
			LOG.debug("user_type:" + responseStruct.get("user_type"));
			// LOG.debug("sess_uname:" + responseStruct.get("sess_uname"));
			// add session id to response json
			sessJson.addProperty(
					VtnServiceJsonConsts.SESSIONID,
					IpcDataUnitWrapper.getIpcStructUint32Value(
							ipcResponseStructId, VtnServiceIpcConsts.ID)
							.toString());
			LOG.debug("session_id:"
					+ IpcDataUnitWrapper.getIpcStructUint32Value(
							ipcResponseStructId, VtnServiceIpcConsts.ID));

			if (opType.equalsIgnoreCase(VtnServiceJsonConsts.DETAIL)) {
				LOG.debug("Case : detail");
				// add type to response json
				if (IpcDataUnitWrapper
						.getIpcStructUint32Value(responseStruct,
								VtnServiceIpcConsts.SESS_TYPE)
						.toString()
						.equals(UncSessionEnums.UsessTypeE.USESS_TYPE_WEB_API
								.getValue())) {
					sessJson.addProperty(VtnServiceJsonConsts.TYPE,
							VtnServiceJsonConsts.WEBAPI);
				} else if (IpcDataUnitWrapper
						.getIpcStructUint32Value(responseStruct,
								VtnServiceIpcConsts.SESS_TYPE)
						.toString()
						.equals(UncSessionEnums.UsessTypeE.USESS_TYPE_WEB_UI
								.getValue())) {
					sessJson.addProperty(VtnServiceJsonConsts.TYPE,
							VtnServiceJsonConsts.WEBUI);
				}
				LOG.debug("type:"
						+ IpcDataUnitWrapper.getIpcStructUint32Value(
								responseStruct, VtnServiceIpcConsts.SESS_TYPE));
				// add user name to response json
				final String userName = IpcDataUnitWrapper
						.getIpcStructUint8ArrayValue(responseStruct,
								VtnServiceIpcConsts.SESS_UNAME);
				LOG.debug("user_name:" + userName);
				if (VtnServiceIpcConsts.USESS_USER_WEB_ADMIN.equals(userName)) {
					sessJson.addProperty(VtnServiceJsonConsts.USERNAME,
							VtnServiceJsonConsts.ADMIN);
				} else if (VtnServiceIpcConsts.USESS_USER_WEB_OPER
						.equals(userName)) {
					sessJson.addProperty(VtnServiceJsonConsts.USERNAME,
							VtnServiceJsonConsts.OPER);
				}
				// add user type to response json
				if (IpcDataUnitWrapper
						.getIpcStructUint32Value(responseStruct,
								VtnServiceIpcConsts.USER_TYPE)
						.toString()
						.equals(UncSessionEnums.UserTypeE.USER_TYPE_ADMIN
								.getValue())) {
					sessJson.addProperty(VtnServiceJsonConsts.USERTYPE,
							VtnServiceJsonConsts.ADMIN);
				} else if (IpcDataUnitWrapper
						.getIpcStructUint32Value(responseStruct,
								VtnServiceIpcConsts.USER_TYPE)
						.toString()
						.equals(UncSessionEnums.UserTypeE.USER_TYPE_OPER
								.getValue())) {
					sessJson.addProperty(VtnServiceJsonConsts.USERTYPE,
							VtnServiceJsonConsts.OPER);
				}
				LOG.debug("usertype:"
						+ IpcDataUnitWrapper.getIpcStructUint32Value(
								responseStruct, VtnServiceIpcConsts.USER_TYPE));
				// add ipaddress to response json
				sessJson.addProperty(
						VtnServiceJsonConsts.IPADDR,
						IpcDataUnitWrapper.getIpcStructIpv4Value(
								responseStruct, VtnServiceJsonConsts.IPADDR)
								.toString());
				LOG.debug("ipaddr:"
						+ IpcDataUnitWrapper.getIpcStructIpv4Value(
								responseStruct, VtnServiceJsonConsts.IPADDR));
				// add login name to response json
				sessJson.addProperty(
						VtnServiceJsonConsts.LOGIN_NAME,
						IpcDataUnitWrapper
								.getIpcStructUint8ArrayValue(responseStruct,
										VtnServiceJsonConsts.LOGIN_NAME)
								.toString());
				LOG.debug("login_name:"
						+ IpcDataUnitWrapper
								.getIpcStructUint8ArrayValue(responseStruct,
										VtnServiceJsonConsts.LOGIN_NAME)
								.toString());
				// add login time to response json
				final IpcStruct ipcResponseTimeStruct = responseStruct
						.getInner(VtnServiceIpcConsts.LOGIN_TIME);
				sessJson.addProperty(
						VtnServiceJsonConsts.LOGIN_TIME,
						IpcDataUnitWrapper.getIpcStructInt64Value(
								ipcResponseTimeStruct,
								VtnServiceIpcConsts.TV_SEC).toString());
				LOG.debug("login_time:"
						+ IpcDataUnitWrapper.getIpcStructInt64Value(
								ipcResponseTimeStruct,
								VtnServiceIpcConsts.TV_SEC));
				// add info to response json
				sessJson.addProperty(
						VtnServiceJsonConsts.INFO,
						IpcDataUnitWrapper.getIpcStructUint8ArrayValue(
								responseStruct, VtnServiceJsonConsts.INFO)
								.toString());
				LOG.debug("info:"
						+ IpcDataUnitWrapper.getIpcStructUint8ArrayValue(
								responseStruct, VtnServiceJsonConsts.INFO));
			}
			sessArray.add(sessJson);
		}
		response.add(VtnServiceJsonConsts.SESSIONS, sessArray);
		LOG.trace("Completed SessionsResource#createGetResponse()");
	}

}