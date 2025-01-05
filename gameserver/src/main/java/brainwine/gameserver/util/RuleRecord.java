package brainwine.gameserver.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**A class that defines a set of rules that can be set by the player.
 * Class fields annotated with the <code>Rule</code> annotation should not be final.
 */
public class RuleRecord {
    private static class SetRuleException extends Exception {
        public SetRuleException(String reason) {
            super(reason);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    protected @interface Rule {
        String value();
        int minValue() default Integer.MIN_VALUE;
        int maxValue() default Integer.MAX_VALUE;
    }

    @JsonIgnore
    public List<String> getRules() {
        return Arrays.stream(this.getClass().getFields()).map(
                f -> f.getAnnotation(Rule.class)
        ).filter(Objects::nonNull).map(Rule::value).collect(Collectors.toList());
    }

    public String setRule(String key, String value) {
        if(key == null) {
            return "Rule key is null";
        }

        final String lower = key.toLowerCase();
        try {
            System.out.println(this.getClass());
            Arrays.stream(this.getClass().getFields()).map(f -> f.getName()).forEach(System.out::println);
            Optional<Field> fieldOpt = Arrays.stream(this.getClass().getFields()).filter(
                    f -> f.getAnnotation(Rule.class) != null && f.getAnnotation(Rule.class).value().equals(lower)
            ).findFirst();

            if(!fieldOpt.isPresent()) {
                return "Rule " + key + " not found";
            }

            Field field = fieldOpt.get();
            Rule rule = field.getAnnotation(Rule.class);

            if(Boolean.TYPE.equals(field.getType())) {
                field.setBoolean(this, parseBoolean(value));
                return null;
            }

            if(Integer.TYPE.equals(field.getType())) {
                field.set(this, parseInt(value, rule));
                return null;
            }

            if(String.class.equals(field.getType())) {
                field.set(this, parseString(value, rule));
                return null;
            }

            return "Rule type not matched";
        } catch (SetRuleException e) {
            return "Invalid argument: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "An unexpected error occurred";
        }
    }

    private int parseInt(String str, Rule rule) throws SetRuleException {
        try {
            int value = Integer.parseInt(str);

            if(value < rule.minValue()) {
                throw new SetRuleException("Value must be at least " + rule.minValue());
            }

            if(value > rule.maxValue()) {
                throw new SetRuleException("Value must be at most " + rule.maxValue());
            }

            return value;
        } catch(NumberFormatException e) {
            throw new SetRuleException(e.getMessage());
        }
    }

    private boolean parseBoolean(String str) throws SetRuleException {
        switch(str.toLowerCase()) {
            case "yes":
            case "true":
            case "on":
                return true;
            case "no":
            case "false":
            case "off":
                return false;
            default:
                throw new SetRuleException(str + " is not a Boolean");
        }
    }

    private String parseString(String str, Rule rule) throws SetRuleException {
        int len = str.length();
        if(len < rule.minValue()) {
            throw new SetRuleException("Value must be at least " + rule.minValue() + " characters long.");
        }

        if(len > rule.maxValue()) {
            throw new SetRuleException("Value must be at most " + rule.maxValue() + " characters long.");
        }

        return str;
    }
}
