/*
 * 
 */
package uk.ac.ebi.fg.myequivalents.webservices.test;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * TODO: Comment me!
 *
 * <dl><dt>date</dt><dd>Sep 11, 2012</dd></dl>
 * @author Marco Brandizi
 *
 */
public class WebTestDataInitializer implements ServletContextListener
{

	@Override
	public void contextDestroyed ( ServletContextEvent e )
	{
	}

	@Override
	public void contextInitialized ( ServletContextEvent e )
	{
		if ( !"true".equals ( System.getProperty ( "uk.ac.ebi.fg.myequivalents.test_flag", null ) ) ) return;
		
		System.out.println ( "\n\n _________________________________ Creating Test Data ________________________________ \n\n\n" );
		
		// TODO
	}

}
