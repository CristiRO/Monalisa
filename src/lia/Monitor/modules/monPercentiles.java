package lia.Monitor.modules;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import lia.Monitor.monitor.MCluster;
import lia.Monitor.monitor.MFarm;
import lia.Monitor.monitor.MNode;
import lia.Monitor.monitor.MonModuleInfo;
import lia.Monitor.monitor.MonitoringModule;
import lia.Monitor.monitor.Result;
import lia.util.Utils;
import lia.util.DynamicThreadPoll.SchJob;
import lia.util.ntp.NTPDate;

public class monPercentiles extends SchJob implements MonitoringModule {
	private MonModuleInfo mmi = null;
    private MNode mn = null;
    
    // Local port forwarding: `ssh -L 9200:alissandra02.cern.ch:9200 cmargine@lxplus.cern.ch`
    
    private String sHost = "localhost";
    private int    iPort = 9200;
    
    // parameters: percentiles (but don't remove the defaults)
    
    private static final Logger logger = Logger.getLogger(monPercentiles.class.getName());
    	
	@Override
	public MonModuleInfo init(MNode node, String args) {
		// parse the host and port from node
		mn = node;
		
		mmi = new MonModuleInfo();
		mmi.setName("AliEnPercentile");
		mmi.setState(0);
		
		String sError = null;
		try{
		    sHost = args;
		    
		    if (sHost.indexOf("://") >0 && sHost.indexOf("://") < 10){
			sHost = sHost.substring(sHost.indexOf("://")+3);
		    }
		    
		    if (sHost.indexOf("/")>0){
			//sURL  = sHost.substring(sHost.indexOf("/"));
			sHost = sHost.substring(0, sHost.indexOf("/"));
		    }
		    
		    if (sHost.indexOf(":")>0){	
			iPort = Integer.parseInt(sHost.substring(sHost.indexOf(":")+1));
			sHost = sHost.substring(0, sHost.indexOf(":"));
		    }
		}
		catch (Exception e){
		    sError = e.getMessage();
		}
		
		if ( sError != null ){
		    mmi.addErrorCount();
		    mmi.setState(1);	// error
		    mmi.setErrorDesc(sError);
		    return mmi;
		}
		
		mmi.lastMeasurement = NTPDate.currentTimeMillis();
		lLastProcess = mmi.lastMeasurement - 120000; // minus 2 minutes
		
		logger.log(Level.INFO, "Percentiles module initiated");
		
		return mmi;
	}
    
	@Override
	public String[] ResTypes() {
		final String[] resTypes = {"p50.0", "p75.0", "p95.0", "p99.0", "p99.9", "p99.99" };
		return resTypes;
	}
	
	@Override
	public String getOsName() {
		return "Linux";
	}
	
    private long   lLastProcess  = 0; 
	
	@Override
	public Object doProcess() throws Exception {
		if (mmi.getState()!=0){
		    throw new IOException("there was some exception during init ...");
		}
		
		long ls        = NTPDate.currentTimeMillis();
		
		Result er      = new Result();
		er.FarmName    = getFarmName();
		er.ClusterName = getClusterName();
		er.NodeName    = mn.getName();
		er.Module      = mmi.getName();
		er.time        = ls;
		
		try {
			logger.log(Level.INFO, "Percentiles module sending requests");
			ElasticsearchQueryCreator creator = new ElasticsearchQueryCreator("http://localhost", Integer.toString(iPort));
			logger.log(Level.INFO, "Created the creator");
			final Map<Double, Double> res;
			
			long currentProcess = NTPDate.currentTimeMillis();
			Set<Double> percentiles = Set.of(50.0, 75.0, 95.0, 99.0, 99.9, 99.99);
			String index = "logstash-old-*";
			String field = "elapsed";
			logger.log(Level.INFO, "Trying to get the result");
			res = creator.getPercentilesForIndex(Long.toString(lLastProcess), Long.toString(currentProcess), percentiles, index, field);
			logger.log(Level.INFO, "Got the result! Parsing...");
			for (Double key : res.keySet()) {
				er.addSet("p" + key.toString(),res.get(key));
			}
			
			er.addSet("Testing_value", 1.0);
		}catch (Exception e){
		    System.err.println("Exception while parsing : "+e+" ( "+e.getMessage()+" )");
		    e.printStackTrace();
		}
		
		if (er.param_name!=null && er.param_name.length>0){
			logger.log(Level.INFO, "Sending it to the moon!");
			System.out.println(er.toString());
		    return er;
		}
		
		return null;
	}	

	@Override
	public String getClusterName() {
		return mn.getClusterName();
	}

	@Override
	public String getFarmName() {
		return mn.getFarmName();
	}

	@Override
	public MonModuleInfo getInfo() {
		return mmi;
	}

	@Override
	public MNode getNode() {
		return mn;
	}

	@Override
	public String getTaskName() {
		return mmi.getName();
	}

	@Override
	public boolean isRepetitive() {
		return true;
	}

    /**
     * Debug method
     * 
     * @param args command line arguments
     * @throws Exception 
     */
    public static void main(String args[]) throws Exception {
        MFarm f = new MFarm("myFarm");
        MCluster c = new MCluster("myCluster", f);
        MNode n = new MNode("sensors", c, f);

        monPercentiles m = new monPercentiles();
        m.init(n, args.length > 0 ? args[0] : null);

        Utils.dumpResults(m.doProcess());
    }
}
