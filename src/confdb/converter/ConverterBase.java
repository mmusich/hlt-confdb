package confdb.converter;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import confdb.data.IConfiguration;
import confdb.db.ConfDB;

public class ConverterBase 
{
    private ConfDB database = null;
    private ConverterEngine converterEngine = null;
	
    private HashMap<Integer, ConfWrapper> confCache = new HashMap<Integer, ConfWrapper>();
    private int maxCacheEntries = 10;
	
    public ConverterBase( String format, Connection connection ) throws ConverterException
    {
    	database = new ConfDB();
    	try {
    		database.connect( connection );
    		converterEngine = ConverterFactory.getConverterEngine( format );
    	} catch (Exception e) {
    		throw new ConverterException( "can't construct converter", e );
    	}
    }
	
    public ConverterBase( String format, String dbType, String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
    {
    	this( format );
    	initDB( dbType, dbUrl, dbUser, dbPwrd );
    }
  
    protected ConverterBase( String format ) throws ConverterException
    {
		try {
			converterEngine = ConverterFactory.getConverterEngine( format );
		} catch (Exception e) {
    		throw new ConverterException( "can't construct converter", e );
		}
    }
    
    protected void initDB( String dbType, String dbUrl, String dbUser, String dbPwrd ) throws ConverterException
    {
    	database = new ConfDB();
    	try {
    		database.connect( dbType, dbUrl, dbUser, dbPwrd );
    	} catch (Exception e) {
    		throw new ConverterException( "can't init database connection", e );
    	}
    }
  
    
    public int getMaxCacheEntries() 
    {
    	return maxCacheEntries;
    }

    public void setMaxCacheEntries(int maxCacheEntries) 
    {
    	this.maxCacheEntries = maxCacheEntries;
    }

    
	
    public ConfDB getDatabase() 
    {
    	return database;
    }
	
    public IConfiguration getConfiguration( int key ) throws ConverterException 
    {
    	ConfWrapper conf = confCache.get( new Integer( key ) );
    	if ( conf != null )
    		return conf.getConfiguration();
    	IConfiguration configuration = database.loadConfiguration( key );
    	if ( configuration == null )
    		throw new ConverterException( "ERROR! no config stored for key " + key );
    	put( key, configuration );
    	return configuration;
    }
		
    synchronized private void put( Integer key, IConfiguration conf )
    {
    	if ( confCache.size() > maxCacheEntries )
	    {
    		List<ConfWrapper> list = new ArrayList<ConfWrapper>( confCache.values() );
    		Collections.sort(list);
    		confCache.remove( list.get(0).getKey() );
	    }
    	confCache.put( key, new ConfWrapper( key, conf ) );
    }
		
    public ConverterEngine getConverterEngine() 
    {
    	return converterEngine;
    }

	public void setConverterEngine(ConverterEngine converterEngine) 
	{
		this.converterEngine = converterEngine;
	}

	
    private class ConfWrapper implements Comparable<ConfWrapper>
    {
    	private Integer key;
    	private IConfiguration configuration = null;
    	private long timestamp;
		
    	ConfWrapper( Integer key, IConfiguration conf ) 
    	{
    		this.key = key;
    		configuration = conf;
    		timestamp = System.currentTimeMillis();
    	}

    	IConfiguration getConfiguration() 
    	{
    		timestamp = System.currentTimeMillis();
    		return configuration;
    	}


    	public int compareTo(ConfWrapper o) 
    	{
    		return (int)(timestamp - o.timestamp);
    	}
		
    	public Integer getKey()
    	{
    		return key;
    	}
    }



}
