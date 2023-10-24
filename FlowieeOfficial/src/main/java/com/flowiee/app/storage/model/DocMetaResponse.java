package com.flowiee.app.storage.model;

import lombok.Data;

@Data
public class DocMetaResponse {
    private int docDataId;
    private String docDataValue;
    private String docFieldName;
    private String docFieldTypeInput;
    private boolean docFieldRequired;
}