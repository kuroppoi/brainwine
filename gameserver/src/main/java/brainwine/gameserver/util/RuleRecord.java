package brainwine.gameserver.util;

import brainwine.gameserver.command.CommandExecutor;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

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
        boolean adminOnly() default false;
        int minValue() default Integer.MIN_VALUE;
        int maxValue() default Integer.MAX_VALUE;
    }

    private Map<String, Field> getAccessibleRules(CommandExecutor executor) {
        Map<String, Field> result = new HashMap<>();
        for(Field f : getClass().getDeclaredFields()) {
            Rule rule = f.getAnnotation(Rule.class);
            if(rule == null) continue;
            if(rule.adminOnly() && !executor.isAdmin()) continue;
            f.setAccessible(true);
            result.put(rule.value(), f);
        }

        return result;
    }

    @JsonIgnore
    public List<String> getRules(CommandExecutor executor) {
        Map<String, Field> accessibleRules = getAccessibleRules(executor);

        List<String> result = new ArrayList<>(accessibleRules.size());
        for(String key : accessibleRules.keySet()) {
            try {
                result.add(ruleToString(accessibleRules.get(key)));
            } catch (IllegalAccessException e) {
                e.printStackTrace();
                result.add("Error while processing rule " + key);
            }
        }

        return result;
    }

    public String getRule(CommandExecutor executor, String key) {
        Map<String, Field> accessibleRules = getAccessibleRules(executor);
        Field field = accessibleRules.get(key);

        if(field == null) return "Rule " + key + " not found or you don't have access to it";

        try {
            return ruleToString(field);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return "Error while processing rule " + key;
        }
    }

    private String ruleToString(Field field) throws IllegalAccessException {
        Rule rule = field.getAnnotation(Rule.class);
        if(rule == null) throw new IllegalAccessException("Rule annotation is missing.");
        String current = rule.value();

        current += " " + field.get(this);

        List<String> notes = new ArrayList<>();
        if(rule.minValue() != Integer.MIN_VALUE) notes.add("min " + rule.minValue());
        if(rule.minValue() != Integer.MIN_VALUE) notes.add("max " + rule.maxValue());

        if(!notes.isEmpty()) current += " (" + String.join(", ", notes) + ")";
        return current;
    }

    public String setRule(CommandExecutor executor, String key, String value) {
        if(key == null) {
            return "Rule key is null";
        }

        final String lower = key.toLowerCase();
        try {
            Map<String, Field> accessibleRules = getAccessibleRules(executor);

            Field field = accessibleRules.get(key);

            if(field == null) {
                return "Rule " + key + " not found or you don't have access to it";
            }

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
