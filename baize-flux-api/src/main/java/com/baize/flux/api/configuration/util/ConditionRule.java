
package com.baize.flux.api.configuration.util;

import lombok.Getter;

import java.util.Objects;

@Getter
public class ConditionRule {

    private final Expression expression;
    private final OptionRule optionRule;

    public ConditionRule(Expression expression, OptionRule optionRule) {
        this.expression = expression;
        this.optionRule = optionRule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ConditionRule)) {
            return false;
        }
        ConditionRule that = (ConditionRule) o;
        return Objects.equals(expression, that.expression)
                && Objects.equals(optionRule, that.optionRule);
    }

    @Override
    public int hashCode() {
        return Objects.hash(expression, optionRule);
    }
}
