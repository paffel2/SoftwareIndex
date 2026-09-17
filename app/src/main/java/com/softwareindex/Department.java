package com.softwareindex;

import androidx.annotation.NonNull;

/**
 * Модель данных для сущности «Кафедра».
 * Содержит идентификатор, наименование кафедры и краткое описание.
 */
public class Department {
    private long id;
    private String name;
    private String notes;

    public Department() {
    }

    public Department(long id, String name, String notes) {
        this.id = id;
        this.name = name;
        this.notes = notes;
    }

    public Department(String name, String notes) {
        this.name = name;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
