package com.baize.flux.api.configuration.util;

import com.baize.flux.api.configuration.Option;
import lombok.experimental.UtilityClass;
import com.fasterxml.jackson.core.type.TypeReference;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@UtilityClass
public class OptionUtil {

    private static final String ERROR_TEMPLATE = "%s: %s\n      type: %s\n      constraint: %s";

    public static String getOptionKeys(List<Option<?>> options) {
        StringBuilder builder = new StringBuilder();
        boolean flag = false;
        for (Option<?> option : options) {
            if (flag) {
                builder.append(", ");
            }
            builder.append("'").append(option.key()).append("'");
            flag = true;
        }
        return builder.toString();
    }

    public static String getOptionKeys(
            List<Option<?>> options, List<RequiredOption.BundledRequiredOptions> bundledOptions) {
        List<List<Option<?>>> optionList = new ArrayList<>();
        for (Option<?> option : options) {
            optionList.add(Collections.singletonList(option));
        }
        for (RequiredOption.BundledRequiredOptions bundledOption : bundledOptions) {
            optionList.add(bundledOption.getRequiredOption());
        }
        boolean flag = false;
        StringBuilder builder = new StringBuilder();
        for (List<Option<?>> optionSet : optionList) {
            if (flag) {
                builder.append(", ");
            }
            builder.append("[").append(getOptionKeys(optionSet)).append("]");
            flag = true;
        }
        return builder.toString();
    }

    public static List<Option<?>> getOptions(Class<?> clazz)
            throws InstantiationException, IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        List<Option<?>> options = new ArrayList<>();
        Object object = clazz.newInstance();
        for (Field field : fields) {
            field.setAccessible(true);
            OptionMark option = field.getAnnotation(OptionMark.class);
            if (option != null) {
                options.add(
                        new Option<>(
                                        !StringUtils.isNotBlank(option.name())
                                                ? formatUnderScoreCase(field.getName())
                                                : option.name(),
                                        new TypeReference<Object>() {
                                            @Override
                                            public Type getType() {
                                                return field.getType();
                                            }
                                        },
                                        field.get(object))
                                .withDescription(option.description()));
            }
        }
        return options;
    }

    public static String formatError(String optionKey, String type, String constraint) {
        return String.format(ERROR_TEMPLATE, "option", optionKey, type, constraint);
    }

    public static String formatOptionsError(String optionKeys, String type, String constraint) {
        return String.format(ERROR_TEMPLATE, "options", optionKeys, type, constraint);
    }

    private static String formatUnderScoreCase(String camel) {
        StringBuilder underScore =
                new StringBuilder(String.valueOf(Character.toLowerCase(camel.charAt(0))));
        for (int i = 1; i < camel.length(); i++) {
            char c = camel.charAt(i);
            underScore.append(Character.isLowerCase(c) ? c : "_" + Character.toLowerCase(c));
        }
        return underScore.toString();
    }
}
