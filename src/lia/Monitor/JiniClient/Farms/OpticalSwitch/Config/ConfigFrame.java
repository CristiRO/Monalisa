package lia.Monitor.JiniClient.Farms.OpticalSwitch.Config;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.border.TitledBorder;

import lia.Monitor.JiniClient.Farms.OpticalSwitch.OpticalSwitchGraphPan;
import lia.Monitor.JiniClient.Farms.OpticalSwitch.OpticalSwitchPan;
import lia.Monitor.tcpClient.tClient;
import lia.net.topology.opticalswitch.OSPort;
import lia.net.topology.opticalswitch.OpticalSwitch;

/**
 * The optical switch configuration panel...
 */
public class ConfigFrame extends JFrame implements ActionListener, ItemListener, WindowListener, MouseListener,
        ComponentListener {

    /**
     * 
     */
    private static final long serialVersionUID = -6191588139523107065L;

    /** Logger used by this class */
    private static final Logger logger = Logger.getLogger("lia.Monitor.JiniClient.Farms.OpticalSwitch.Config");

    private PortsPanel portsPanel = null;
    private LegendDialog legendDialog;
    private final ReducedPortTable portTable;
    private final ConnectionTable connTable;

    private final JCheckBox fdxCheck;

    private final JButton legendButton;
    private final JButton connectButton;
    private final JButton disconnectButton;

    private final JButton gmplsButton;
    //	public GMPLSAdmin gmplsAdmin;

    private final JButton destroyLinkSimulateButton;

    private JLabel deviceName;
    private JLabel inPortLabel;
    private JLabel outPortLabel;

    public static final Font borderFont = new Font("Arial", Font.PLAIN, 10);
    public static final Font deviceFont = new Font("Arial", Font.PLAIN, 12);

    private final Hashtable conns;
    private final Hashtable connState;
    private final Hashtable connLoss;
    private final Hashtable connSwTime;
    private final Hashtable connDuration;

    private final Vector portAdminListeners;
    private boolean fdx = true;

    protected Hashtable linkSimulationStates;
    protected OSPort currentClickedPort;

    OpticalSwitchMonitorControl control;
    public MonaLisaPanel p;

    public ConfigFrame(String OSName, OpticalSwitchMonitorControl control) {

        super(OSName);

        this.control = control;

        portAdminListeners = new Vector();
        linkSimulationStates = new Hashtable();

        conns = new Hashtable();
        connState = new Hashtable();
        connLoss = new Hashtable();
        connSwTime = new Hashtable();
        connDuration = new Hashtable();

        getContentPane().addMouseListener(this);
        getContentPane().addComponentListener(this);

        p = new MonaLisaPanel();
        p.addMouseListener(this);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel l = new JLabel(OSName);
        l.setOpaque(false);
        l.setFont(PortsPanel.titleFont.deriveFont(Font.BOLD, 18));
        JPanel p1 = new JPanel();
        p1.setOpaque(false);
        p1.addMouseListener(this);
        p1.setLayout(new FlowLayout());
        p1.add(l);
        p.add(p1);

        portsPanel = new PortsPanel(this);
        portsPanel.setOpaque(false);
        JPanel p2 = new JPanel();
        p2.setOpaque(false);
        p2.setLayout(new BorderLayout());
        p2.add(portsPanel, BorderLayout.CENTER);
        p.add(p2);

        JPanel southPanel = new JPanel();
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.Y_AXIS));
        southPanel.setOpaque(false);

        //		JPanel devicePanel = new JPanel();
        //		devicePanel.setOpaque(false);
        //		devicePanel.setBorder(BorderFactory.createTitledBorder("Device parameters"));
        //		((TitledBorder)devicePanel.getBorder()).setTitleFont(borderFont);
        //		devicePanel.setLayout(new GridLayout(0, 1));
        //		
        //		deviceName = new JLabel();
        //		deviceName.setOpaque(false);
        //		deviceName.setFont(deviceFont);
        //		l = new JLabel("Device name: ");
        //		l.setFont(deviceFont);
        //		l.setOpaque(false);
        //		JPanel p3 = new JPanel();
        //		p3.setOpaque(false);
        //		p3.setLayout(new BoxLayout(p3, BoxLayout.X_AXIS));
        //		p3.add(l);
        //		p3.add(deviceName);
        //		p3.add(Box.createHorizontalStrut(4));
        //		devicePanel.add(p3);
        //		
        //		inPortLabel = new JLabel();
        //		inPortLabel.setFont(deviceFont);
        //		inPortLabel.setOpaque(false);
        //		l = new JLabel("IN Port Labels : ");
        //		l.setFont(deviceFont);
        //		l.setOpaque(false);
        //		JPanel p4 = new JPanel();
        //		p4.setOpaque(false);
        //		p4.setLayout(new BoxLayout(p4, BoxLayout.X_AXIS));
        //		p4.add(l);
        //		p4.add(Box.createHorizontalStrut(4));
        //		p4.add(inPortLabel);
        //		devicePanel.add(p4);
        //		
        //		outPortLabel = new JLabel();
        //		outPortLabel.setFont(deviceFont);
        //		outPortLabel.setOpaque(false);
        //		l = new JLabel("OUT Port Labels: ");
        //		l.setFont(deviceFont);
        //		l.setOpaque(false);
        //		JPanel p5 = new JPanel();
        //		p5.setOpaque(false);
        //		p5.setLayout(new BoxLayout(p5, BoxLayout.X_AXIS));
        //		p5.add(l);
        //		p5.add(Box.createHorizontalStrut(4));
        //		p5.add(outPortLabel);
        //		devicePanel.add(p5);

        //		southPanel.add(devicePanel);
        //		southPanel.add(Box.createVerticalStrut(4));

        JPanel portPanel = new JPanel();
        portPanel.setOpaque(false);
        portPanel.setBorder(BorderFactory.createTitledBorder("Port parameters"));
        ((TitledBorder) portPanel.getBorder()).setTitleFont(borderFont);
        portPanel.setLayout(new GridLayout(0, 1));

        portTable = new ReducedPortTable(this);
        portTable.setOpaque(false);
        portPanel.add(portTable);
        portTable.addMouseListener(this);

        southPanel.add(portPanel);
        southPanel.add(Box.createVerticalStrut(4));

        connTable = new ConnectionTable(this);
        connTable.addMouseListener(this);

        JPanel buttonPanel = new JPanel();
        buttonPanel.addMouseListener(this);
        buttonPanel.setOpaque(false);
        buttonPanel.setLayout(new FlowLayout());
        legendButton = new JButton("Legend");
        legendButton.addActionListener(this);
        connectButton = new JButton("Connect");
        connectButton.addActionListener(this);
        connectButton.setEnabled(false);
        disconnectButton = new JButton("Disconnect");
        disconnectButton.addActionListener(this);
        disconnectButton.setEnabled(false);
        fdxCheck = new JCheckBox("FDX");
        fdxCheck.setSelected(true);
        fdxCheck.setOpaque(false);
        fdxCheck.addItemListener(this);
        buttonPanel.add(fdxCheck);
        buttonPanel.add(legendButton);
        buttonPanel.add(connectButton);
        buttonPanel.add(disconnectButton);
        gmplsButton = new JButton("GMPLS");
        gmplsButton.addActionListener(this);
        gmplsButton.setVisible(false);
        buttonPanel.add(gmplsButton);
        destroyLinkSimulateButton = new JButton("");
        destroyLinkSimulateButton.addActionListener(this);
        destroyLinkSimulateButton.setEnabled(false);
        destroyLinkSimulateButton.setVisible(false);
        buttonPanel.add(destroyLinkSimulateButton);
        setCurrentClickedPort(null);

        southPanel.add(buttonPanel);
        p.add(southPanel);

        p.add(Box.createVerticalGlue());

        getContentPane().setLayout(new BorderLayout());

        p.addMouseListener(this);
        getContentPane().add(p);

        //		JScrollPane scrollP = new JScrollPane(p);
        //		
        //		scrollP.addMouseListener(this);
        //		scrollP.getViewport().addMouseListener(this);
        //		
        //		scrollP.setOpaque(false);
        //		getContentPane().add(scrollP);
        setSize(600, 500);
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        addWindowListener(this);
    }

    public String getToolTipText(MouseEvent e) {
        return "Cng";
    }

    protected void setCurrentClickedPort(OSPort port) {

        if (port == null) {
            currentClickedPort = null;
            destroyLinkSimulateButton.setText("No selected port");
            destroyLinkSimulateButton.setToolTipText("No selected port");
            destroyLinkSimulateButton.setEnabled(false);
            return;
        }

        final OpticalSwitchGraphPan in = OpticalSwitchGraphPan.getInstance();
        if ((in != null) && in.checkPortConnectsFake(getTitle(), port.name())) {
            currentClickedPort = port;
            linkSimulationStates.put(port, Boolean.TRUE);
            destroyLinkSimulateButton.setText("Selected port can not be used");
            destroyLinkSimulateButton.setToolTipText("No selected port");
            destroyLinkSimulateButton.setEnabled(false);
            return;
        }

        if (!linkSimulationStates.containsKey(port)) {
            currentClickedPort = port;
            linkSimulationStates.put(port, Boolean.TRUE);
            destroyLinkSimulateButton.setText("Simulate");
            destroyLinkSimulateButton.setToolTipText("Simulate fiber failure for " + port);
            destroyLinkSimulateButton.setEnabled(true);
            return;
        }
        currentClickedPort = port;
        boolean state = ((Boolean) linkSimulationStates.get(port)).booleanValue();
        if (state) {
            destroyLinkSimulateButton.setText("Simulate");
            destroyLinkSimulateButton.setToolTipText("Simulate fiber failure for " + port);
            destroyLinkSimulateButton.setEnabled(true);
        } else {
            destroyLinkSimulateButton.setText("End simulation");
            destroyLinkSimulateButton.setToolTipText("End simulation of fiber failure for " + port);
            destroyLinkSimulateButton.setEnabled(true);
        }
    }

    protected void setSimulateState() {
        destroyLinkSimulateButton.setVisible(!destroyLinkSimulateButton.isVisible());
    }

    public void addPortAdminListener(PortAdminListener listener) {

        synchronized (getTreeLock()) {
            if (listener == null) {
                return;
            }
            portAdminListeners.add(listener);
        }
    }

    public void removePortAdminListener(PortAdminListener listener) {

        synchronized (getTreeLock()) {
            if (listener == null) {
                return;
            }
            portAdminListeners.remove(listener);
        }
    }

    public void changePortState(String portName, String newSignalType) {

        synchronized (getTreeLock()) {
            for (int i = 0; i < portAdminListeners.size(); i++) {
                ((PortAdminListener) portAdminListeners.get(i)).changePortState(portName, newSignalType);
            }
        }
    }

    public void disconnectPorts(String inputPort, String outputPort, boolean fullDuplex) {

        synchronized (getTreeLock()) {
            for (int i = 0; i < portAdminListeners.size(); i++) {
                ((PortAdminListener) portAdminListeners.get(i)).disconnectPorts(inputPort, outputPort, fullDuplex);
            }
        }
    }

    public void connectPorts(String inputPort, String outputPort, boolean fullDuplex) {

        synchronized (getTreeLock()) {
            for (int i = 0; i < portAdminListeners.size(); i++) {
                ((PortAdminListener) portAdminListeners.get(i)).connectPorts(inputPort, outputPort, fullDuplex);
            }
        }
    }

    public void setConnectButtonState(boolean enabled) {
        connectButton.setEnabled(enabled);
    }

    public void setDisconnectButtonState(boolean enabled) {
        disconnectButton.setEnabled(enabled);
    }

    public void setDeviceParams(String deviceName, String inPortLabel, String outPortLabel) {
        if ((deviceName != null) && (this.deviceName != null)) {
            this.deviceName.setText(deviceName);
        }
        if ((inPortLabel != null) && (this.inPortLabel != null)) {
            this.inPortLabel.setText(inPortLabel);
        }
        if ((outPortLabel != null) && (this.outPortLabel != null)) {
            this.outPortLabel.setText(outPortLabel);
        }
    }

    public Hashtable getCurrentConns() {

        return conns;
    }

    private void removeConn(String portName) {

        conns.remove(portName);
        connState.remove(portName);
        connLoss.remove(portName);
        connSwTime.remove(portName);
        connDuration.remove(portName);
    }

    private void newConn(String port1, String port2) {

        conns.put(port1, port2);
        conns.put(port2, port1);
    }

    private void addConn(String port1, String port2, String state, String loss, String swTime, String duration) {

        connState.put(port1, state);
        connState.put(port2, state);
        connLoss.put(port1, loss);
        connLoss.put(port2, loss);
        connSwTime.put(port1, swTime);
        connSwTime.put(port2, swTime);
        connDuration.put(port1, duration);
        connDuration.put(port2, duration);
    }

    // as a rule -> call setportstate prior to calling this method
    public void setConnState(String port1, String port2, String state, String insertionLoss, String swTime,
            String duration) {
        synchronized (getTreeLock()) {
            if (!conns.containsKey("in_" + port1) || !conns.containsKey("out_" + port2)) {
                logger.warning("Trying to set parameters for undeclared connection " + port1 + " -> " + port2);
                return;
            }
            addConn("in_" + port1, "out_" + port2, state, insertionLoss, swTime, duration);
        }
    }

    public void setCurrentInputPort(OSPort port) {
        portTable.setInput(port);
    }

    public void setCurrentOutputPort(OSPort port) {
        portTable.setOutput(port);
    }

    public void setCurrentConnPorts(OSPort port1, OSPort port2) {

        synchronized (getTreeLock()) {
            if ((port1 == null) || (port2 == null)) {
                connTable.setConnection(null, null, null, null);
                return;
            }

            if (!conns.containsKey("in_" + port1) || !conns.containsKey("out_" + port2)) {
                connTable.setConnection(null, null, null, null);
                return;
            }

            String p = "in_" + port1;
            String state = null;
            if (connState.containsKey(p)) {
                state = (String) connState.get(p);
            }
            String loss = null;
            if (connLoss.containsKey(p)) {
                loss = (String) connLoss.get(p);
            }
            String swTime = null;
            if (connSwTime.containsKey(p)) {
                swTime = (String) connSwTime.get(p);
            }
            String duration = null;
            if (connDuration.containsKey(p)) {
                duration = (String) connDuration.get(p);
            }
            connTable.setConnection(state, loss, swTime, duration);
        }
    }

    public void update(OpticalSwitch info) {
        portsPanel.update(info);
        portsPanel.repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        Object source = e.getSource();

        if (source.equals(legendButton)) {
            if (legendDialog == null) {
                legendDialog = new LegendDialog(this);
            }
            legendDialog.setLocation(this.getX() + ((this.getWidth() - legendDialog.getWidth()) / 2), this.getY()
                    + ((this.getHeight() - legendDialog.getHeight()) / 2));
            legendDialog.setVisible(true);
            return;
        }

        if (source.equals(connectButton)) {
            if ((portsPanel.portInClicked == null) || (portsPanel.portOutClicked == null)) {
                return;
            }
            String inputPort = portsPanel.portInClicked.name();
            String outputPort = portsPanel.portOutClicked.name();
            logger.info("Sending connect for device " + getTitle() + " between ports " + inputPort + " and "
                    + outputPort);
            connectPorts(inputPort, outputPort, fdx);
            return;
        }

        if (source.equals(disconnectButton)) {
            if ((portsPanel.portInClicked == null) || (portsPanel.portOutClicked == null)) {
                return;
            }
            String inputPort = portsPanel.portInClicked.name();
            String outputPort = portsPanel.portOutClicked.name();
            logger.info("Sending disconnect for device " + getTitle() + " between ports " + inputPort + " and "
                    + outputPort);
            disconnectPorts(inputPort, outputPort, fdx);
            return;
        }

        if (source.equals(destroyLinkSimulateButton)) {
            if ((currentClickedPort == null) || (control == null)) {
                return;
            }
            boolean state = ((Boolean) linkSimulationStates.get(currentClickedPort)).booleanValue();
            if (state) {
                try {
                    String ret = control.remoteCtrl.disconnectPorts(currentClickedPort.name(), null, "SIMULATE", fdx);
                    logger.info("Simulate fiber failure for " + currentClickedPort + " returned " + ret);
                    if (ret.equals("OK")) {
                        linkSimulationStates.put(currentClickedPort, Boolean.FALSE);
                        setCurrentClickedPort(currentClickedPort);
                    }
                } catch (Exception ex) {
                    logger.info("Simulate fiber failure for " + currentClickedPort + " failed with "
                            + ex.getLocalizedMessage());
                }
            } else {
                try {
                    String ret = control.remoteCtrl.connectPorts(currentClickedPort.name(), null, "SIMULATE", fdx);
                    logger.info("End simulation of fiber failure for " + currentClickedPort + " returned " + ret);
                    if (ret.equals("OK")) {
                        linkSimulationStates.put(currentClickedPort, Boolean.TRUE);
                        setCurrentClickedPort(currentClickedPort);
                    }
                } catch (Exception ex) {
                    logger.info("End simulation of fiber failure for " + currentClickedPort + " failed with "
                            + ex.getLocalizedMessage());
                }
            }
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {

        Object source = e.getItemSelectable();
        if (source == fdxCheck) {
            fdx = !fdx;
        }
    }

    @Override
    public void windowOpened(WindowEvent e) {
    }

    @Override
    public void windowClosing(WindowEvent e) {

        if ((control == null) || (control.provider == null) || (((tClient) control.provider).trcframe == null)
                || (((tClient) control.provider).trcframe.serMonitorBase == null)
                || (((tClient) control.provider).trcframe.serMonitorBase.main == null)) {
            return;
        }
        OpticalSwitchPan panel = (OpticalSwitchPan) ((tClient) control.provider).trcframe.serMonitorBase.main
                .getGraphical("OpticalSwitchMap");
        if (panel == null) {
            return;
        }
        panel.enterCmd();
    }

    @Override
    public void windowClosed(WindowEvent e) {
    }

    @Override
    public void windowIconified(WindowEvent e) {
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
    }

    @Override
    public void windowActivated(WindowEvent e) {
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent e) {

        final Point p1 = e.getPoint();
        final Point p2 = e.getComponent().getLocationOnScreen();
        p1.setLocation(p1.getX() + p2.getX(), p1.getY() + p2.getY());
        Rectangle eyeRect = p.getEyeRect();
        if ((eyeRect != null) && eyeRect.contains(p1)) {
            if (e.isControlDown()) {
                setSimulateState();
            }
            e.consume();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }

    @Override
    public void componentHidden(ComponentEvent e) {
        if (p != null) {
            p.requestFocus();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {
        if (p != null) {
            p.requestFocus();
        }
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (p != null) {
            p.requestFocus();
        }
    }

    @Override
    public void componentShown(ComponentEvent e) {
        if (p != null) {
            p.requestFocus();
        }
    }

} // end of class ConfigFrame

