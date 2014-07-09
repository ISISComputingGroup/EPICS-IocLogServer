package org.isis.logserver.message;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessageMatcher {

    /** List of messages to suppress  */
    private List <String> patterns = new ArrayList<String>();

    /**Constructor */
	public MessageMatcher(final String filename) throws Exception
	{
	     // Open the file
	     FileInputStream fstream = new FileInputStream(filename);
	     // Get the object of DataInputStream
	     DataInputStream in = new DataInputStream(fstream);
	         BufferedReader br = new BufferedReader(new InputStreamReader(in));
	     String strLine;
	     //Read File Line By Line
	     while ((strLine = br.readLine()) != null)
	     {
	         strLine = strLine.trim();
             //skip empty lines:
	         if (strLine.length() <= 0)
	             continue;
	    	 //skip comments:
	    	 if(strLine.startsWith("#"))
	    	     continue;
	       System.out.println ("Suppression pattern: " + strLine);
	       addExpression(strLine);
	     }
	     //Close the input stream
	     in.close();
	}


    public void addExpression(final String expression)
    {
        patterns.add(expression);
    }

    /** Checks if message text is should be suppressed
     * @param message_text String
     * @return true if this message should be suppressed
     *
     *  */
    public boolean check(final String message_text)
    {
    	for(int i=0; i<patterns.size(); i++)
    	{
    	    final String patternStr = patterns.get(i);
    	    // Create the pattern
    	    final Pattern pattern = Pattern.compile(patternStr);
    	    final Matcher matcher = pattern.matcher(message_text);

	        if (matcher.find())
	            return true;
    	}
    	return false;
    }
}
