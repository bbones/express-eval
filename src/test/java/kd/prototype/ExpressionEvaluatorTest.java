package kd.prototype;

import kd.prtotype.ExpressionEvaluator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionEvaluatorTest {

    private final ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Test
    void testBasicEquals() {
        Map<String, Object> vars = Map.of("REQUEST_TYPE", "AR");
        assertTrue(evaluator.evaluate(vars, "REQUEST_TYPE = 'AR'"));
    }

    @Test
    void testNotEqualsAnd() {
        Map<String, Object> vars = Map.of("REQUEST_TYPE", "AR", "DESK", "AGRO");
        assertTrue(evaluator.evaluate(vars, "REQUEST_TYPE != 'NEW' AND DESK = 'AGRO'"));
    }

    @Test
    void testParenthesesOrAnd() {
        Map<String, Object> vars = Map.of("REQUEST_TYPE", "AR", "DESK", "AGRO", "IS_MEDIUM_TERM", true);
        assertTrue(evaluator.evaluate(vars, "(REQUEST_TYPE = 'NEW' OR REQUEST_TYPE = 'AR') AND DESK = 'AGRO' AND IS_MEDIUM_TERM"));
    }

    @Test
    void testNotNull() {
        Map<String, Object> vars = Map.of("IS_MEDIUM_TERM", true);
        assertTrue(evaluator.evaluate(vars, "IS_MEDIUM_TERM NOT NULL"));
    }

    @Test
    void testNull() {
        Map<String, Object> vars = Map.of();
        assertTrue(evaluator.evaluate(vars, "X NULL"));
    }

    @Test
    void testNumericComparisons() {
        Map<String, Object> vars = Map.of("PRIORITY", 5, "COST", 100.0);
        assertTrue(evaluator.evaluate(vars, "PRIORITY >= 5 AND COST < 200.5"));
    }

    @Test
    void testNegation() {
        Map<String, Object> vars = Map.of("DESK", "AGRO");
        assertTrue(evaluator.evaluate(vars, "NOT (DESK = 'LOC')"));
    }

    @Test
    void testFalseCase() {
        Map<String, Object> vars = Map.of("REQUEST_TYPE", "IR");
        assertFalse(evaluator.evaluate(vars, "REQUEST_TYPE = 'AR'"));
    }

    @Test
    void testBooleanComparison() {
        Map<String, Object> vars = Map.of("IS_VALID", true, "IS_TEST", false);

        assertTrue(evaluator.evaluate(vars, "IS_VALID = true"));
        assertFalse(evaluator.evaluate(vars, "IS_VALID = false"));
        assertTrue(evaluator.evaluate(vars, "IS_TEST != true"));
    }

    @Test
    void testBooleanEqualTrue() {
        Map<String, Object> vars = Map.of("IS_VALID", true);
        assertTrue(evaluator.evaluate(vars, "IS_VALID = true"));
    }

    @Test
    void testBooleanEqualFalse() {
        Map<String, Object> vars = Map.of("IS_VALID", false);
        assertTrue(evaluator.evaluate(vars, "IS_VALID = false"));
    }

    @Test
    void testBooleanNotEqual() {
        Map<String, Object> vars = Map.of("IS_VALID", true);
        assertTrue(evaluator.evaluate(vars, "IS_VALID != false"));
        assertFalse(evaluator.evaluate(vars, "IS_VALID != true"));
    }

    @Test
    void testBooleanDirectUsage() {
        Map<String, Object> vars = Map.of("IS_ENABLED", true, "IS_ARCHIVED", false);
        assertTrue(evaluator.evaluate(vars, "IS_ENABLED"));
        assertFalse(evaluator.evaluate(vars, "IS_ARCHIVED"));
    }

    @Test
    void testBooleanWithAndOr() {
        Map<String, Object> vars = Map.of("A", true, "B", false, "C", true);
        assertTrue(evaluator.evaluate(vars, "A AND C"));
        assertFalse(evaluator.evaluate(vars, "A AND B"));
        assertTrue(evaluator.evaluate(vars, "A OR B"));
        assertFalse(evaluator.evaluate(vars, "B AND C"));
    }

    @Test
    void testBooleanWithNot() {
        Map<String, Object> vars = Map.of("FLAG", true);
        assertFalse(evaluator.evaluate(vars, "NOT FLAG"));
        assertTrue(evaluator.evaluate(vars, "NOT (FLAG = false)"));
    }

    @Test
    void testBooleanParenthesesGrouping() {
        Map<String, Object> vars = Map.of("X", true, "Y", false, "Z", true);
        assertTrue(evaluator.evaluate(vars, "X AND (Y OR Z)"));
        assertFalse(evaluator.evaluate(vars, "(X AND Y) OR (Z AND Y)"));
    }

    @Test
    void testBooleanMixedTypes() {
        Map<String, Object> vars = Map.of("X", true, "Y", 5);
        assertTrue(evaluator.evaluate(vars, "X AND Y = 5"));
        assertFalse(evaluator.evaluate(vars, "X AND Y = 10"));
    }

    @Test
    void testBooleanUndefinedDefaultsToFalse() {
        Map<String, Object> vars = Map.of(); // empty
        assertThrows(RuntimeException.class, () -> evaluator.evaluate(vars, "IS_ACTIVE"));
    }

    @Test
    void testBooleanWithNulls() {
        Map<String, Object> vars = Map.of();
        assertTrue(evaluator.evaluate(vars, "IS_READY NULL"));
        assertFalse(evaluator.evaluate(vars, "IS_READY NOT NULL"));
    }

}