/*
 * Copyright (c) 2012-2013 NEC Corporation
 * All rights reserved.
 * 
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 */

/*
 *  @brief   ODBC Manager
 *  @file    odbcm_bind_controller.cc
 */

#include <stdio.h>
#include <sstream>
#include "odbcm_db_varbind.hh"
#include "odbcm_common.hh"
#include "odbcm_utils.hh"
#include "odbcm_db_tableschema.hh"

using unc::uppl::DBVarbind;
/**
 * @Description : Input binding function for controller_common_table
 * @param[in]   : vector<TableAttrSchema> &column_attr, HSTMT &r_hstmt
 * @return      : ODBCM_RC_STATUS
 **/
ODBCM_RC_STATUS DBVarbind::bind_controller_table_input(
    std::vector<TableAttrSchema> &column_attr/*DBTableSchema->rowlist_ entry*/,
     HSTMT &r_hstmt/**statement handler which carries the SQL Query*/) {
  SQLRETURN odbc_rc = SQL_SUCCESS;  // odbc API's return code initialize with 0
  SQLUSMALLINT col_no = 0;  //   column number
  /**Vector iterator to take the TableAttrSchema structures*/
  std::vector< TableAttrSchema >::iterator i;
  /**Flag to decide printing logs */
  uint8_t log_flag = 1;

  /**Loop for iterate all the elements in the vector, the TableAttrSchema
  * table_attribute_name value will be compared and corresponding
  * structure member will be binded here*/
  for (i = column_attr.begin(); i != column_attr.end(); ++i) {
    /*In case of bind failure return the parameter bind error */
    if (odbc_rc == SQL_ERROR||odbc_rc == SQL_INVALID_HANDLE) {
      pfc_log_error("ODBCM::DBVarbind::bind_controller_table_input: "
          "Error in binding");
      /**in case of error while binding return to application caller with the
       * error code */
      return ODBCM_RC_PARAM_BIND_ERROR;
    }
    ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
      DATATYPE_UINT8_ARRAY_32/**data type char(32)*/,
      CTR_NAME/*attribute name MACRO*/) {
  /**binding structure buffer member for controller name input,
 
  * column size is ODBCM_SIZE_32,
  * Data type CHAR[32],
  * and buffer size will passed as length of value */
    odbc_rc = BindInputParameter_SQL_VARCHAR(
      r_hstmt/**sql statement handler*/,
      ++col_no/*parameter number (sequential order)*/,
      ODBCM_SIZE_32/*column size in DB table*/,
      0/**decimal point */,
      p_ctr_table->szcontroller_name/*buffer to carry values*/,
      sizeof(p_ctr_table->szcontroller_name)-1/**buffer length*/,
      NULL/**strlen or NULL*/);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16/**data type char(16)*/,
        CTR_TYPE/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_SMALLINT(
      r_hstmt,
      ++col_no,
      0,
      0,
      reinterpret_cast<SQLSMALLINT*> (&p_ctr_table->stype),
      0,
      NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32 /**data type char(32)*/,
        CTR_VERSION/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_VARCHAR(
        r_hstmt,
        ++col_no,
        ODBCM_SIZE_32,
        0,
        p_ctr_table->szversion,
        sizeof(p_ctr_table->szversion)-1,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_128/**data type char(128)*/,
        CTR_DESCRIPTION/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_VARCHAR(
        r_hstmt,
        ++col_no,
        ODBCM_SIZE_128+1,
        0,
        p_ctr_table->szdescription,
        sizeof(p_ctr_table->szdescription),
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_IPV4 /**data type ipv4*/,
        CTR_IP_ADDRESS/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_BIGINT(
        r_hstmt,
        ++col_no,
        0,
        0,
        reinterpret_cast<SQLBIGINT*>(&p_ctr_table->szip_address),
        0,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32/**data type char(32)*/,
        CTR_USER_NAME/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_VARCHAR(
        r_hstmt,
        ++col_no,
        ODBCM_SIZE_32,
        0,
        p_ctr_table->szuser_name,
        sizeof(p_ctr_table->szuser_name)-1,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_257/**data type char(257)*/,
        CTR_PASSWORD/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_VARCHAR(
        r_hstmt,
        ++col_no,
        ODBCM_SIZE_257,
        0,
        p_ctr_table->szpassword,
        sizeof(p_ctr_table->szpassword)-1,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16/**data type char(16)*/,
        CTR_ENABLE_AUDIT/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_SMALLINT(
        r_hstmt,
        ++col_no,
        0,
        0,
        reinterpret_cast<SQLSMALLINT*>(&p_ctr_table->senable_audit),
        0,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32/**data type char(32)*/,
        CTR_ACTUAL_VERSION/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_VARCHAR(
        r_hstmt,
        ++col_no,
        ODBCM_SIZE_32,
        0,
        p_ctr_table->szactual_version,
        sizeof(p_ctr_table->szactual_version)-1,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16/**data type unit16_t*/,
        CTR_OPER_STATUS/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_SMALLINT(
        r_hstmt,
        ++col_no,
        0,
        0,
        reinterpret_cast<SQLSMALLINT*>(&p_ctr_table->soper_status),
        0,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_9/**data type char(9)*/,
        CTR_VALID/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_CHAR(
        r_hstmt,
        ++col_no,
        ODBCM_SIZE_9,
        0,
        p_ctr_table->svalid,
        sizeof(p_ctr_table->svalid)-1,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16/**data type unit16_t*/,
        CTR_CS_ROW_STATUS/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_SMALLINT(
        r_hstmt,
        ++col_no,
        0,
        0,
        reinterpret_cast<SQLSMALLINT*>(&p_ctr_table->scs_row_status),
        0,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_9/**data type char(9)*/,
        CTR_CS_ATTR/*attribute name MACRO*/) {
      odbc_rc = BindInputParameter_SQL_CHAR(
        r_hstmt,
        ++col_no,
        ODBCM_SIZE_9,
        0,
        p_ctr_table->scs_attr,
        sizeof(p_ctr_table->scs_attr)-1,
        NULL);
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    }
    if ((*i).p_table_attribute_value != NULL && log_flag == 0) {
      if (odbc_rc != SQL_SUCCESS)
        ODBCMUtils::OdbcmHandleDiagnosticsPrint(SQL_HANDLE_STMT, r_hstmt);
      /**reset flag value 1 */
      log_flag = 1;
    } else {
      pfc_log_info("ODBCM::DBVarbind::**NO bind**i/p:CTR_TABLE:%s:datatype=%d:"
          , ((*i).table_attribute_name).c_str(),
          (*i).request_attribute_type);
    }
  }  // for loop end
  return ODBCM_RC_SUCCESS;
}
/**
 * @Description : Output binding function for controller_common_table
 * @param[in]   : vector<TableAttrSchema> &column_attr, HSTMT &r_hstmt
 * @return      : ODBCM_RC_STATUS
 **/
ODBCM_RC_STATUS DBVarbind::bind_controller_table_output(
    std::vector<TableAttrSchema> &column_attr/*DBTableSchema->rowlist_ entry*/,
    HSTMT &r_hstmt/**statement handler which carries the SQL Query*/) {
  SQLRETURN odbc_rc = SQL_SUCCESS;  // odbc APIs return code
  SQLINTEGER  indptr = 0;  // Pointer to value that indicates the number of
                          // bytes available to return
  uint16_t col_no = 0;  // column number
  /**Flag to decide printing logs */
  uint8_t log_flag = 1;
  /**Vector iterator to take the TableAttrSchema structures*/
  std::vector< TableAttrSchema >::iterator i;

  /**Loop for iterate all the elements in the vector, the TableAttrSchema
 * table_attribute_name value will be compared and corresponding
 * structure member will be binded here*/
  for (i = column_attr.begin(); i != column_attr.end(); ++i) {
    /*In case of bind failure return the parameter bind error */
    if (odbc_rc == SQL_ERROR||odbc_rc == SQL_INVALID_HANDLE) {
      pfc_log_error("ODBCM::DBVarbind::bind_controller_com_table_output"
          "bind parameter error");
      /**in case of error while binding return to application caller with the
      * error code */
      return ODBCM_RC_PARAM_BIND_ERROR;
    }
    /**binding structure buffer member for controller name output,
     * column size is ODBCM_SIZE_32,
     * Data type CHAR[32],
     * and buffer size will passed as length of value,
     * ptr to indicates available no. of bytes return */
    ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
      DATATYPE_UINT8_ARRAY_32,
      CTR_NAME/*attribute name MACRO*/) {
    odbc_rc = BindCol_SQL_VARCHAR(
      r_hstmt/**sql statement handler*/,
      ++col_no/*parameter number (sequential order)*/,
      p_ctr_table->szcontroller_name/*buffer to fetch values*/,
      ODBCM_SIZE_32+1,
      /**no.of bytes available to return*/
      reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16,
        CTR_TYPE/*attribute name MACRO*/) {
      odbc_rc = BindCol_SQL_SMALLINT(
        r_hstmt,
        ++col_no,
        reinterpret_cast<SQLSMALLINT*>(&p_ctr_table->stype),
        sizeof(SQLSMALLINT),
        reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    }  ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT8_ARRAY_32,
          CTR_VERSION/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_VARCHAR(
          r_hstmt,
          ++col_no,
          p_ctr_table->szversion,
          ODBCM_SIZE_32+1,
          reinterpret_cast<SQLLEN*>(&indptr));
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i/**TableAttrSchema structure*/),
          DATATYPE_UINT8_ARRAY_128,
          CTR_DESCRIPTION/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_VARCHAR(
            r_hstmt,
            ++col_no,
            p_ctr_table->szdescription,
            ODBCM_SIZE_128+1,
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_IPV4,
          CTR_IP_ADDRESS/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_BIGINT(
            r_hstmt,
            ++col_no,
            reinterpret_cast<SQLBIGINT*>(&p_ctr_table->szip_address),
            sizeof(SQLBIGINT),
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT8_ARRAY_32,
          CTR_USER_NAME/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_VARCHAR(
            r_hstmt,
            ++col_no,
            p_ctr_table->szuser_name,
            ODBCM_SIZE_32+1,
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT8_ARRAY_257,
          CTR_PASSWORD/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_VARCHAR(
            r_hstmt,
            ++col_no,
            p_ctr_table->szpassword,
            ODBCM_SIZE_257+1,
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT16,
          CTR_ENABLE_AUDIT/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_SMALLINT(
            r_hstmt,
            ++col_no,
            reinterpret_cast<SQLSMALLINT*>(&p_ctr_table->senable_audit),
            sizeof(SQLSMALLINT),
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT8_ARRAY_32,
          CTR_ACTUAL_VERSION/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_VARCHAR(
            r_hstmt,
            ++col_no,
            p_ctr_table->szactual_version,
            ODBCM_SIZE_32+1,
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT16,
          CTR_OPER_STATUS/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_SMALLINT(
            r_hstmt,
            ++col_no,
            reinterpret_cast<SQLSMALLINT*>(&p_ctr_table->soper_status),
            sizeof(SQLSMALLINT),
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT8_ARRAY_9,
          CTR_VALID/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_VARCHAR(
            r_hstmt,
            ++col_no,
            p_ctr_table->svalid,
            ODBCM_SIZE_9+1,
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT16,
          CTR_CS_ROW_STATUS/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_SMALLINT(
            r_hstmt,
            ++col_no,
            reinterpret_cast<SQLSMALLINT*>(&p_ctr_table->scs_row_status),
            sizeof(SQLSMALLINT),
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT8_ARRAY_9,
          CTR_CS_ATTR/*attribute name MACRO*/) {
        odbc_rc = BindCol_SQL_VARCHAR(
            r_hstmt,
            ++col_no,
            p_ctr_table->scs_attr,
            ODBCM_SIZE_9+1,
            reinterpret_cast<SQLLEN*>(&indptr));
      /**set flag value 0 to print column binding details */
      log_flag = 0;
    }
    if (log_flag == 0) {
      /**reset flag value 1*/
      log_flag = 1;
    }
  }  // for loop end
  return ODBCM_RC_SUCCESS;
}

/**
 * @Description : To fill the controller_common_table
 * values into bind controller_common_table_ structure.
 * @param[in]   : vector<TableAttrSchema> &column_attr
 * @return      : ODBCM_RC_STATUS
 **/
ODBCM_RC_STATUS DBVarbind::fill_controller_table(
    std::vector<TableAttrSchema> &column_attr
    /*DBTableSchema->rowlist_ entry*/) {
  /**Vector iterator to take the TableAttrSchema structures*/
  std::vector< TableAttrSchema >::iterator i;
  /**Loop for iterate all the elements in the vector, the TableAttrSchema
    * table_attribute_name value will be compared and corresponding
    * structure member will be filled*/
  for (i = column_attr.begin(); i != column_attr.end(); ++i) {
    ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
      DATATYPE_UINT8_ARRAY_32/**data type char(32)*/,
      CTR_NAME/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for controller_name CHAR[32]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_32]> cn_value =
        *((ColumnAttrValue <uint8_t[ODBCM_SIZE_32]>*)
            ((*i).p_table_attribute_value));
      /**clear the allocated buffer memory to receive the controller_name
        * from caller*/ 
      ODBCM_MEMSET(p_ctr_table->szcontroller_name, 0, ODBCM_SIZE_32+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(
          p_ctr_table->szcontroller_name,
          &cn_value.value,
          (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "szController_name = %s",
           p_ctr_table->szcontroller_name);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16,
        CTR_TYPE/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for type uint8*/
      ColumnAttrValue <uint16_t> type_value =
        *((ColumnAttrValue <uint16_t>*)
          ((*i).p_table_attribute_value));
      ODBCM_MEMSET(&p_ctr_table->stype, 0, sizeof(SQLSMALLINT));
      p_ctr_table->stype = type_value.value;
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: sType= %d",
          p_ctr_table->stype);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32,
        CTR_VERSION/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for version CHAR[32]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_32]> ver_value =
        *((ColumnAttrValue <uint8_t[ODBCM_SIZE_32]>*)
            ((*i).p_table_attribute_value));
      ODBCM_MEMSET(p_ctr_table->szversion, 0, ODBCM_SIZE_32+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(p_ctr_table->szversion,
          &ver_value.value, (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "szversion = %s", p_ctr_table->szversion);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_128,
        CTR_DESCRIPTION/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for description CHAR[128]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_128]> desc_value =
        *((ColumnAttrValue <uint8_t[128]>*)
          ((*i).p_table_attribute_value));
      ODBCM_MEMSET(p_ctr_table->szdescription, 0, ODBCM_SIZE_128+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(p_ctr_table->szdescription, &desc_value.value,
          (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "szDescription = %s", p_ctr_table->szdescription);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_IPV4,
        CTR_IP_ADDRESS/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for ip_address SQLBIGINT*/
      ColumnAttrValue <uint32_t> ipv4_value =
        *((ColumnAttrValue <uint32_t>*)((*i).p_table_attribute_value));

      ODBCM_MEMSET(&p_ctr_table->szip_address, 0, sizeof(SQLBIGINT));
      p_ctr_table->szip_address = ipv4_value.value;
/*
      struct sockaddr_in IPV4;
      IPV4.sin_addr.s_addr = inet_addr("10.0.0.1");
      p_ctr_table->szip_address = IPV4.sin_addr.s_addr;
*/
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: szIp_address = %s",
          ODBCMUtils::get_ip_string(p_ctr_table->szip_address).c_str());
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32,
        CTR_USER_NAME/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for user_name CHAR[32]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_32]> user_value =
        *((ColumnAttrValue <uint8_t[32]>*)
                    ((*i).p_table_attribute_value));
      ODBCM_MEMSET(p_ctr_table->szuser_name, 0, ODBCM_SIZE_32+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(p_ctr_table->szuser_name, &user_value.value,
          (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "szUser_name = %s", p_ctr_table->szuser_name);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_257,
        CTR_PASSWORD/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for password CHAR[256]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_257]> pass_value =
        *((ColumnAttrValue <uint8_t[ODBCM_SIZE_257]>*)
            ((*i).p_table_attribute_value));
      ODBCM_MEMSET(p_ctr_table->szpassword, 0, ODBCM_SIZE_257+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(p_ctr_table->szpassword, &pass_value.value,
          (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "szPassword = %s", p_ctr_table->szpassword);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16,
        CTR_ENABLE_AUDIT/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for enable_audit unit16_t*/
      ColumnAttrValue <uint16_t> audit_value =
        *((ColumnAttrValue <uint16_t>*)
        ((*i).p_table_attribute_value));
      ODBCM_MEMSET(&p_ctr_table->senable_audit, 0, sizeof(SQLSMALLINT));
      p_ctr_table->senable_audit = audit_value.value;
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "enable_audit = %d", p_ctr_table->senable_audit);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32,
        CTR_ACTUAL_VERSION/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for actual_version CHAR[32]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_32]> actual_version_value =
        *((ColumnAttrValue <uint8_t[32]>*)
            ((*i).p_table_attribute_value));
      ODBCM_MEMSET(p_ctr_table->szactual_version, 0, ODBCM_SIZE_32+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(p_ctr_table->szactual_version, &actual_version_value.value,
          (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "szactual_version = %s", p_ctr_table->szactual_version);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT16,
          CTR_OPER_STATUS/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for oper_status unit16_t*/
      ColumnAttrValue <uint16_t> oper_value =
        *((ColumnAttrValue <uint16_t>*)
          ((*i).p_table_attribute_value));
      ODBCM_MEMSET(&p_ctr_table->soper_status, 0, sizeof(SQLSMALLINT));
      /**copying the value from template to binded buffer */
      p_ctr_table->soper_status = oper_value.value;
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "soper_status = %d", p_ctr_table->soper_status);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_9,
        CTR_VALID/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for valid CHAR[9]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_9]> valid_value =
          *((ColumnAttrValue <uint8_t[ODBCM_SIZE_9]>*)
            ((*i).p_table_attribute_value));
      ODBCM_MEMSET(p_ctr_table->svalid, 0, ODBCM_SIZE_9+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(p_ctr_table->svalid, &valid_value.value,
          (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "sValid = %s", p_ctr_table->svalid);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16,
        CTR_CS_ROW_STATUS/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for cs_row_status unit16_t*/
      ColumnAttrValue <uint16_t> rs_value =
        *((ColumnAttrValue <uint16_t>*)
          ((*i).p_table_attribute_value));
      ODBCM_MEMSET(&p_ctr_table->scs_row_status, 0, sizeof(SQLSMALLINT));
      p_ctr_table->scs_row_status = rs_value.value;
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "sCs_row_status = %d", p_ctr_table->scs_row_status);
    } ODBCM_COMPARE_ATTRNAME_DATATYPE((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_9,
        CTR_CS_ATTR/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to receive the void* values from
       * caller and typecast it into appropriate data type,
       * for cs_attr CHAR[9]*/
      ColumnAttrValue <uint8_t[ODBCM_SIZE_9]> csa_value =
          *((ColumnAttrValue<uint8_t[ODBCM_SIZE_9]>*)
            ((*i).p_table_attribute_value));
      ODBCM_MEMSET(p_ctr_table->scs_attr, 0, ODBCM_SIZE_9+1);
      /**copying the value from template to binded buffer */
      ODBCM_MEMCPY(p_ctr_table->scs_attr, &csa_value.value,
          (*i).table_attribute_length);
      odbcm_debug_info("ODBCM::DBVarbind::fill:CTR_TABLE: "
          "sCs_attr = %s", p_ctr_table->scs_attr);
    }
  }
  return ODBCM_RC_SUCCESS;
}

/**
 * @Description : Function to fetch the controller_common_table
 * values(which is stored in  controller_common_table_ struct )
 * and store into TableAttSchema
 * @param[in]   : vector<TableAttrSchema> &column_attr
 * @return      : ODBCM_RC_STATUS
 **/
ODBCM_RC_STATUS DBVarbind::fetch_controller_table(
    std::vector<TableAttrSchema> &column_attr
    /*DBTableSchema->rowlist_ entry*/) {
  /**Vector iterator to take the TableAttrSchema structures*/
  std::vector< TableAttrSchema >::iterator i;
  /**Loop for iterate all the elements in the vector, the TableAttrSchema
     * table_attribute_name value will be compared and corresponding
     * structure member will be fetched */
  for (i = column_attr.begin(); i != column_attr.end(); ++i) {
    ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32,
        CTR_NAME/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
          uint8_t[ODBCM_SIZE_32+1],
          val_controller_name);
      ODBCM_MEMCPY(
          val_controller_name->value,
          p_ctr_table->szcontroller_name,
          sizeof(p_ctr_table->szcontroller_name));/* sizeof will return 32+1 */
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "controller_name = %s" , val_controller_name->value);
      (*i).p_table_attribute_value = val_controller_name;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16,
        CTR_TYPE/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
            uint16_t,
            type_value);
      type_value->value = p_ctr_table->stype;
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "type = %d", type_value->value);

      (*i).p_table_attribute_value = type_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32,
        CTR_VERSION/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
            uint8_t[ODBCM_SIZE_32+1],
            val_version);
      ODBCM_MEMCPY(
            val_version->value,
            p_ctr_table->szversion,
            sizeof(p_ctr_table->szversion));
      (*i).p_table_attribute_value = val_version;
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: ""szversion = %s",
          val_version->value);
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_128,
        CTR_DESCRIPTION/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
            uint8_t[ODBCM_SIZE_128+1],
            desc_value);
      ODBCM_MEMCPY(
            desc_value->value,
            p_ctr_table->szdescription,
            sizeof(p_ctr_table->szdescription));
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "description = %s", desc_value->value);
      (*i).p_table_attribute_value = desc_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_IPV4,
        CTR_IP_ADDRESS/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
            uint32_t,
            ip_value);
      ip_value->value = p_ctr_table->szip_address;
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "from db ip_address = %s",
          ODBCMUtils::get_ip_string(p_ctr_table->szip_address).c_str());
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "ip_address = %s",
          ODBCMUtils::get_ip_string(ip_value->value).c_str());
      (*i).p_table_attribute_value = ip_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32,
        CTR_USER_NAME/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
            uint8_t[ODBCM_SIZE_32+1],
            user_value);
      ODBCM_MEMCPY(
            user_value->value,
            p_ctr_table->szuser_name,
            sizeof(p_ctr_table->szuser_name));
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "user_name = %s", user_value->value);
      (*i).p_table_attribute_value = user_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT8_ARRAY_257,
          CTR_PASSWORD/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
            uint8_t[ODBCM_SIZE_257+1],
            pass_value);
      ODBCM_MEMCPY(
            pass_value->value,
            p_ctr_table->szpassword,
            sizeof(p_ctr_table->szpassword));
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "password = %s", pass_value->value);
      (*i).p_table_attribute_value = pass_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT16,
          CTR_ENABLE_AUDIT/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(uint16_t, ad_value);
      ad_value->value = p_ctr_table->senable_audit;

      (*i).p_table_attribute_value = ad_value;
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: senable_audit = %d",
          ad_value->value);
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_32,
        CTR_ACTUAL_VERSION/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
            uint8_t[ODBCM_SIZE_32+1],
            av_value);
      ODBCM_MEMCPY(
            av_value->value,
            p_ctr_table->szactual_version,
            sizeof(p_ctr_table->szactual_version));
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "actual_version = %s", av_value->value);
      (*i).p_table_attribute_value = av_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT16,
        CTR_OPER_STATUS/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(uint16_t, rs_value);
      rs_value->value = p_ctr_table->soper_status;
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "oper_status = %d", rs_value->value);
      (*i).p_table_attribute_value = rs_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_9,
        CTR_VALID/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
              uint8_t[ODBCM_SIZE_9+1],
              valid_value);
      ODBCM_MEMCPY(
              valid_value->value,
              p_ctr_table->svalid,
              sizeof(p_ctr_table->svalid));
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "valid = %s", valid_value->value);
      (*i).p_table_attribute_value = valid_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
          DATATYPE_UINT16,
          CTR_CS_ROW_STATUS/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(uint16_t, rs_value);
      rs_value->value = p_ctr_table->scs_row_status;
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "cs_row_status = %d", rs_value->value);

      (*i).p_table_attribute_value = rs_value;
    } ODBCM_COMPARE_FETCH_DATA((*i)/**TableAttrSchema structure*/,
        DATATYPE_UINT8_ARRAY_9,
        CTR_CS_ATTR/*attribute name MACRO*/) {
      /**ColumnAttrValue is a template to send the fetched values to
          * caller. typecast it into void*, memory will be allocated
          * for the template to send to caller*/
      ODBCM_ALLOCATE_COLUMN_ATTRVALUE_T(
                uint8_t[ODBCM_SIZE_9+1],
                cs_value);
      ODBCM_MEMCPY(
                cs_value->value,
                p_ctr_table->scs_attr,
                sizeof(p_ctr_table->scs_attr));
      odbcm_debug_info("ODBCM::DBVarbind::fetch:CTR_TABLE: "
          "cs_attr = %s", cs_value->value);
      (*i).p_table_attribute_value = cs_value;
    }
  }
  return ODBCM_RC_SUCCESS;
}
/** EOF*/