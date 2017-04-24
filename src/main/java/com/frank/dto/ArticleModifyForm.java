package com.frank.dto;

import javax.validation.constraints.Min;

/**
 * Created by frank on 17/4/10.
 */
public class ArticleModifyForm extends ArticleForm {
    @Min(value=1, message="文章ID错误")
    private int document_id;

    public int getDocument_id() {
        return document_id;
    }

    public void setDocument_id(int document_id) {
        this.document_id = document_id;
    }

    @Override
    public String toString() {
        return "ArticleModifyForm{" +
                "document_id=" + document_id +
                '}';
    }
}
