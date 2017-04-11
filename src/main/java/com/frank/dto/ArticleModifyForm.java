package com.frank.dto;

/**
 * Created by frank on 17/4/10.
 */
public class ArticleModifyForm extends ArticleForm {
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
