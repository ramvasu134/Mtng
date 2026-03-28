package com.Mtng.Mtng.model;

/**
 * SignalMessage – WebRTC signaling payload sent over STOMP WebSocket.
 *
 * <p>Types used:</p>
 * <ul>
 *   <li>join         – new participant announces presence to room</li>
 *   <li>offer        – WebRTC SDP offer (initiator → responder)</li>
 *   <li>answer       – WebRTC SDP answer (responder → initiator)</li>
 *   <li>ice-candidate – ICE candidate exchange between peers</li>
 *   <li>leave        – participant leaving the room</li>
 * </ul>
 */
public class SignalMessage {

    private String type;   // join | offer | answer | ice-candidate | leave | chat | mic-toggle
    private String from;   // sender's username
    private String to;     // target username (null = broadcast to all)
    private String data;   // SDP JSON or ICE candidate JSON or chat text
    private String displayName;  // sender's display name
    private String target;  // alias for 'to' for frontend compatibility
    private String sender;  // alias for 'from' for frontend compatibility
    private String message; // alias for 'data' for chat messages
    private String candidate; // ICE candidate JSON string
    private Boolean micOn;  // mic toggle state

    public SignalMessage() {}

    public SignalMessage(String type, String from, String to, String data) {
        this.type = type;
        this.from = from;
        this.to   = to;
        this.data = data;
    }

    public String getType()              { return type; }
    public void   setType(String type)   { this.type = type; }

    public String getFrom()              { return from; }
    public void   setFrom(String from)   { this.from = from; }

    public String getTo()                { return to; }
    public void   setTo(String to)       { this.to = to; }

    public String getData()              { return data; }
    public void   setData(String data)   { this.data = data; }

    public String getDisplayName()       { return displayName; }
    public void   setDisplayName(String displayName) { this.displayName = displayName; }

    public String getTarget()            { return target; }
    public void   setTarget(String target) { this.target = target; }

    public String getSender()            { return sender; }
    public void   setSender(String sender) { this.sender = sender; }

    public String getMessage()           { return message; }
    public void   setMessage(String message) { this.message = message; }

    public String getCandidate()         { return candidate; }
    public void   setCandidate(String candidate) { this.candidate = candidate; }

    public Boolean getMicOn()            { return micOn; }
    public void   setMicOn(Boolean micOn) { this.micOn = micOn; }
}

