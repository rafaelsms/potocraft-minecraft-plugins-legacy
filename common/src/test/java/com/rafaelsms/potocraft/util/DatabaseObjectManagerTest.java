package com.rafaelsms.potocraft.util;

import com.rafaelsms.potocraft.database.Database;
import com.rafaelsms.potocraft.database.DatabaseField;
import com.rafaelsms.potocraft.database.DatabaseObjectManager;
import com.rafaelsms.potocraft.database.converters.ZonedDateConverter;
import org.bson.Document;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DatabaseObjectManagerTest {

    private final DatabaseObjectManager databaseObjectManager = new DatabaseObjectManager();

    @Test
    void assureCorrectConversion() throws Database.DatabaseException {
        databaseObjectManager.registerConverter(ZonedDateTime.class, new ZonedDateConverter());

        TestingClassExpanded testingClass = new TestingClassExpanded();
        testingClass.stringTest = "nice string";
        testingClass.aLong = 412L;
        testingClass.booleanTest = false;
        testingClass.objectMap = Map.of("first", 1, "second", "second", "third", 3.0);
        testingClass.zonedDateTime = ZonedDateTime.now();
        testingClass.set = new HashSet<>(List.of("4", "set", "5", "set"));
        testingClass.object = null;

        Document document = databaseObjectManager.toDocument(testingClass);
        System.out.println(document);
        TestingClassExpanded fromDocument = databaseObjectManager.fromDocument(document, TestingClassExpanded.class);

        assertNotNull(fromDocument);

        assertNull(testingClass.object);
        assertNotNull(fromDocument.object); // doesn't have @DatabaseField

        assertEquals(testingClass.aLong, fromDocument.aLong);
        assertEquals(testingClass.booleanTest, fromDocument.booleanTest);
        assertEquals(testingClass.stringTest, fromDocument.stringTest);
        assertEquals(testingClass.stringTest, fromDocument.stringTest);
        assertEquals(testingClass.zonedDateTime, fromDocument.zonedDateTime);
        assertEquals(testingClass.set, fromDocument.set);
        assertEquals(testingClass.set.size(), fromDocument.set.size());
        assertTrue(fromDocument.integers.containsAll(testingClass.integers));
        assertEquals(testingClass.integers.size(), fromDocument.integers.size());
        assertTrue(fromDocument.objectMap.entrySet().containsAll(testingClass.objectMap.entrySet()));
        assertEquals(testingClass.objectMap.size(), fromDocument.objectMap.size());
    }

    @SuppressWarnings("FieldMayBeFinal")
    public static class TestingClass {

        @DatabaseField protected String stringTest = "initial string value";

        @DatabaseField protected boolean booleanTest = true;

        @DatabaseField protected long aLong = 4013L;

        @DatabaseField protected List<Integer> integers = List.of(3, 5, 6, 7);

    }

    @SuppressWarnings("FieldMayBeFinal")
    public static class TestingClassExpanded extends TestingClass {

        @DatabaseField private String stringTest;

        @DatabaseField private Map<String, Object> objectMap;

        @DatabaseField ZonedDateTime zonedDateTime;

        @DatabaseField private Set<String> set;

        private Object object = new Object();
    }
}
