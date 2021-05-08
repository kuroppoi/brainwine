package brainwine.gameserver.msgpack.models;

import brainwine.gameserver.server.requests.DialogRequest;

/**
 * For {@link DialogRequest}
 * TODO Figure out more about all this.
 */
public class DialogInputData {
    
    private String dialogName;
    private int dialogId;
    private String[] inputData;
    private String action;
    
    public DialogInputData(String dialogName) {
        this.dialogName = dialogName;
    }
    
    public DialogInputData(int dialogId, String[] inputData) {
        this.dialogId = dialogId;
        this.inputData = inputData;
    }
    
    public DialogInputData(int dialogId, String action) {
        this.dialogId = dialogId;
        this.action = action;
    }
    
    public boolean isType1() {
        return dialogName != null;
    }
    
    public boolean isType2() {
        return dialogId != 0 && inputData != null;
    }
    
    public boolean isType3() {
        return dialogId != 0 && action != null;
    }
    
    public String getDialogName() {
        return dialogName;
    }
    
    public int getDialogId() {
        return dialogId;
    }
    
    public String[] getInputData() {
        return inputData;
    }
    
    public String getAction() {
        return action;
    }
}
