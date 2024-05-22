package brainwine.gameserver.player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import brainwine.gameserver.dialog.Dialog;
import brainwine.gameserver.dialog.DialogHelper;
import brainwine.gameserver.dialog.DialogListItem;
import brainwine.gameserver.dialog.DialogSection;
import brainwine.gameserver.dialog.input.DialogSelectInput;
import brainwine.gameserver.item.Item;

/**
 * Manages a trade session between two players: an initiator and a recipient.
 * 
 * The initiator is the one initiating the trade, and is the first to make an offer.
 * The recipient is the player whom the initiator makes an offer to.
 * 
 * The recipient will functionally not be aware of the trade until the initiator submits their offer,
 * and will not be part of this trade (by holding the {@code TradeSession} object) until accepting it.
 * 
 * After submitting their offer, the initiator should not be allowed to make any new offers
 * to the recipient while the trade is active, and initiating a trade with a different player
 * should result in a cancellation of the current trade first.
 * 
 * When the recipient accepts the initiator's trade request, it is their turn to make a counter-offer.
 * This works the same as before, but the "Give freely" option should be excluded from the dialog.
 * Once the counter-offer is submitted, the recipient should be blocked from making new offers
 * just like how it was with the initiator.
 * 
 * If the initiator accepts the recipient's counter-offer, one final inventory check should be done
 * and if both parties have the required items, the trade is finalized.
 * 
 * TODO Find a good way to track trades & implement achievements.
 */
public class TradeSession {
    
    /**
     * Trade states.
     */
    private enum State {
        /**
         * The initiator is currently making an offer.
         * The recipient is not aware of the trade at this point.
         */
        INITIATOR_OFFERING,
        
        /**
         * The initiator has submitted their offer and is being viewed by the recipient.
         */
        RECIPIENT_VIEWING_OFFER,
        
        /**
         * The recipient has accepted the initiator's trade request and is currently making a counter-offer.
         */
        RECIPIENT_OFFERING,
        
        /**
         * The recipient has submitted their counter-offer and is being viewed by the initiator.
         * If the initiator accepts the offer, the trade will be finalized.
         */
        INITIATOR_VIEWING_OFFER,
        
        /**
         * The trade has ended and is no longer valid.
         */
        TRADE_ENDED
    }
    
    public static final int ITEM_LIMIT = 8; // Maximum number of different items
    private final Map<Item, Integer> initiatorOffers = new HashMap<>();
    private final Map<Item, Integer> recipientOffers = new HashMap<>();
    private final Player initiator;
    private final Player recipient;
    private boolean isRecipientAware; // Whether or not the recipient is aware of this trade
    private State state = State.INITIATOR_OFFERING;
    private long timeoutAt;
    
    public TradeSession(Player initiator, Player recipient) {
        this.initiator = initiator;
        this.recipient = recipient;
    }
    
    /**
     * Called when one of the participating players drags an item to offer.
     */
    protected void onItemOffered(Player player, Item item) {
        // Do nothing if state is invalid
        if((player == initiator && state != State.INITIATOR_OFFERING) || (player == recipient && state != State.RECIPIENT_OFFERING)) {
            return;
        }
        
        Map<Item, Integer> offers = getOffers(player);
        
        // Do nothing if item limit has been reached and item is not already part of the offer
        if(offers.size() >= ITEM_LIMIT && !offers.containsKey(item)) {
            player.notify("You cannot offer any more items.");
            return;
        }
        
        Player otherPlayer = getOtherPlayer(player);
        player.showDialog(Dialogs.createQuantitySelectorDialog(player, otherPlayer, item), input -> {
            // Handle cancellation
            if(input.length == 0 || (input.length == 1 && input[0].equals("cancel"))) {
                cancel(player);
                return;
            }
            
            // Parse quantity
            int quantity = 0;
            
            try {
                quantity = Integer.parseInt(String.valueOf(input[0]));
            } catch(NumberFormatException e) {
                abort();
                return;
            }
            
            // Process input
            onItemQuantitySelected(player, item, quantity);
        });
        
        // Update timeout
        setTimeoutSeconds(10);
    }
    
