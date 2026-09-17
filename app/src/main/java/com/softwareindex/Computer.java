package com.softwareindex;

/**
 * Модель данных для сущности «ЭВМ / Компьютер».
 * Привязана к конкретной аудитории (classroomId) и хранит наименование и спецификацию оборудования.
 */
public class Computer {
    private long id;
    private long classroomId;
    private String name;
    private String specs;

    public Computer() {
    }

    public Computer(long id, long classroomId, String name, String specs) {
        this.id = id;
        this.classroomId = classroomId;
        this.name = name;
        this.specs = specs;
    }

    public Computer(long classroomId, String name, String specs) {
        this.classroomId = classroomId;
        this.name = name;
        this.specs = specs;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getClassroomId() {
        return classroomId;
    }

    public void setClassroomId(long classroomId) {
        this.classroomId = classroomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecs() {
        return specs;
    }

    public void setSpecs(String specs) {
        this.specs = specs;
    }
}
