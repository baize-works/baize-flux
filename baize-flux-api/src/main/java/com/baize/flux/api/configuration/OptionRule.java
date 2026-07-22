package com.baize.flux.api.configuration;

import java.util.*;

/**
 * Declares all supported options and their structural and value validation rules.
 */
public final class OptionRule {

    /**
     * Marker interface for all configuration rules.
     */
    public interface Rule {
    }

    /**
     * Declares that all specified options are required.
     */
    public static final class RequiredRule implements Rule {

        private final List<Option<?>> options;

        public RequiredRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof RequiredRule)) {
                return false;
            }

            RequiredRule that = (RequiredRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "RequiredRule{options=" + options + '}';
        }
    }

    /**
     * Declares that exactly one of the specified options must have a value.
     */
    public static final class ExactlyOneRule implements Rule {

        private final List<Option<?>> options;

        public ExactlyOneRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ExactlyOneRule)) {
                return false;
            }

            ExactlyOneRule that = (ExactlyOneRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "ExactlyOneRule{options=" + options + '}';
        }
    }

    /**
     * Declares that zero or one of the specified options may have a value.
     */
    public static final class AtMostOneRule implements Rule {

        private final List<Option<?>> options;

        public AtMostOneRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AtMostOneRule)) {
                return false;
            }

            AtMostOneRule that = (AtMostOneRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "AtMostOneRule{options=" + options + '}';
        }
    }

    /**
     * Declares that all specified options must either be present together
     * or absent together.
     */
    public static final class AllOrNoneRule implements Rule {

        private final List<Option<?>> options;

        public AllOrNoneRule(List<Option<?>> options) {
            this.options = immutableOptions(options, "options");
        }

        public List<Option<?>> options() {
            return options;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof AllOrNoneRule)) {
                return false;
            }

            AllOrNoneRule that = (AllOrNoneRule) obj;
            return Objects.equals(options, that.options);
        }

        @Override
        public int hashCode() {
            return Objects.hash(options);
        }

        @Override
        public String toString() {
            return "AllOrNoneRule{options=" + options + '}';
        }
    }

    /**
     * Declares that the specified options become required when a condition matches.
     */
    public static final class ConditionalRequiredRule implements Rule {

        private final RuleCondition condition;
        private final List<Option<?>> requiredOptions;

        public ConditionalRequiredRule(
                RuleCondition condition,
                List<Option<?>> requiredOptions) {

            this.condition = Objects.requireNonNull(
                    condition,
                    "condition must not be null"
            );

            this.requiredOptions = immutableOptions(
                    requiredOptions,
                    "requiredOptions"
            );
        }

        public RuleCondition condition() {
            return condition;
        }

        public List<Option<?>> requiredOptions() {
            return requiredOptions;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ConditionalRequiredRule)) {
                return false;
            }

            ConditionalRequiredRule that =
                    (ConditionalRequiredRule) obj;

            return Objects.equals(condition, that.condition)
                    && Objects.equals(requiredOptions, that.requiredOptions);
        }

        @Override
        public int hashCode() {
            return Objects.hash(condition, requiredOptions);
        }

        @Override
        public String toString() {
            return "ConditionalRequiredRule{"
                    + "condition="
                    + condition
                    + ", requiredOptions="
                    + requiredOptions
                    + '}';
        }
    }

    /**
     * Declares a value constraint for one option.
     *
     * @param <T> option value type
     */
    public static final class ValueRule<T> implements Rule {

        private final Option<T> option;
        private final Constraint<T> constraint;

        public ValueRule(
                Option<T> option,
                Constraint<T> constraint) {

            this.option = Objects.requireNonNull(
                    option,
                    "option must not be null"
            );

            this.constraint = Objects.requireNonNull(
                    constraint,
                    "constraint must not be null"
            );
        }

        public Option<T> option() {
            return option;
        }

        public Constraint<T> constraint() {
            return constraint;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ValueRule)) {
                return false;
            }

            ValueRule<?> that = (ValueRule<?>) obj;

            return Objects.equals(option, that.option)
                    && Objects.equals(constraint, that.constraint);
        }

        @Override
        public int hashCode() {
            return Objects.hash(option, constraint);
        }

        @Override
        public String toString() {
            return "ValueRule{"
                    + "option="
                    + option
                    + ", constraint="
                    + constraint
                    + '}';
        }
    }

    private final List<Option<?>> options;
    private final List<Rule> rules;

    private OptionRule(
            List<Option<?>> options,
            List<Rule> rules) {

        this.options = immutableOptions(options, "options");
        this.rules = immutableRules(rules);
    }

    public static Builder builder() {
        return new Builder();
    }

    public List<Option<?>> options() {
        return options;
    }

    public List<Rule> rules() {
        return rules;
    }

    @Override
    public String toString() {
        return "OptionRule{"
                + "options="
                + options
                + ", rules="
                + rules
                + '}';
    }

    /**
     * Builder used to declare options and validation rules.
     */
    public static final class Builder {

        private final Map<String, Option<?>> options =
                new LinkedHashMap<String, Option<?>>();

        private final List<Rule> rules =
                new ArrayList<Rule>();

        private Builder() {
        }

        /**
         * Registers optional configuration options.
         */
        public Builder optional(Option<?>... options) {
            requireAtLeast(1, "optional", options);
            register(options);
            return this;
        }

        /**
         * Registers required configuration options.
         */
        public Builder required(Option<?>... options) {
            requireAtLeast(1, "required", options);

            for (Option<?> option : options) {
                Objects.requireNonNull(
                        option,
                        "option must not be null"
                );

                if (option.hasDefaultValue()) {
                    throw new IllegalArgumentException(
                            "Required option '"
                                    + option.key()
                                    + "' must not define a default value"
                    );
                }
            }

            register(options);
            rules.add(
                    new RequiredRule(Arrays.asList(options))
            );

            return this;
        }

        /**
         * Declares that exactly one option must resolve to a value.
         */
        public Builder exactlyOneOf(Option<?>... options) {
            requireAtLeast(2, "exactlyOneOf", options);

            register(options);
            rules.add(
                    new ExactlyOneRule(Arrays.asList(options))
            );

            return this;
        }

        /**
         * Declares that zero or one option may resolve to a value.
         */
        public Builder atMostOneOf(Option<?>... options) {
            requireAtLeast(2, "atMostOneOf", options);

            register(options);
            rules.add(
                    new AtMostOneRule(Arrays.asList(options))
            );

            return this;
        }

        /**
         * Declares that all options must either resolve to values
         * or all remain absent.
         */
        public Builder allOrNone(Option<?>... options) {
            requireAtLeast(2, "allOrNone", options);

            register(options);
            rules.add(
                    new AllOrNoneRule(Arrays.asList(options))
            );

            return this;
        }

        /**
         * Declares conditionally required options.
         */
        public Builder conditionalRequired(
                RuleCondition condition,
                Option<?>... requiredOptions) {

            Objects.requireNonNull(
                    condition,
                    "condition must not be null"
            );

            requireAtLeast(
                    1,
                    "conditionalRequired",
                    requiredOptions
            );

            Set<Option<?>> referencedOptions =
                    Objects.requireNonNull(
                            condition.referencedOptions(),
                            "condition referenced options must not be null"
                    );

            register(
                    referencedOptions.toArray(
                            new Option<?>[referencedOptions.size()]
                    )
            );

            register(requiredOptions);

            rules.add(
                    new ConditionalRequiredRule(
                            condition,
                            Arrays.asList(requiredOptions)
                    )
            );

            return this;
        }

        /**
         * Adds a value constraint for an option.
         */
        public <T> Builder constrain(
                Option<T> option,
                Constraint<T> constraint) {

            Objects.requireNonNull(
                    option,
                    "option must not be null"
            );

            Objects.requireNonNull(
                    constraint,
                    "constraint must not be null"
            );

            register(option);

            rules.add(
                    new ValueRule<T>(option, constraint)
            );

            return this;
        }

        public OptionRule build() {
            return new OptionRule(
                    new ArrayList<Option<?>>(options.values()),
                    new ArrayList<Rule>(rules)
            );
        }

        private void register(Option<?>... newOptions) {
            Objects.requireNonNull(
                    newOptions,
                    "options must not be null"
            );

            for (Option<?> option : newOptions) {
                Objects.requireNonNull(
                        option,
                        "option must not be null"
                );

                Option<?> existing = options.get(option.key());

                if (existing == null) {
                    options.put(option.key(), option);
                    continue;
                }

                if (existing != option) {
                    throw new IllegalArgumentException(
                            "Option key '"
                                    + option.key()
                                    + "' is declared by different Option instances"
                    );
                }
            }
        }

        private static void requireAtLeast(
                int minimum,
                String method,
                Option<?>[] options) {

            if (options == null || options.length < minimum) {
                throw new IllegalArgumentException(
                        method
                                + " requires at least "
                                + minimum
                                + " option(s)"
                );
            }
        }
    }

    private static List<Option<?>> immutableOptions(
            List<Option<?>> options,
            String fieldName) {

        Objects.requireNonNull(
                options,
                fieldName + " must not be null"
        );

        List<Option<?>> copiedOptions =
                new ArrayList<Option<?>>(options.size());

        for (Option<?> option : options) {
            copiedOptions.add(
                    Objects.requireNonNull(
                            option,
                            fieldName + " must not contain null"
                    )
            );
        }

        return Collections.unmodifiableList(copiedOptions);
    }

    private static List<Rule> immutableRules(
            List<Rule> rules) {

        Objects.requireNonNull(
                rules,
                "rules must not be null"
        );

        List<Rule> copiedRules =
                new ArrayList<Rule>(rules.size());

        for (Rule rule : rules) {
            copiedRules.add(
                    Objects.requireNonNull(
                            rule,
                            "rules must not contain null"
                    )
            );
        }

        return Collections.unmodifiableList(copiedRules);
    }
}