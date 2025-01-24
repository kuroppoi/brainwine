package brainwine.gameserver.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Calendar;

public class ValueWithExpiry<T> {
    @JsonProperty("expires_at")
    private Calendar expiresAt;
    @JsonProperty("value")
    private T value;

    private Calendar now() {
        return Calendar.getInstance();
    }

    public ValueWithExpiry() {}

    /**Make the value expire within the specified time. This uses the system clock to compute the expiration date.
     *
     * @param value wrapped value
     * @param duration duration to be parsed with {@link DateTimeUtils#parseFormattedDuration(String)}
     */
    public ValueWithExpiry(T value, String duration) {
        this.expiresAt = now();
        this.expiresAt.add(Calendar.MINUTE, DateTimeUtils.parseFormattedDuration(duration));
        this.value = value;
    }

    /**Make the value expired at the specified time.
     *
     * @param value wrapped value
     * @param expiresAt duration
     */
    public ValueWithExpiry(T value, Calendar expiresAt) {
        this.expiresAt = expiresAt;
        this.value = value;
    }

    /**Get a ValueWithExpiry that is guaranteed to be expired. Do not use the contained value in the returned instance.
     *
     * @return an expired ValueWithExpiry instance
     * @param <T> type of the contained value. Assume the value to be null.
     */
    public static <T> ValueWithExpiry<T> getExpired() {
        ValueWithExpiry<T> value = new ValueWithExpiry<>();
        value.expiresAt = null;
        value.value = null;
        return value;
    }

    /**Check if the value is expired according to the system clock
     *
     * @return whether the value is expired. Note that it will return true also if the expiration date is null.
     */
    @JsonIgnore
    public boolean isExpired() {
        return isExpired(Calendar.getInstance());
    }

    /**Check if the value is expired according to some assumed current time
     *
     * @param now the assumed current time
     * @return whether the value is expired. Note that it will return true also if the expiration date is null.
     */
    public boolean isExpired(Calendar now) {
        return expiresAt == null || expiresAt.before(now);
    }

    /**Return the time until expiry in milliseconds
     *
     * @param currentTime the time since epoch
     * @return
     */
    public long getTimeUntilExpiry(long currentTime) {
        return expiresAt.getTimeInMillis() - currentTime;
    }

    /**Return the wrapped value. This doesn't check if the value is expired
     *
     * @return
     */
    public T getValue() {
        return value;
    }
}