    /**
     * Called by {@link #onItemOffered(Player, Item)} when the player submits an item quantity.
     */
    private void onItemQuantitySelected(Player player, Item item, int quantity) {
        // Do nothing if state is invalid
        if((player == initiator && state != State.INITIATOR_OFFERING) || (player == recipient && state != State.RECIPIENT_OFFERING)) {
            return;
        }
        
        // Abort if quantiy is invalid or if the player does not have enough of this item
        if(quantity <= 0 || !player.getInventory().hasItem(item, quantity)) {
            abort();
            return;
        }
        
        Map<Item, Integer> offers = getOffers(player);
        
        // Do nothing if item limit has been reached and item is not already part of the offer
        if(offers.size() >= ITEM_LIMIT && !offers.containsKey(item)) {
            return;
        }
        
        // Store offer
        offers.put(item, quantity);
        
        // Show offer status dialog
        if(player == initiator) {
            player.showDialog(Dialogs.createInitiatorOfferStatusDialog(recipient, offers), input -> {
                // Validate input
                if(input.length != 1) {
                    abort();
                    return;
                }
                
                // Handle action
                switch(String.valueOf(input[0])) {
                    case "Request trade":
                        onInitiatorSendTradeRequest();
                        break;
                    case "Give freely":
                        onInitiatorGiveFreely();
                        break;
                    case "cancel":
                        // Quirk: V3 auto-cancels dialogs when a new one pops up, which isn't very helpful here...
                        if(!player.isV3()) {
                            cancel(player);
                        }
                        
                        break;
                    default:
                        abort();
                        break;
                }
            });
        } else if(player == recipient) {
            player.showDialog(Dialogs.createRecipientOfferStatusDialog(initiator, initiatorOffers, offers), input -> {
                // Validate input
                if(input.length != 1) {
                    abort();
                    return;
                }
                
                // Handle action
                switch(String.valueOf(input[0])) {
                    case "Submit offer":
                        onRecipientSubmitOffer();
                        break;
                    case "Cancel":
                        // Yes, the uppercase 'C' is important.
                        cancel(player);
                        break;
                }
            });
        }
        
        // Update timeout
        setTimeoutSeconds(20);
    }
    
    /**
     * Called by {@link #onItemQuantitySelected(Player, Item, int)} when the initiator chooses to give their offer freely.
     * TODO Should there be a confirmation dialog?
     */
    private void onInitiatorGiveFreely() {
        // Do nothing if state is invalid
        if(state != State.INITIATOR_OFFERING) {
            return;
        }
        
        // End trade if recipient is currently unavailable
        if(!canTrade(recipient)) {
            initiator.showDialog(DialogHelper.messageDialog(String.format("%s cannot receive items right now -- try again in a minute.", recipient.getName())));
            end();
            return;
        }
        
        // Check initiator's inventory
        if(!checkInventory(initiator)) {
            abort("The trade could not be fulfilled.");
            return;
        }
        
        // Try to end the trade
        if(!end()) {
            return;
        }
        
        // Deliver goodies
        initiatorOffers.forEach((item, quantity) -> {
            initiator.getInventory().removeItem(item, quantity, true);
            recipient.getInventory().addItem(item, quantity, true);
        });
        
        // Show feedback
        initiator.showDialog(Dialogs.createOfferDialog(String.format("You sent free goodies to %s!", recipient.getName()), "Sent:", initiatorOffers));
        recipient.showDialog(Dialogs.createOfferDialog(String.format("You received goodies from %s!", initiator.getName()), "Received:", initiatorOffers));
    }
    
    /**
     * Called by {@link #onItemQuantitySelected(Player, Item, int)} when the initiator submits their offer.
     */
    private void onInitiatorSendTradeRequest() {
        // Do nothing if state is invalid
        if(state != State.INITIATOR_OFFERING) {
            return;
        }
        
        // Abort trade if no offers are present
        if(initiatorOffers.isEmpty()) {
            abort();
            return;
        }
        
        // End trade if recipient is unavailable
        if(!canTrade(recipient)) {
            initiator.showDialog(DialogHelper.messageDialog(String.format("%s cannot trade right now -- try again in a minute.", recipient.getName())));
            end();
            return;
        }
        
        // Update trade state
        state = State.RECIPIENT_VIEWING_OFFER;
        isRecipientAware = true;
        
        // Show feedback to initiator
        initiator.showDialog(Dialogs.createOfferDialog("Your offer has been sent:", initiatorOffers));
        
        // Show trade request dialog to recipient
        recipient.showDialog(Dialogs.createOfferDialog(String.format("%s wants to trade:", initiator.getName()), null, "Are you interested?", initiatorOffers).setActions("yesno"), input -> {
            // Handle cancellation
            if(input.length == 1 && input[0].equals("cancel")) {
                cancel(recipient);
                return;
            }
            
            onRecipientAcceptTradeRequest();
        });
        
        // Update timeout
        setTimeoutSeconds(10);
    }
    
