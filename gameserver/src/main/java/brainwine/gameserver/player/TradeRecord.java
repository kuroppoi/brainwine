package brainwine.gameserver.player;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;

import brainwine.gameserver.item.Item;
import brainwine.gameserver.item.ItemRegistry;

/**
 * Record containing data of a successfully completed {@link TradeSession}.
 * If the trade was a gift, {@code recipientOffers} will be empty which can be checked using {@link TradeRecord#isComplimentary()}.
 */
public class TradeRecord {
    
    private OffsetDateTime date;
    private String initiator;
    private String recipient;
    private Map<Item, Integer> initiatorOffers;
    private Map<Item, Integer> recipientOffers;
    
    @JsonCreator
    private TradeRecord() {}
    
    @JsonIgnore
    protected TradeRecord(String initiator, String recipient, Map<Item, Integer> initiatorOffers, Map<Item, Integer> recipientOffers) {
        this.date = OffsetDateTime.now();
        this.initiator = initiator;
        this.recipient = recipient;
        this.initiatorOffers = new HashMap<>(initiatorOffers);
        this.recipientOffers = new HashMap<>(recipientOffers);
    }
    
    public OffsetDateTime getDate() {
        return date;
    }
    
    public String getInitiator() {
        return initiator;
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public Map<Item, Integer> getInitiatorOffers() {
        return Collections.unmodifiableMap(initiatorOffers);
    }
    
    public Map<Item, Integer> getRecipientOffers() {
        return Collections.unmodifiableMap(recipientOffers);
    }
    
    @JsonIgnore
    public boolean isComplimentary() {
        return recipientOffers.isEmpty();
    }
    
    @JsonIgnore
    public boolean isEarthBomb() {
        Item earth = ItemRegistry.getItem("ground/earth");
        return isComplimentary() && initiatorOffers.size() == 1 && initiatorOffers.getOrDefault(earth, 0) == 1000;
    }
}
