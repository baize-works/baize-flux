package com.baize.flux.api.configuration;

import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigurationContractTest {

    @Test
    public void strictValidationAcceptsFallbackKey() {
        Option<String> option = Options.key("current.key")
                .stringType()
                .fallbackKeys("legacy.key")
                .noDefaultValue();
        Map<String, Object> values = new HashMap<String, Object>();
        values.put("legacy", Collections.<String, Object>singletonMap("key", "value"));

        ValidationResult result = ConfigValidator.strict().validate(
                ReadonlyConfig.fromMap(values),
                OptionRule.builder().required(option).build());

        assertTrue(result.isValid());
    }

    @Test
    public void missingValueAccessRetainsOptionKey() {
        Option<String> option = Options.key("required.key")
                .stringType()
                .noDefaultValue();

        try {
            ReadonlyConfig.fromMap(Collections.<String, Object>emptyMap()).get(option);
            fail("Expected ConfigAccessException");
        } catch (ConfigAccessException e) {
            assertEquals("required.key", e.optionKey());
            assertEquals("required.key", e.getContext().get("optionKey"));
        }
    }

    @Test
    public void duplicateKeyMustUseSameOptionInstance() {
        Option<String> first = Options.key("duplicate")
                .stringType().noDefaultValue();
        Option<String> second = Options.key("duplicate")
                .stringType().description("different").noDefaultValue();

        try {
            OptionRule.builder().optional(first).required(second);
            fail("Expected conflicting option declaration");
        } catch (IllegalArgumentException e) {
            assertTrue(e.getMessage().contains("different Option instances"));
        }
    }

    @Test
    public void integerConvertersRejectFractionalNumbers() {
        Option<Integer> integerOption = Options.key("integer")
                .intType().noDefaultValue();
        Option<Long> longOption = Options.key("long")
                .longType().noDefaultValue();

        assertConversionFails(integerOption, Double.valueOf(1.5));
        assertConversionFails(longOption, Double.valueOf(1.5));
    }

    @Test
    public void stringConverterRejectsNonStringValues() {
        Option<String> option = Options.key("name")
                .stringType().noDefaultValue();
        assertConversionFails(option, Integer.valueOf(1));
    }

    @Test
    public void nullDefaultValueIsRejected() {
        try {
            Options.key("name").stringType().defaultValue(null);
            fail("Expected null default value rejection");
        } catch (IllegalArgumentException e) {
            assertEquals("Default value must not be null", e.getMessage());
        }
    }

    @Test
    public void conditionalRequiredRegistersReferencedOptionsOnce() {
        Option<String> trigger = Options.key("trigger")
                .stringType().noDefaultValue();
        Option<String> target = Options.key("target")
                .stringType().noDefaultValue();

        OptionRule rule = OptionRule.builder()
                .conditionalRequired(RuleCondition.present(trigger), target)
                .build();

        assertEquals(2, rule.options().size());
        assertEquals(1, rule.rules().size());
    }

    private static <T> void assertConversionFails(Option<T> option, Object raw) {
        try {
            ReadonlyConfig.fromMap(Collections.<String, Object>singletonMap(option.key(), raw))
                    .get(option);
            fail("Expected ConfigConversionException");
        } catch (ConfigConversionException expected) {
            assertFalse(expected.getMessage().isEmpty());
        }
    }
}