    /**
     * Called by {@link #onInitiatorTradeRequest()} when the recipient accepts the initiator's trade request.
     */
    private void onRecipientAcceptTradeRequest() {
        // Do nothing if state is invalid
        if(state != State.RECIPIENT_VIEWING_OFFER) {
            return;
        }
        
        // Cancel recipient's existing trade session if it exists
        if(recipient.isTrading()) {
            recipient.getTradeSession().cancel(recipient);
        }
        
        // Update trade state
        state = State.RECIPIENT_OFFERING;
        recipient.setTradeSession(this);
        
        // Show feedback to initiator
        initiator.showDialog(DialogHelper.messageDialog(String.format("%s accepted your trade request.", recipient.getName()), "Their offer will be along shortly."));
        
        // Show feedback to recipient
        recipient.showDialog(Dialogs.createOfferDialog("You accepted a trade request for:", null,
                String.format("Drag the item you'd like to trade to %s, then select the amount to offer.", initiator.getName()), initiatorOffers));
        
        // Update timeout
        setTimeoutSeconds(20);
    }
    
    /**
     * Called by {@link #onItemQuantitySelected(Player, Item, int)} when the recipient submits their counter-offer.
     */
    private void onRecipientSubmitOffer() {
        // Do nothing if state is invalid
        if(state != State.RECIPIENT_OFFERING) {
            return;
        }
        
        // Abort trade if no offers are present
        if(recipientOffers.isEmpty()) {
            abort();
            return;
        }
        
        // Update state
        state = State.INITIATOR_VIEWING_OFFER;
        
        // Show feedback to recipient
        recipient.showDialog(Dialogs.createOfferDialog("Your offer has been sent:", recipientOffers));
        
        // Show the recipient's offer to the initiator
        initiator.showDialog(Dialogs.createFinalOfferDialog(initiator, recipient, initiatorOffers, recipientOffers), input -> {
            // Handle cancellation
            if(input.length == 1 && input[0].equals("cancel")) {
                cancel(initiator);
                return;
            }
            
            // Finalize the trade
            complete();
        });
        
        // Update timeout
        setTimeoutSeconds(20);
    }
    
    /**
     * Called by {@link #onRecipientSubmitOffer()} when the initiator accepts the recipient's counter-offer.
     * Checks both parties' inventories and completes the trade if everything checks out.
     */
    private void complete() {
        // Check inventory of both players
        if(!checkInventory(initiator) || !checkInventory(recipient)) {
            abort("The trade could not be fulfilled.");
            return;
        }
        
        // Try to end the trade
        if(!end()) {
            return;
        }
        
        // Update inventories
        initiatorOffers.forEach((item, quantity) -> {
            initiator.getInventory().removeItem(item, quantity, true);
            recipient.getInventory().addItem(item, quantity, true);
        });
        
        recipientOffers.forEach((item, quantity) -> {
            recipient.getInventory().removeItem(item, quantity, true);
            initiator.getInventory().addItem(item, quantity, true);
        });
        
        // Show trade completion dialog
        initiator.showDialog(Dialogs.createOfferDialog(String.format("You traded with %s.", recipient.getName()), "Received:", recipientOffers));
        recipient.showDialog(Dialogs.createOfferDialog(String.format("You traded with %s.", initiator.getName()), "Received:", initiatorOffers));
    }
    
