package org.programmers.signalbuddyfinal.global.config;

import static org.hibernate.type.StandardBasicTypes.DOUBLE;

import org.hibernate.boot.model.FunctionContributions;
import org.hibernate.boot.model.FunctionContributor;
import org.hibernate.type.BasicType;

public class CustomMariaDbFunctionContributor implements FunctionContributor {

    private static final String FUNCTION_NAME = "match2_against";
    private static final String FUNCTION_PATTERN = "match (?1, ?2) against (?3 in boolean mode)";

    @Override
    public void contributeFunctions(final FunctionContributions functionContributions) {
        BasicType<Double> resultType = functionContributions
            .getTypeConfiguration()
            .getBasicTypeRegistry()
            .resolve(DOUBLE);

        functionContributions.getFunctionRegistry()
            .registerPattern(FUNCTION_NAME, FUNCTION_PATTERN, resultType);
    }
}
