package com.example.demo.dtos;

public class ChatMessage {
    private String senderName;
    private String receiverName;
    private String timestamp;
    private String receiverId;

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }


    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }






    public String getReceiverName() {
        return receiverName;
    }

    public void setReceiverName(String receiverName) {
        this.receiverName = receiverName;
    }
    public String getReceiverId(){
        return receiverId;
    }
    public String getContent(){
        return content;
    }
    public void setContent(String content){
        this.content=content;
    }

    public void setReceiverId(String receiverId){
        this.receiverId=receiverId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    private String content;
    private MessageType type;

    public enum MessageType { CHAT, JOIN, LEAVE, TYPING }
    // Getters and Setters
}
