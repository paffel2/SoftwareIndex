package com.softwareindex;

/**
 * Модель данных для сущности «Класс / Аудитория».
 * Хранит информацию о номере аудитории, количества расположенных в ней ЭВМ,
 * привязанной кафедре и дополнительных примечаниях.
 */
public class ComputerRoom {
    private long id;
    private String roomNumber;
    private int computerCount;
    private long departmentId;
    private String departmentName;
    private String notes;

    public ComputerRoom() {
    }

    /**
     * Конструктор для загрузки полной информации из БД (с подгруженным именем кафедры и кол-вом ЭВМ).
     */
    public ComputerRoom(long id, String roomNumber, int computerCount, long departmentId, String departmentName, String notes) {
        this.id = id;
        this.roomNumber = roomNumber;
        this.computerCount = computerCount;
        this.departmentId = departmentId;
        this.departmentName = departmentName;
        this.notes = notes;
    }

    /**
     * Конструктор для создания или обновления аудитории.
     */
    public ComputerRoom(String roomNumber, long departmentId, String notes) {
        this.roomNumber = roomNumber;
        this.departmentId = departmentId;
        this.notes = notes;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public int getComputerCount() {
        return computerCount;
    }

    public void setComputerCount(int computerCount) {
        this.computerCount = computerCount;
    }

    public long getDepartmentId() {
        return departmentId;
    }

    public void setDepartmentId(long departmentId) {
        this.departmentId = departmentId;
    }

    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
