package com.softwareindex;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Вспомогательный класс управления базой данных SQLite.
 * Включает 3 сущности с реляционными связями (FOREIGN KEY):
 * 1. Таблица кафедр (departments)
 * 2. Таблица аудиторий/классов (classrooms)
 * 3. Таблица компьютеров/ЭВМ (computers)
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "computer_registry.db";
    private static final int DATABASE_VERSION = 4;

    // Таблица 1: Кафедры
    public static final String TABLE_DEPARTMENTS = "departments";
    public static final String COLUMN_DEPT_ID = "_id";
    public static final String COLUMN_DEPT_NAME = "name";
    public static final String COLUMN_DEPT_NOTES = "notes";

    // Таблица 2: Аудитории / Классы
    public static final String TABLE_CLASSROOMS = "classrooms";
    public static final String COLUMN_ROOM_ID = "_id";
    public static final String COLUMN_ROOM_NUMBER = "room_number";
    public static final String COLUMN_DEPARTMENT_ID = "department_id";
    public static final String COLUMN_ROOM_NOTES = "notes";

    // Таблица 3: ЭВМ / Компьютеры
    public static final String TABLE_COMPUTERS = "computers";
    public static final String COLUMN_COMP_ID = "_id";
    public static final String COLUMN_COMP_ROOM_ID = "classroom_id";
    public static final String COLUMN_COMP_NAME = "name";
    public static final String COLUMN_COMP_SPECS = "specs";

    // Константы режима сортировки
    public static final String SORT_ROOM_AND_COUNT = "room_and_count";
    public static final String SORT_ROOM_ONLY = "room_only";
    public static final String SORT_COUNT_DESC = "count_desc";
    public static final String SORT_COUNT_ASC = "count_asc";

    /**
     * Класс для агрегированной статистики по базе данных.
     */
    public static class Stats {
        public int totalRooms;
        public int totalComputers;
        public int maxComputers;
        public double avgComputers;

    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        // Включение поддержки внешних ключей в SQLite
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Создание таблицы кафедр
        String CREATE_DEPARTMENTS_TABLE = "CREATE TABLE " + TABLE_DEPARTMENTS + " ("
                + COLUMN_DEPT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_DEPT_NAME + " TEXT NOT NULL UNIQUE, "
                + COLUMN_DEPT_NOTES + " TEXT"
                + ");";

        // Создание таблицы классов с внешним ключом на кафедру
        String CREATE_CLASSROOMS_TABLE = "CREATE TABLE " + TABLE_CLASSROOMS + " ("
                + COLUMN_ROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_ROOM_NUMBER + " TEXT NOT NULL, "
                + COLUMN_DEPARTMENT_ID + " INTEGER NOT NULL, "
                + COLUMN_ROOM_NOTES + " TEXT, "
                + "FOREIGN KEY (" + COLUMN_DEPARTMENT_ID + ") REFERENCES " + TABLE_DEPARTMENTS + " (" + COLUMN_DEPT_ID + ") ON DELETE CASCADE"
                + ");";

        // Создание таблицы ЭВМ с внешним ключом на класс
        String CREATE_COMPUTERS_TABLE = "CREATE TABLE " + TABLE_COMPUTERS + " ("
                + COLUMN_COMP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_COMP_ROOM_ID + " INTEGER NOT NULL, "
                + COLUMN_COMP_NAME + " TEXT NOT NULL, "
                + COLUMN_COMP_SPECS + " TEXT NOT NULL, "
                + "FOREIGN KEY (" + COLUMN_COMP_ROOM_ID + ") REFERENCES " + TABLE_CLASSROOMS + " (" + COLUMN_ROOM_ID + ") ON DELETE CASCADE"
                + ");";

        db.execSQL(CREATE_DEPARTMENTS_TABLE);
        db.execSQL(CREATE_CLASSROOMS_TABLE);
        db.execSQL(CREATE_COMPUTERS_TABLE);

        // Наполнение базы начальными тестовыми данными
        seedTestData(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_COMPUTERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSROOMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEPARTMENTS);
        onCreate(db);
    }

    /**
     * Генерация демонстрационных тестовых данных для кафедр, классов и компьютеров.
     */
    private void seedTestData(SQLiteDatabase db) {
        long deptPo = insertSeedDept(db, "Кафедра ПО", "Программное обеспечение");
        long deptIt = insertSeedDept(db, "Кафедра ИТ", "Информационные технологии");
        long deptIb = insertSeedDept(db, "Кафедра ИБ", "Информационная безопасность");
        long deptVt = insertSeedDept(db, "Кафедра ВТ", "Вычислительная техника");
        long deptSapr = insertSeedDept(db, "Кафедра САПР", "Системы автоматизированного проектирования");

        long r101 = insertSeedRoom(db, "101", deptPo, "Лаборатория базового программирования");
        long r102 = insertSeedRoom(db, "102", deptIt, "Класс искусственного интеллекта");
        long r204 = insertSeedRoom(db, "204", deptIb, "Лаборатория защиты информации");
        long r210 = insertSeedRoom(db, "210", deptPo, "Компьютерный класс №1");
        long r305 = insertSeedRoom(db, "305", deptVt, "Лаборатория мобильной разработки");
        long r408 = insertSeedRoom(db, "408", deptSapr, "Лаборатория 3D-моделирования и САПР");
        long r501 = insertSeedRoom(db, "501", deptVt, "Вычислительный центр суперкомпьютеров");

        // ЭВМ для аудитории 101
        insertSeedComp(db, r101, "ПК Преподавателя (101-01)", "Intel Core i5-11400, 16 ГБ ОЗУ, SSD 512 ГБ, Intel UHD 730, 24\" FHD");
        for (int i = 2; i <= 15; i++) {
            insertSeedComp(db, r101, "Рабочая станция " + i + " (101-" + String.format(Locale.US, "%02d", i) + ")", "Intel Core i5-11400, 16 ГБ ОЗУ, SSD 512 ГБ, Intel UHD 730, 24\" FHD");
        }

        // ЭВМ для аудитории 102
        insertSeedComp(db, r102, "ИИ-Станция Главная (102-01)", "Intel Core i7-12700, 32 ГБ ОЗУ, NVMe SSD 1 ТБ, RTX 3060 12ГБ, 27\" QHD");
        for (int i = 2; i <= 12; i++) {
            insertSeedComp(db, r102, "ИИ-Станция " + i + " (102-" + String.format(Locale.US, "%02d", i) + ")", "Intel Core i7-12700, 32 ГБ ОЗУ, NVMe SSD 1 ТБ, RTX 3060 12ГБ, 27\" QHD");
        }

        // ЭВМ для аудитории 204
        for (int i = 1; i <= 8; i++) {
            insertSeedComp(db, r204, "ПК Защиты информации " + i, "AMD Ryzen 5 5600G, 16 ГБ ОЗУ, SSD 256 ГБ, Radeon Vega 7, 23.8\" FHD");
        }

        // ЭВМ для аудитории 210
        insertSeedComp(db, r210, "ПК Преподавателя 210", "Intel Core i5-13400, 16 ГБ ОЗУ, SSD 512 ГБ, GTX 1660 Super, 24\" FHD");
        for (int i = 2; i <= 20; i++) {
            insertSeedComp(db, r210, "Учебный ПК " + i, "Intel Core i5-13400, 16 ГБ ОЗУ, SSD 512 ГБ, GTX 1660 Super, 24\" FHD");
        }

        // ЭВМ для аудитории 305
        for (int i = 1; i <= 10; i++) {
            insertSeedComp(db, r305, "Mac mini M2 №" + i, "Apple Mac mini M2, 16 ГБ ОЗУ, SSD 512 ГБ, macOS Sonoma, 27\" 4K");
        }

        // ЭВМ для аудитории 408
        for (int i = 1; i <= 14; i++) {
            insertSeedComp(db, r408, "САПР Станция " + i, "AMD Ryzen 7 5700X, 32 ГБ ОЗУ, SSD 1 ТБ, RTX 4060 8ГБ, 27\" FHD 144 Гц");
        }

        // ЭВМ для аудитории 501
        for (int i = 1; i <= 6; i++) {
            insertSeedComp(db, r501, "Суперкомпьютерный узел " + i, "Intel Core i9-13900K, 64 ГБ ОЗУ, SSD 2 ТБ, RTX 4080 16ГБ, 32\" 4K");
        }
    }

    private long insertSeedDept(SQLiteDatabase db, String name, String notes) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEPT_NAME, name);
        values.put(COLUMN_DEPT_NOTES, notes);
        return db.insert(TABLE_DEPARTMENTS, null, values);
    }

    private long insertSeedRoom(SQLiteDatabase db, String roomNumber, long deptId, String notes) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM_NUMBER, roomNumber);
        values.put(COLUMN_DEPARTMENT_ID, deptId);
        values.put(COLUMN_ROOM_NOTES, notes);
        return db.insert(TABLE_CLASSROOMS, null, values);
    }

    private void insertSeedComp(SQLiteDatabase db, long roomId, String name, String specs) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMP_ROOM_ID, roomId);
        values.put(COLUMN_COMP_NAME, name);
        values.put(COLUMN_COMP_SPECS, specs);
        db.insert(TABLE_COMPUTERS, null, values);
    }

    // --- Операции с Кафедрами ---

    public long addDepartment(Department dept) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEPT_NAME, dept.getName());
        values.put(COLUMN_DEPT_NOTES, dept.getNotes());
        return db.insert(TABLE_DEPARTMENTS, null, values);
    }

    public List<Department> getAllDepartments() {
        List<Department> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DEPARTMENTS, null, null, null, null, null, COLUMN_DEPT_NAME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            int idIdx = cursor.getColumnIndexOrThrow(COLUMN_DEPT_ID);
            int nameIdx = cursor.getColumnIndexOrThrow(COLUMN_DEPT_NAME);
            int notesIdx = cursor.getColumnIndexOrThrow(COLUMN_DEPT_NOTES);

            do {
                long id = cursor.getLong(idIdx);
                String name = cursor.getString(nameIdx);
                String notes = cursor.getString(notesIdx);
                list.add(new Department(id, name, notes));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }

    public int deleteDepartment(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_DEPARTMENTS, COLUMN_DEPT_ID + " = ?", new String[]{String.valueOf(id)});
    }

    // --- Операции с Аудиториями / Классами ---

    public long addRoom(ComputerRoom room) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM_NUMBER, room.getRoomNumber());
        values.put(COLUMN_DEPARTMENT_ID, room.getDepartmentId());
        values.put(COLUMN_ROOM_NOTES, room.getNotes());
        return db.insert(TABLE_CLASSROOMS, null, values);
    }

    public int updateRoom(ComputerRoom room) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROOM_NUMBER, room.getRoomNumber());
        values.put(COLUMN_DEPARTMENT_ID, room.getDepartmentId());
        values.put(COLUMN_ROOM_NOTES, room.getNotes());
        return db.update(TABLE_CLASSROOMS, values, COLUMN_ROOM_ID + " = ?", new String[]{String.valueOf(room.getId())});
    }

    public int deleteRoom(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_CLASSROOMS, COLUMN_ROOM_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public ComputerRoom getRoomById(long roomId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT c." + COLUMN_ROOM_ID
                + ", c." + COLUMN_ROOM_NUMBER
                + ", c." + COLUMN_DEPARTMENT_ID
                + ", d." + COLUMN_DEPT_NAME + " AS department_name"
                + ", c." + COLUMN_ROOM_NOTES
                + ", (SELECT COUNT(*) FROM " + TABLE_COMPUTERS + " comp WHERE comp." + COLUMN_COMP_ROOM_ID + " = c." + COLUMN_ROOM_ID + ") AS computer_count"
                + " FROM " + TABLE_CLASSROOMS + " c "
                + " INNER JOIN " + TABLE_DEPARTMENTS + " d ON c." + COLUMN_DEPARTMENT_ID + " = d." + COLUMN_DEPT_ID
                + " WHERE c." + COLUMN_ROOM_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(roomId)});
        ComputerRoom room = null;
        if (cursor != null && cursor.moveToFirst()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID));
            String roomNum = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM_NUMBER));
            long deptId = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT_ID));
            String deptName = cursor.getString(cursor.getColumnIndexOrThrow("department_name"));
            String notes = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROOM_NOTES));
            int count = cursor.getInt(cursor.getColumnIndexOrThrow("computer_count"));

            room = new ComputerRoom(id, roomNum, count, deptId, deptName, notes);
            cursor.close();
        }
        return room;
    }

    /**
     * Возвращает список всех аудиторий с вычисленным количеством ЭВМ и поддержкой поиска/сортировки.
     */
    public List<ComputerRoom> getAllRooms(String sortMode, String searchQuery) {
        List<ComputerRoom> roomList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        StringBuilder rawQuery = new StringBuilder();
        rawQuery.append("SELECT c.").append(COLUMN_ROOM_ID)
                .append(", c.").append(COLUMN_ROOM_NUMBER)
                .append(", c.").append(COLUMN_DEPARTMENT_ID)
                .append(", d.").append(COLUMN_DEPT_NAME).append(" AS department_name")
                .append(", c.").append(COLUMN_ROOM_NOTES)
                .append(", (SELECT COUNT(*) FROM ").append(TABLE_COMPUTERS).append(" comp WHERE comp.").append(COLUMN_COMP_ROOM_ID).append(" = c.").append(COLUMN_ROOM_ID).append(") AS computer_count")
                .append(" FROM ").append(TABLE_CLASSROOMS).append(" c ")
                .append(" INNER JOIN ").append(TABLE_DEPARTMENTS).append(" d ON c.").append(COLUMN_DEPARTMENT_ID).append(" = d.").append(COLUMN_DEPT_ID);

        List<String> selectionArgsList = new ArrayList<>();
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            rawQuery.append(" WHERE c.").append(COLUMN_ROOM_NUMBER).append(" LIKE ? ")
                    .append(" OR d.").append(COLUMN_DEPT_NAME).append(" LIKE ? ")
                    .append(" OR c.").append(COLUMN_ROOM_NOTES).append(" LIKE ? ")
                    .append(" OR EXISTS (SELECT 1 FROM ").append(TABLE_COMPUTERS).append(" comp3 WHERE comp3.").append(COLUMN_COMP_ROOM_ID).append(" = c.").append(COLUMN_ROOM_ID).append(" AND comp3.").append(COLUMN_COMP_SPECS).append(" LIKE ?) ");
            String arg = "%" + searchQuery.trim() + "%";
            selectionArgsList.add(arg);
            selectionArgsList.add(arg);
            selectionArgsList.add(arg);
            selectionArgsList.add(arg);
        }

        String orderBy;
        if (SORT_ROOM_ONLY.equals(sortMode)) {
            orderBy = " LENGTH(c." + COLUMN_ROOM_NUMBER + ") ASC, c." + COLUMN_ROOM_NUMBER + " ASC";
        } else if (SORT_COUNT_DESC.equals(sortMode)) {
            orderBy = " computer_count DESC, LENGTH(c." + COLUMN_ROOM_NUMBER + ") ASC, c." + COLUMN_ROOM_NUMBER + " ASC";
        } else if (SORT_COUNT_ASC.equals(sortMode)) {
            orderBy = " computer_count ASC, LENGTH(c." + COLUMN_ROOM_NUMBER + ") ASC, c." + COLUMN_ROOM_NUMBER + " ASC";
        } else {
            // По умолчанию: сортировка по номеру класса и количеству ЭВМ
            orderBy = " LENGTH(c." + COLUMN_ROOM_NUMBER + ") ASC, c." + COLUMN_ROOM_NUMBER + " ASC, computer_count ASC";
        }

        rawQuery.append(" ORDER BY ").append(orderBy);

        String[] selectionArgs = selectionArgsList.toArray(new String[0]);
        Cursor cursor = db.rawQuery(rawQuery.toString(), selectionArgs);

        if (cursor != null && cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ROOM_ID);
            int roomNumIndex = cursor.getColumnIndexOrThrow(COLUMN_ROOM_NUMBER);
            int countIndex = cursor.getColumnIndexOrThrow("computer_count");
            int deptIdIndex = cursor.getColumnIndexOrThrow(COLUMN_DEPARTMENT_ID);
            int deptNameIndex = cursor.getColumnIndexOrThrow("department_name");
            int notesIndex = cursor.getColumnIndexOrThrow(COLUMN_ROOM_NOTES);

            do {
                long id = cursor.getLong(idIndex);
                String roomNumber = cursor.getString(roomNumIndex);
                int count = cursor.getInt(countIndex);
                long deptId = cursor.getLong(deptIdIndex);
                String deptName = cursor.getString(deptNameIndex);
                String notes = cursor.getString(notesIndex);

                roomList.add(new ComputerRoom(id, roomNumber, count, deptId, deptName, notes));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return roomList;
    }

    // --- Операции с Компьютерами / ЭВМ ---

    public long addComputer(Computer computer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMP_ROOM_ID, computer.getClassroomId());
        values.put(COLUMN_COMP_NAME, computer.getName());
        values.put(COLUMN_COMP_SPECS, computer.getSpecs());
        return db.insert(TABLE_COMPUTERS, null, values);
    }

    public int updateComputer(Computer computer) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COMP_NAME, computer.getName());
        values.put(COLUMN_COMP_SPECS, computer.getSpecs());
        return db.update(TABLE_COMPUTERS, values, COLUMN_COMP_ID + " = ?", new String[]{String.valueOf(computer.getId())});
    }

    public int deleteComputer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_COMPUTERS, COLUMN_COMP_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public List<Computer> getComputersForRoom(long classroomId) {
        List<Computer> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_COMPUTERS, null, COLUMN_COMP_ROOM_ID + " = ?", new String[]{String.valueOf(classroomId)}, null, null, COLUMN_COMP_ID + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            int idIdx = cursor.getColumnIndexOrThrow(COLUMN_COMP_ID);
            int roomIdx = cursor.getColumnIndexOrThrow(COLUMN_COMP_ROOM_ID);
            int nameIdx = cursor.getColumnIndexOrThrow(COLUMN_COMP_NAME);
            int specsIdx = cursor.getColumnIndexOrThrow(COLUMN_COMP_SPECS);

            do {
                long id = cursor.getLong(idIdx);
                long roomId = cursor.getLong(roomIdx);
                String name = cursor.getString(nameIdx);
                String specs = cursor.getString(specsIdx);

                list.add(new Computer(id, roomId, name, specs));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return list;
    }


    /**
     * Сброс всех таблиц базы данных и повторное заполнение демонстрационными данными.
     */
    public void resetToDefaultData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("DELETE FROM " + TABLE_COMPUTERS);
        db.execSQL("DELETE FROM " + TABLE_CLASSROOMS);
        db.execSQL("DELETE FROM " + TABLE_DEPARTMENTS);
        seedTestData(db);
    }
}
