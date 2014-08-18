package org.isis.logserver.message;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MessageMatcher {

	private static final String[] FILE_HEADER = {
				"# Suppression file",
				"# Each line is a regular expression. Matching messages will be suppressed",
				"#",
				"# Suppress \"*** *** ***\" decorations and extra text logged by autosave",
				"#"
	};
	
	private static final String[] DEFAULT_PATTERNS = {
		"\\*\\*\\* \\*\\*\\*.*",
		"(save_restore)*.*(Can't write new backup file).*",
		"(save_file: Backup file)*.*(is bad).*",
		"errlog = [0-9]+ messages were discarded",
		".*tPortmapd.*panic.*"		
	};
	
    /** List of messages to suppress  */
    private List <String> patterns = new ArrayList<String>();
    
    

    /**Constructor */
	public MessageMatcher(final String filename) throws Exception
	{
		File f = new File(filename);
		
		if(!f.exists()) 
		{ 
			System.out.println("The specified file: '" + filename + "' does not exist. Creating default suppressions file.");
			setDefaultSuppressions();
			saveSuppressionsToFile(filename);
			return;
		}
		
		try(FileInputStream fstream = new FileInputStream(filename);
				DataInputStream in = new DataInputStream(fstream);
		        BufferedReader br = new BufferedReader(new InputStreamReader(in));)
		{	         
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
		       //System.out.println ("Suppression pattern: " + strLine);
		       addExpression(strLine);
		     }
		     
		     System.out.println ("Suppression patterns loaded from file: '" + filename + "'");
		}
		catch (Exception ex)
		{
			System.out.println("Error reading suppressions file; using defaults.");
			setDefaultSuppressions();
		}
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
    
	/**
	 * Saves the current configuration to the specified file.
	 */
	public void saveSuppressionsToFile(String filename)
	{
		try(PrintWriter out = new PrintWriter(filename))
		{
			out.println(patternsToText());
		} 
		catch (FileNotFoundException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private String patternsToText()
	{
		StringBuilder patternsStr = new StringBuilder();	
		
		String lineSep = System.lineSeparator();
		
		for(String headerLine: FILE_HEADER)
		{
			patternsStr.append(headerLine + lineSep);
		}
		
		for(String pattern: patterns)
		{
			patternsStr.append(pattern + lineSep);
		}
		
		return patternsStr.toString();
	}
	
	private void setDefaultSuppressions()
	{
		patterns.clear();
		
		for(String suppression: DEFAULT_PATTERNS)
		{
			addExpression(suppression);
		}
	}
}
