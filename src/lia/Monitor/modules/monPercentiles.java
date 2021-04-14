package lia.Monitor.modules;

import java.io.IOException;
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
	private static final Logger logger = Logger.getLogger(monPercentiles.class.getName());
	private MonModuleInfo mmi = null;
    private MNode mn = null;
    
    // Local port forwarding: `ssh -L 9200:alissandra02.cern.ch:9200 <user>@lxplus.cern.ch`
    
    // default values for AliEn
    // localhost, 9200, (50.0; 75.0; 95.0; 99.0; 99.9; 99.99), logstash-old-*, elapsed

    // default values for JAliEn
    // localhost, 9200, (50.0; 75.0; 95.0; 99.0; 99.9; 99.99), logstash-new-*, duration

    private String sHost = "localhost";
    private int    iPort = 9200;
    private String[] resTypes = {"p50.0", "p75.0", "p95.0", "p99.0", "p99.9", "p99.99" };
    private Set<Double> percentiles = Set.of(50.0, 75.0, 95.0, 99.0, 99.9, 99.99);
    private String indexPattern = "logstash-new-*";
    private String timestampFieldName = "duration";
    
    private ElasticsearchQueryCreator creator;

    
	@Override
	public MonModuleInfo init(MNode node, String args) {
		mn = node;
		
		mmi = new MonModuleInfo();
		mmi.setName("AliEnPercentile");
		mmi.setState(0);
		
		String sError = null;
				
		// strip " from the start and end of args
		args = args.strip();
		args = args.substring(1, args.length() - 1);
				
		try {
			String[] argArray = args.split(",");
			sHost = argArray[0].strip();
			iPort = Integer.parseInt(argArray[1].strip());
			
			String s = argArray[2].strip();
			s = s.substring(s.indexOf("(") + 1); // start getting the content between brackets
			s = s.substring(0, s.indexOf(")"));  // end getting the content between brackets
			String[] percentilesStringArray = s.split(";");
			Double[] newPercentiles = new Double[percentilesStringArray.length];
			String[] newResTypes = new String[percentilesStringArray.length];
			for (int i = 0; i < percentilesStringArray.length; ++i) {
				String percentile = percentilesStringArray[i].strip();
				newPercentiles[i] = Double.parseDouble(percentile);
				newResTypes[i] = "p" + percentile;
			}
			resTypes = newResTypes;
			percentiles = Set.of(newPercentiles);
			
			indexPattern = argArray[3].strip();
			timestampFieldName = argArray[4].strip();
			
			creator = new ElasticsearchQueryCreator("http://" + sHost, Integer.toString(iPort));
		} catch (Exception e) {
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
		
		logger.log(Level.INFO, "Parsed host: " + sHost);
		logger.log(Level.INFO, "Parsed port: " + iPort);
		logger.log(Level.INFO, "Parsed percentiles: " + String.join(" ", resTypes));
		logger.log(Level.INFO, "Parsed index pattern: " + indexPattern);
		logger.log(Level.INFO, "Parsed timestamp field name: " + timestampFieldName);
		
		return mmi;
	}
    
	@Override
	public String[] ResTypes() {
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
			final Map<Double, Double> res;
			
			long currentProcess = NTPDate.currentTimeMillis();
			res = creator.getPercentilesForIndex(Long.toString(lLastProcess), Long.toString(currentProcess), percentiles, indexPattern, timestampFieldName);
			for (Double key : res.keySet()) {
				er.addSet("p" + key.toString(), res.get(key));
			}
		}catch (Exception e){
		    System.err.println("Exception while parsing : "+e+" ( "+e.getMessage()+" )");
		    e.printStackTrace();
		}
		
		if (er.param_name!=null && er.param_name.length>0){
		    Vector<Result> v = new Vector<>();
		    v.add(er);
			return v;
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
