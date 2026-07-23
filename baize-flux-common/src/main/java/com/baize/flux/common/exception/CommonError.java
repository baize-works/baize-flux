package com.baize.flux.common.exception;

import org.apache.commons.collections4.map.SingletonMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static com.baize.flux.common.exception.CommonErrorCode.*;


public class CommonError {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static FluxRuntimeException fileOperationFailed(
            String identifier, String operation, String fileName, Throwable cause) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("operation", operation);
        params.put("fileName", fileName);
        return new FluxRuntimeException(FILE_OPERATION_FAILED, params, cause);
    }

    public static FluxRuntimeException fileOperationFailed(
            String identifier, String operation, String fileName) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("operation", operation);
        params.put("fileName", fileName);
        return new FluxRuntimeException(FILE_OPERATION_FAILED, params);
    }

    public static FluxRuntimeException fileNotExistFailed(
            String identifier, String operation, String fileName) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("operation", operation);
        params.put("fileName", fileName);
        return new FluxRuntimeException(FILE_NOT_EXISTED, params);
    }



    public static FluxRuntimeException unsupportedDataType(
            String identifier, String dataType, String field) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("dataType", dataType);
        params.put("field", field);
        return new FluxRuntimeException(UNSUPPORTED_DATA_TYPE, params);
    }

    public static FluxRuntimeException unsupportedVersion(String identifier, String version) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("version", version);
        return new FluxRuntimeException(VERSION_NOT_SUPPORTED, params);
    }

    public static FluxRuntimeException unsupportedEncoding(String encoding) {
        Map<String, String> params = new SingletonMap<>("encoding", encoding);
        return new FluxRuntimeException(UNSUPPORTED_ENCODING, params);
    }


    public static FluxRuntimeException convertToConnectorTypeError(
            String identifier, String dataType, String field) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("dataType", dataType);
        params.put("field", field);
        return new FluxRuntimeException(CONVERT_TO_CONNECTOR_TYPE_ERROR_SIMPLE, params);
    }

    public static FluxRuntimeException getCatalogTableWithUnsupportedType(
            String catalogName, String tableName, Map<String, String> fieldWithDataTypes) {
        Map<String, String> params = new HashMap<>();
        params.put("catalogName", catalogName);
        params.put("tableName", tableName);
        try {
            params.put("fieldWithDataTypes", OBJECT_MAPPER.writeValueAsString(fieldWithDataTypes));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new FluxRuntimeException(GET_CATALOG_TABLE_WITH_UNSUPPORTED_TYPE_ERROR, params);
    }

    public static FluxRuntimeException getCatalogTablesWithUnsupportedType(
            String catalogName, Map<String, Map<String, String>> tableUnsupportedTypes) {
        Map<String, String> params = new HashMap<>();
        params.put("catalogName", catalogName);
        try {
            params.put(
                    "tableUnsupportedTypes",
                    OBJECT_MAPPER.writeValueAsString(tableUnsupportedTypes));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        return new FluxRuntimeException(
                GET_CATALOG_TABLES_WITH_UNSUPPORTED_TYPE_ERROR, params);
    }

    public static FluxRuntimeException jsonOperationError(String identifier, String payload) {
        return jsonOperationError(identifier, payload, null);
    }

    public static FluxRuntimeException jsonOperationError(
            String identifier, String payload, Throwable cause) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("payload", payload);
        FluxErrorCode code = JSON_OPERATION_FAILED;

        if (cause != null) {
            return new FluxRuntimeException(code, params, cause);
        } else {
            return new FluxRuntimeException(code, params);
        }
    }

    public static FluxRuntimeException unsupportedOperation(
            String identifier, String operation) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("operation", operation);
        return new FluxRuntimeException(OPERATION_NOT_SUPPORTED, params);
    }

    public static FluxRuntimeException sqlTemplateHandledError(
            String tableName,
            String keyName,
            String template,
            String placeholder,
            String optionName) {
        Map<String, String> params = new HashMap<>();
        params.put("tableName", tableName);
        params.put("keyName", keyName);
        params.put("template", template);
        params.put("placeholder", placeholder);
        params.put("optionName", optionName);
        return new FluxRuntimeException(SQL_TEMPLATE_HANDLED_ERROR, params);
    }

    public static FluxRuntimeException unsupportedArrayGenericType(
            String identifier, String dataType, String fieldName) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("dataType", dataType);
        params.put("fieldName", fieldName);
        return new FluxRuntimeException(UNSUPPORTED_ARRAY_GENERIC_TYPE, params);
    }

    public static FluxRuntimeException unsupportedRowKind(
            String identifier, String tableId, String rowKind) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("tableId", tableId);
        params.put("rowKind", rowKind);
        return new FluxRuntimeException(UNSUPPORTED_ROW_KIND, params);
    }

    public static FluxRuntimeException formatDateTimeError(String datetime, String field) {
        Map<String, String> params = new HashMap<>();
        params.put("datetime", datetime);
        params.put("field", field);
        return new FluxRuntimeException(CommonErrorCode.FORMAT_DATETIME_ERROR, params);
    }

    public static FluxRuntimeException formatDateError(String date, String field) {
        Map<String, String> params = new HashMap<>();
        params.put("date", date);
        params.put("field", field);
        return new FluxRuntimeException(CommonErrorCode.FORMAT_DATE_ERROR, params);
    }

    public static FluxRuntimeException unsupportedMethod(
            String identifier, String methodName) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        params.put("methodName", methodName);
        return new FluxRuntimeException(CommonErrorCode.UNSUPPORTED_METHOD, params);
    }

    public static FluxRuntimeException illegalArgument(String argument, String operation) {
        Map<String, String> params = new HashMap<>();
        params.put("argument", argument);
        params.put("operation", operation);
        return new FluxRuntimeException(ILLEGAL_ARGUMENT, params);
    }

    public static FluxRuntimeException closeFailed(String identifier, Throwable cause) {
        Map<String, String> params = new HashMap<>();
        params.put("identifier", identifier);
        return new FluxRuntimeException(CLOSE_FAILED, params, cause);
    }

}
