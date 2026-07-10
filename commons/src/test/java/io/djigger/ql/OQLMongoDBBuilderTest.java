package io.djigger.ql;

import com.mongodb.MongoClientSettings;
import org.bson.BsonDocument;
import org.bson.conversions.Bson;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies the OQL -> MongoDB {@link Bson} query translation. This also exercises the
 * antlr-generated OQL parser, which is why it is a useful guard for the (deferred) antlr upgrade.
 */
public class OQLMongoDBBuilderTest {

    private static BsonDocument toBson(String expression) {
        return OQLMongoDBBuilder.build(expression)
                .toBsonDocument(BsonDocument.class, MongoClientSettings.getDefaultCodecRegistry());
    }

    @Test
    public void testEquality() {
        assertEquals(BsonDocument.parse("{ \"env\": \"S01\" }"), toBson("env = S01"));
    }

    @Test
    public void testQuotedValueIsStripped() {
        assertEquals(BsonDocument.parse("{ \"name\": \"hello world\" }"), toBson("name = \"hello world\""));
    }

    @Test
    public void testAnd() {
        BsonDocument bson = toBson("a = 1 and b = 2");
        assertTrue(bson.containsKey("$and"), () -> "expected an $and query but got " + bson.toJson());
    }

    @Test
    public void testOr() {
        BsonDocument bson = toBson("a = 1 or b = 2");
        assertTrue(bson.containsKey("$or"), () -> "expected an $or query but got " + bson.toJson());
    }

    @Test
    public void testRegex() {
        BsonDocument bson = toBson("a ~ foo");
        assertTrue(bson.get("a").isRegularExpression(), () -> "expected a regex query but got " + bson.toJson());
        assertEquals("foo", bson.get("a").asRegularExpression().getPattern());
    }

    @Test
    public void testInvalidExpressionThrows() {
        assertThrows(RuntimeException.class, () -> OQLMongoDBBuilder.build("a =="));
    }
}
