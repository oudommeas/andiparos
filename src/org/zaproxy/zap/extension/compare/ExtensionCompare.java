/*
 * Zed Attack Proxy (ZAP) and its related class files.
 * 
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 * 
 * Copyright 2010 psiinon@gmail.com
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.zaproxy.zap.extension.compare;

import java.awt.EventQueue;
import java.io.File;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

import org.apache.commons.httpclient.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.parosproxy.paros.Constant;
import org.parosproxy.paros.db.Database;
import org.parosproxy.paros.db.RecordHistory;
import org.parosproxy.paros.db.TableHistory;
import org.parosproxy.paros.extension.ExtensionAdaptor;
import org.parosproxy.paros.extension.ExtensionHook;
import org.parosproxy.paros.extension.SessionChangedListener;
import org.parosproxy.paros.extension.report.ReportGenerator;
import org.parosproxy.paros.model.HistoryReference;
import org.parosproxy.paros.model.Model;
import org.parosproxy.paros.model.Session;
import org.parosproxy.paros.model.SessionListener;
import org.parosproxy.paros.network.HttpMalformedHeaderException;
import org.parosproxy.paros.view.View;

/**
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
public class ExtensionCompare extends ExtensionAdaptor implements SessionChangedListener, SessionListener {

	private static final String CRLF = "\r\n";
	private JMenuItem menuCompare = null;

    private static Log log = LogFactory.getLog(ExtensionCompare.class);

	/**
     * 
     */
    public ExtensionCompare() {
        super();
 		initialize();
    }

    /**
     * @param name
     */
    public ExtensionCompare(String name) {
        super(name);
    }

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
        this.setName("ExtensionCompare");
	}
	
	public void hook(ExtensionHook extensionHook) {
	    super.hook(extensionHook);
	    if (getView() != null) {
	        extensionHook.getHookMenu().addReportMenuItem(getMenuCompare());
	    }
	}
	

	public void sessionChanged(final Session session)  {
	    if (EventQueue.isDispatchThread()) {
		    sessionChangedEventHandler(session);

	    } else {
	        
	        try {
	            EventQueue.invokeAndWait(new Runnable() {
	                public void run() {
	        		    sessionChangedEventHandler(session);
	                }
	            });
	        } catch (Exception e) {
	        	log.warn(e.getMessage(), e);
	        }
	    }
	}
	
	private void sessionChangedEventHandler(Session session) {
	}
	
    private JMenuItem getMenuCompare() {
        if (menuCompare == null) {
        	menuCompare = new JMenuItem();
        	menuCompare.setText(Constant.messages.getString("cmp.file.menu.compare"));

        	menuCompare.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                	
                	compareSessions();
                }
            });
        }
        return menuCompare;
    }
    
	private void buildHistoryMap (TableHistory th, Map<String, String> map) 
    		throws SQLException, HttpMalformedHeaderException {

		// Get the first session id
    	RecordHistory rh = null;
    	for (int i=0; i < 100; i++ ) {
    		rh = th.read(i);
    		if (rh != null) {
    			break;
    		}
    	}
    	if (rh == null) {
    		return;
    	}
        @SuppressWarnings("unchecked")
    	Vector<Integer> hIds = th.getHistoryList(rh.getSessionId(), HistoryReference.TYPE_MANUAL);
    	for (Integer hId : hIds) {
    		RecordHistory recH = th.read(hId);
    		URI uri = recH.getHttpMessage().getRequestHeader().getURI();
    		String mapKey = recH.getHttpMessage().getRequestHeader().getMethod() +
    			" " + uri.toString();
    		
    		// TODO Optionally strip off params?
    		if (mapKey.indexOf("?") > -1) {
    			mapKey = mapKey.substring(0, mapKey.indexOf("?"));
    		}
    		
    		String val = map.get(mapKey);
    		String code = recH.getHttpMessage().getResponseHeader().getStatusCode() + " ";
    		if (val == null) {
    			map.put(mapKey, code);
    		} else if (val.indexOf(code) < 0){
    			map.put(mapKey, val + code);
    		}
    	}
    }
    
    private void compareSessions () {
		JFileChooser chooser = new JFileChooser(Model.getSingleton().getOptionsParam().getUserDirectory());
		File file = null;
	    chooser.setFileFilter(new FileFilter() {
	           public boolean accept(File file) {
	                if (file.isDirectory()) {
	                    return true;
	                } else if (file.isFile() && file.getName().endsWith(".session")) {
	                    return true;
	                }
	                return false;
	            }
	           public String getDescription() {
	               return Constant.messages.getString("file.format.zap.session");
	           }
	    });
	    int rc = chooser.showOpenDialog(View.getSingleton().getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
			try {
	    		file = chooser.getSelectedFile();
	    		if (file == null) {
	    			return;
	    		}
	    		Model cmpModel = new Model();
	    		Session session = cmpModel.getSession();

	    	    //log.info("opening session file " + file.getAbsolutePath());
	    	    //WaitMessageDialog waitMessageDialog = View.getSingleton().getWaitMessageDialog("Loading session file.  Please wait ...");
	    		session.open(file, this);

				Database db = new Database();
				db.open(file.getAbsolutePath());
				
				Map <String, String> curMap = new HashMap<String,String>();
				Map <String, String> cmpMap = new HashMap<String,String>();
				
				// Load the 2 sessions into 2 maps
				this.buildHistoryMap(Database.getSingleton().getTableHistory(), curMap);
				this.buildHistoryMap(db.getTableHistory(), cmpMap);

		    	File outputFile = this.getOutputFile();

		    	if (outputFile != null) {
		    		// Write the result to the specified file
		    		try {
				        TreeSet<String> sset = new TreeSet<String>();
				        // Combine the keys for both maps
				        sset.addAll(curMap.keySet());
				        sset.addAll(cmpMap.keySet());
				        
					    StringBuffer sb = new StringBuffer(500);
					    sb.append("<?xml version=\"1.0\"?>");
					    sb.append(CRLF);
					    sb.append("<report>");
					    sb.append(CRLF);
					    sb.append("<session-names>");
					    sb.append(CRLF);
					    sb.append("<session1>");
					    sb.append(Model.getSingleton().getSession().getSessionName());
					    sb.append("</session1>");
					    sb.append(CRLF);
					    sb.append("<session2>");
					    sb.append(session.getSessionName());
					    sb.append("</session2>");
					    sb.append(CRLF);
					    sb.append("</session-names>");
					    sb.append(CRLF);


				        Iterator<String> iter = sset.iterator();
				        while (iter.hasNext()) {
						    sb.append("<urlrow>");	
						    sb.append(CRLF);
				        	String key = iter.next();
				        	String method = key.substring(0, key.indexOf(" "));
				        	String url = key.substring(key.indexOf(" ") + 1);

						    sb.append("<method>");
						    sb.append(method);
						    sb.append("</method>");
						    sb.append(CRLF);
						    
						    sb.append("<url>");
						    sb.append(url);
						    sb.append("</url>");
						    sb.append(CRLF);
						    
						    sb.append("<code1>");
						    if (curMap.containsKey(key)) {
				            	sb.append(curMap.get(key));
				            } else {
				            	sb.append("---");
				            }
						    sb.append("</code1>");
						    sb.append(CRLF);

						    sb.append("<code2>");
				            if (cmpMap.containsKey(key)) {
				            	sb.append(cmpMap.get(key));
				            } else {
				            	sb.append("---");
				            }
						    sb.append("</code2>");
						    sb.append(CRLF);
	
						    sb.append("</urlrow>");	
						    sb.append(CRLF);
				        }

					    sb.append("</report>");	
					    sb.append(CRLF);
					    
				        ReportGenerator.stringToHtml(sb.toString(), 
					    		"xml" + File.separator + "reportCompare.xsl", 
					    		outputFile.getAbsolutePath());
				
				    } catch (Exception e1) {
			        	log.warn(e1.getMessage(), e1);
				    }
		    	}
		    	
	    		//waitMessageDialog.setVisible(true);
			} catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
	    }
    }

    private File getOutputFile() {

	    JFileChooser chooser = new JFileChooser(getModel().getOptionsParam().getUserDirectory());
	    chooser.setFileFilter(new FileFilter() {
	           public boolean accept(File file) {
	                if (file.isDirectory()) {
	                    return true;
	                } else if (file.isFile() && 
	                		file.getName().toLowerCase().endsWith(".htm")) {
	                    return true;
	                } else if (file.isFile() && 
	                		file.getName().toLowerCase().endsWith(".html")) {
	                    return true;
	                }
	                return false;
	            }
	           public String getDescription() {
	               return Constant.messages.getString("file.format.html");
	           }
	    });
	    
		File file = null;
	    int rc = chooser.showSaveDialog(getView().getMainFrame());
	    if(rc == JFileChooser.APPROVE_OPTION) {
    		file = chooser.getSelectedFile();
    		if (file == null) {
    			return file;
    		}
            getModel().getOptionsParam().setUserDirectory(chooser.getCurrentDirectory());
    		String fileName = file.getAbsolutePath().toLowerCase();
    		if (! fileName.endsWith(".htm") &&
    				! fileName.endsWith(".html")) {
    		    fileName += ".html";
    		    file = new File(fileName);
    		}
    		return file;
    		
	    }
	    return file;
    }

	@Override
	public void sessionOpened(File file, Exception e) {
		
	}

	@Override
	public void sessionSaved(Exception e) {
		
	}
}