    /**
     * @param player The player whose inventory to be check.
     * @return {@code true} if the player has enough of each item they have offered, otherwise {@code false}.
     */
    private boolean checkInventory(Player player) {
        Map<Item, Integer> offers = getOffers(player);
        
        for(Entry<Item, Integer> entry : offers.entrySet()) {
            if(!player.getInventory().hasItem(entry.getKey(), entry.getValue())) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Calls {@link #abort(String)} with a generic message.
     */
    private void abort() {
        abort("The trade was cancelled due to an error.");
    }
    
    /**
     * Aborts the trade and notifies the participants with the specified message.
     * The recipient will not be notified of the cancellation if they aren't aware of the trade.
     * 
     * @param message The message to display. Can be {@code null}, in which case no message will be shown.
     */
    private void abort(String message) {
        // Do nothing if the trade has already ended
        if(!end()) {
            return;
        }
        
        // Don't notify if message isn't present
        if(message == null) {
            return;
        }
        
        initiator.showDialog(DialogHelper.messageDialog(message));
        
        // Only notify recipient if they are aware of this trade
        if(isRecipientAware) {
            recipient.showDialog(DialogHelper.messageDialog(message));
        }
    }
    
    /**
     * Cancels the trade and notifies the participants.
     * The recipient will not be notified of the cancellation if they aren't aware of the trade.
     * 
     * @param canceller The player who cancelled the trade.
     */
    public void cancel(Player canceller) {
        // Do nothing if the trade has already ended
        if(!end()) {
            return;
        }
        
        String message = String.format("%s cancelled the trade.", canceller.getName());
        String cancellerMessage = String.format("You cancelled the trade with %s.", getOtherPlayer(canceller).getName());
        initiator.showDialog(DialogHelper.messageDialog(initiator == canceller ? cancellerMessage : message));
        
        // Only notify recipient if they are aware of this trade
        if(isRecipientAware) {
            recipient.showDialog(DialogHelper.messageDialog(recipient == canceller ? cancellerMessage : message));
        }
    }
    
    /**
     * Ends the trade if it has timed out due to an action taking too long.
     */
    public void timeout() {
        // Do nothing if it's not time yet
        if(System.currentTimeMillis() < timeoutAt) {
            return;
        }
        
        // Try to end the trade
        if(!end()) {
            return;
        }
        
        initiator.showDialog(DialogHelper.messageDialog(String.format("Your trade with %s has timed out.", recipient.getName())));
        
        // Only notify recipient if they are aware of this trade
        if(isRecipientAware) {
            recipient.showDialog(DialogHelper.messageDialog(String.format("Your trade with %s has timed out.", initiator.getName())));
        }
    }
    
    /**
     * Sets the state to {@link State#TRADE_ENDED} and sets the trade session of both players to {@code null}
     * if they are in this trade according to {@link #isTradeCurrent(Player)}
     * 
     * @return {@code false} if the trade has already been ended, otherwise {@code true}.
     */
    private boolean end() {
        if(state == State.TRADE_ENDED) {
            return false;
        }
        
        if(isTradeCurrent(initiator)) initiator.setTradeSession(null);
        if(isTradeCurrent(recipient)) recipient.setTradeSession(null);
        state = State.TRADE_ENDED;
        return true;
    }
    
    /**
     * Updates the timeout value.
     * 
     * @param seconds The amount of seconds until the trade should timeout.
     */
    private void setTimeoutSeconds(int seconds) {
        timeoutAt = System.currentTimeMillis() + seconds * 1000L;
    }
    
    /**
     * @return The current offers of the given player, or {@code null} if the player is not part of this trade.
     */
    private Map<Item, Integer> getOffers(Player player) {
        return player == initiator ? initiatorOffers : player == recipient ? recipientOffers : null;
    }
    
    /**
     * @return The other trade participant that is not the specified one, or {@code null} if the player is not part of this trade.
     */
    private Player getOtherPlayer(Player player) {
        return player == initiator ? recipient : player == recipient ? initiator : null;
    }
    
    /**
     * @return {@code true} if the given player is either the initiator or recipient of this trade, otherwise {@code false}.
     */
    public boolean isParticipant(Player player) {
        return player == initiator || player == recipient;
    }
    
    /**
     * @return {@code true} if the given player is available to trade right now, otherwise {@code false}.
     */
    public boolean canTrade(Player player) {
        return recipient.isOnline() && recipient.getZone() == initiator.getZone() && !recipient.isTrading();
    }
    
    /**
     * @return {@code true} if the given player is currently in this trade, otherwise {@code false}.
     * 
     * A player is considered to be part of the trade when they are either:
     * - The initiator of the trade
     * - The recipient and have accepted the initiator's request to trade
     */
    private boolean isTradeCurrent(Player player) {
        return player.getTradeSession() == this;
    }
    
    /**
     * Helper class for creating trading-related dialogs.
     */
    private static class Dialogs {
        
        public static final List<String> ITEM_QUANTITY_OPTIONS =
                Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "15", "20", "25", "30", "40", "50", "75", "100", "200", "500", "1000", "5000", "25000", "100000");
        
        public static Dialog createQuantitySelectorDialog(Player offerer, Player target, Item item) {
            // Get quantity options that are available to the player
            List<String> quantityOptions = ITEM_QUANTITY_OPTIONS.stream()
                    .filter(quantity -> offerer.getInventory().hasItem(item, Integer.parseInt(quantity)))
                    .collect(Collectors.toList());
            
            return new Dialog()
                .addSection(new DialogSection()
                    .setTitle(String.format("Trade with %s", target.getName())))
                .addSection(new DialogSection()
                    .setText(String.format("Quantity of %s to trade:", item.getTitle()))
                    .setInput(new DialogSelectInput()
                        .setOptions(quantityOptions)
                        .setKey("quantity")));
        }
        
        public static Dialog createInitiatorOfferStatusDialog(Player recipient, Map<Item, Integer> offers) {
            Dialog dialog = createOfferDialog("Your current offer:", offers);
            
            // Add multi-item trading hint if the item limit hasn't been reached yet
            if(offers.size() < TradeSession.ITEM_LIMIT) {
                dialog.addSection(new DialogSection()
                    .setText(String.format("Drag another item to %s to include it in your offer. "
                            + "You can trade up to %s different items at the same time this way.", recipient.getName(), TradeSession.ITEM_LIMIT)));
            }
            
            dialog.addSection(new DialogSection()
                .setInput(new DialogSelectInput()
                    .setOptions("Request trade", "Give freely")
                    .setKey("type")));
            
            return dialog;
        }
        
        public static Dialog createRecipientOfferStatusDialog(Player initiator, Map<Item, Integer> initiatorOffers, Map<Item, Integer> recipientOffers) {
            Dialog dialog = createOfferDialog(String.format("%s's offer:", initiator.getName()), initiatorOffers).setActions("Cancel", "Submit offer");
            dialog.addSection(createOfferSection(recipientOffers).setTitle("Your current offer:"));
            
            // Add multi-item trading hint if the item limit hasn't been reached yet
            if(recipientOffers.size() < TradeSession.ITEM_LIMIT) {
                dialog.addSection(new DialogSection()
                    .setText(String.format("Drag another item to %s to include it in your offer. "
                            + "You can trade up to %s different items at the same time this way.", initiator.getName(), TradeSession.ITEM_LIMIT)));
            }
            
            return dialog;
        }
        
        public static Dialog createFinalOfferDialog(Player initiator, Player recipient, Map<Item, Integer> initiatorOffers, Map<Item, Integer> recipientOffers) {
            return createOfferDialog(String.format("%s has offered:", recipient.getName()), recipientOffers).setActions("yesno")
                .addSection(createOfferSection(initiatorOffers).setText("For your:"))
                .addSection(new DialogSection().setText("Do you accept this trade?"));
        }
        
        public static Dialog createOfferDialog(String title, Map<Item, Integer> offer) {
            return createOfferDialog(title, null, offer);
        }
        
        public static Dialog createOfferDialog(String title, String text, Map<Item, Integer> offer) {
            return createOfferDialog(title, text, null, offer);
        }
        
        public static Dialog createOfferDialog(String title, String text, String footer, Map<Item, Integer> offer) {
            Dialog dialog = new Dialog().addSection(createOfferSection(offer).setTitle(title).setText(text));
            
            if(footer != null) {
                dialog.addSection(new DialogSection().setText(footer));
            }
            
            return dialog;
        }
        
        private static DialogSection createOfferSection(Map<Item, Integer> offers) {
            DialogSection section = new DialogSection();
            offers.forEach((item, quantity) -> {
                section.addItem(new DialogListItem().setItem(item.getCode())
                    .setImage(String.format("inventory/%s", item.getId()))
                    .setText(String.format("%s x %s", item.getTitle(), quantity)));
            });
            return section;
        }
    }
    
}
