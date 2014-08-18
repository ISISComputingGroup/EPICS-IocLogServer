package org.isis.logserver.message;

/** 
 *  IOC Log Message status. 
 *  Has information about current message (message text, number of repeats, message_id);
 *  Keeps previous message info (message_id and number of counts).
 *  @author Kay Kasemir
 */
public class MessageState
{
    private String message_text;
    private int repeat_count = 0;
    private long id = -1;
    private int previous_repeat_count = 0;
    private long previous_id = -1;

    public MessageState(final String message_text)
    {
        this.message_text = message_text;
    }

    public boolean isNewMessage()
    {
        return id < 0;
    }

    public int getRepeatCount()
    {
        return repeat_count;
    }

    public long getMessageID()
    {
        return id;
    }

    public void setMessageID(final long id)
    {
        if (this.id >= 0)
            throw new Error("Message ID cannot be changed for existing message");
        this.id = id;
    }

    void updateMessageState(final String new_message_text)
    {    	
        if (message_text.equals(new_message_text))  //same text
        {        	
            ++repeat_count;
        }
        else  //new text
        {        	
            previous_repeat_count = repeat_count;
            previous_id = id;
            repeat_count = 0;
            id = -1;
            message_text = new_message_text;
        }
    }

    public boolean hadPreviousMessage()
    {
        return previous_id >= 0;
    }

    public int getPreviousRepeatCount()
    {
        return previous_repeat_count;
    }

    public long getPreviousMessageID()
    {
        return previous_id;
    }
}
