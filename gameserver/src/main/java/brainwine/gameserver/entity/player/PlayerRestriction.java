package brainwine.gameserver.entity.player;

import java.time.OffsetDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIncludeProperties;

// TODO issuers should be Player objects, but there is no practical reason for this as issuers are only saved 
// for record keeping as of right now.
@JsonIncludeProperties({"issuer", "reason", "start_date", "end_date", "pardon_date", "pardon_issuer"})
public class PlayerRestriction {
    
    private String issuer;
    private String reason;
    private OffsetDateTime startDate;
    private OffsetDateTime endDate;
    private OffsetDateTime pardonDate;
    private String pardonIssuer;
    
    @JsonCreator
    private PlayerRestriction() {}
    
    public PlayerRestriction(Player issuer, String reason, OffsetDateTime endDate) {
        this.issuer = issuer == null ? null : issuer.getDocumentId();
        this.reason = reason;
        this.endDate = endDate;
        startDate = OffsetDateTime.now();
    }
    
    public String getIssuer() {
        return issuer;
    }
    
    public String getReason() {
        return reason;
    }
    
    public boolean isActive() {
        return !isPardoned() && OffsetDateTime.now().isBefore(endDate);
    }
    
    public OffsetDateTime getStartDate() {
        return startDate;
    }
    
    public OffsetDateTime getEndDate() {
        return endDate;
    }
    
    public void pardon(Player issuer) {
        if(!isPardoned()) {
            pardonIssuer = issuer == null ? null : issuer.getDocumentId();
            pardonDate = OffsetDateTime.now();
        }
    }
    
    public boolean isPardoned() {
        return pardonDate != null;
    }
    
    public OffsetDateTime getPardonDate() {
        return pardonDate;
    }
    
    public String getPardonIssuer() {
        return pardonIssuer;
    }
}
