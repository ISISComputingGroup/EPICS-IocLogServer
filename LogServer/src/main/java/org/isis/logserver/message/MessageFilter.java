package org.isis.logserver.message;

import java.util.HashMap;
import java.util.Map;

/** Message filter
*
*  Determines if the text message is new or repeated
*  If this is a new message, it's info is added to messages map
*  If this is a repeated message, number of repeats for this message is updated in messages map
*  @author Katia Danilova, Kay Kasemir
*/
public class MessageFilter
{
    final private static MessageFilter instance = new MessageFilter();

    /** Map of client to MessageState, keeps track of messages status */
    final private Map<String, MessageState> messages = new HashMap<String, MessageState>();

    /** Private constructor for singleton */
	private MessageFilter()
	{
		// Nothing to do
	}

    /** The one and only singleton instance */
    public static MessageFilter getInstance()
    {
        return instance;
    }

    /** Saves new message to messages map or updates message status
     *  @param client host name
     *  @param message_text
     *  @return MessageState
     */
    public MessageState checkMessageState(final String client, final String message_text)
    {
        MessageState state = messages.get(client);
        if (state == null)
        {   // New client, add to map        	
            state = new MessageState(message_text);
            messages.put(client, state);
        }
        else
        {   // Known client, check if it's the same message        	
            state.updateMessageState(message_text);
        }
        return state;
    }
}
