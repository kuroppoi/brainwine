package brainwine.gameserver.util;

public class DateTimeUtils {
    
    /**
     * Converts a formatted duration string to an integer representing the duration in minutes.
     * Returns -1 if the input duration string format is invalid.
     * 
     * Format examples: 1y15d = 1 year & 15 days, 8w12h9m = 8 weeks, 12 hours & 9 minutes
     * Time unit characters: y = year, w = week, d = day, h = hour, m = minute
     */
    public static int parseFormattedDuration(String durationString) {
        long duration = 0;
        char[] chars = durationString.toCharArray();
        String unitCountString = "";
        
        for(int i = 0; i < chars.length; i++) {
            char c = chars[i];
            
            // If character is a number, append it to the total unit count string and skip to the next character
            if(Character.isDigit(c)) {
                unitCountString += c;
                continue;
            }
            
            // Return if character is (presumably) a unit id, but no amount has been specified yet
            if(unitCountString.isEmpty()) {
                return -1;
            }
            
            int unitCount = 0;
            
            try {
                unitCount = Integer.parseInt(unitCountString);
            } catch(NumberFormatException e) {
                return -1;
            }
            
            unitCountString = "";
            
            switch(c) {
                case 'y': duration += unitCount * 525600; break;
                case 'w': duration += unitCount * 10080; break;
                case 'd': duration += unitCount * 1440; break;
                case 'h': duration += unitCount * 60; break;
                case 'm': duration += unitCount; break;
                default: return -1;
            }  
        }
        
        return duration < 0 || duration > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)duration;
    }
}
