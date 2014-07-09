package org.isis.logserver.message;

import java.sql.Date;

/** IOC Log Message information
 *  @author Katia Danilova, Kay Kasemir
 */
public class ClientMessageInfo {
	private long msg_id;
	private int msg_count;
	private String client_name;
	private String msg_text;
	private Date date;
	private String type;
	private String severity;
	private String name;
	private String eventtime;
	private String user;
	private String appl_id;

    public ClientMessageInfo(String client_name, long msg_id, String msg_text, int msg_count)
    {
    	this.client_name = client_name;
        this.msg_id = msg_id;
        this.msg_text = msg_text.trim();
        this.msg_count = msg_count;
    }
    
    public ClientMessageInfo(String client_name, String msg_text, int msg_count)
    {
    	this.client_name = client_name;      
        this.msg_text = msg_text.trim();
        this.msg_count = msg_count;
    }


    public ClientMessageInfo(long id, Date date, String type, String name, String eventtime, String severity,
            String host, String user, String text, int repeated, String appl_id )
    {
    	this.client_name = host;
        this.msg_id = id;
        this.msg_text = text.trim();
        this.msg_count = repeated;
        this.date=date;
        this.type=type;
        this.severity=severity;
        this.setName(name);
        this.eventtime=eventtime;
        this.user=user;
        this.appl_id=appl_id; 
    }


    /** @return msg_id */
    public long getMsgId()
    {
        return msg_id;
    }


    /**
     * @param msg_id the msg_id to set
     */
    public void setMsgId(long msg_id)
    {
        this.msg_id = msg_id;
    }


    /** @return msg_count */
    public int getMsgCount()
    {
        return msg_count;
    }

    /**
     * @param msg_id the msg_id to set
     */
    public void setMsgCount(int count)
    {
        this.msg_count = count;
    }

    /** @return client_name */
    public String getClientName()
    {
        return client_name;
    }   

    /** @return msg_text */
    public String getMsgText()
    {
        return msg_text;
    }
    
    
    /** @return date */
    public Date getDate()
    {
        return date;
    }


    /** @return type */
    public String getType()
    {
        return type;
    }

    
    /** @return eventtime */
    public String getEventtime()
    {
        return eventtime;
    }


    /** @return severity */
    public String getSeverity()
    {
        return severity;
    }


    /** @return user */
    public String getUser()
    {
        return user;
    }


    /** @return appl_id */
    public String getApplId()
    {
        return appl_id;
    }


	public void setName(String name) {
		this.name = name;
	}


	public String getName() {
		return name;
	}


}
