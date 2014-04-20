package jp.co.cachet.quickfix;

import java.io.*;
import java.util.Iterator;
import java.util.concurrent.CountDownLatch;

import javax.swing.JFrame;
import javax.swing.UIManager;

import jp.co.cachet.quickfix.application.InitiatorApplication;
import jp.co.cachet.quickfix.ui.InitiatorFrame;
import jp.co.cachet.quickfix.ui.model.*;

import org.quickfixj.jmx.JmxExporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.*;

public class InitiatorMain {

    private static final Logger log = LoggerFactory.getLogger(InitiatorMain.class);
    private static final CountDownLatch shutdownLatch = new CountDownLatch(1);
    private static InitiatorMain client;

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            client = new InitiatorMain(args);
            client.start();
            shutdownLatch.await();
        } catch (Exception e) {
            log.info(e.getMessage(), e);
        }
	}

    private boolean initiatorStarted = false;
    private Initiator initiator = null;
    private JFrame frame = null;
    
    public InitiatorMain(String[] args) throws Exception {
        InputStream inputStream = null;
        if (args.length == 0) {
            inputStream = InitiatorMain.class.getResourceAsStream("/initiator.cfg");
        } else if (args.length == 1) {
            inputStream = new FileInputStream(args[0]);
        }
        if (inputStream == null) {
            System.out.println("usage: " + InitiatorMain.class.getName() + " [configFile].");
            return;
        }
        SessionSettings settings = new SessionSettings(inputStream);
        inputStream.close();
        
        boolean logHeartbeats = Boolean.valueOf(System.getProperty("logHeartbeats", "true")).booleanValue();
        
        OrderTableModel orderTableModel = new OrderTableModel();
        FillTableModel fillTableModel = new FillTableModel();
        PriceTableModel priceTableModel = new PriceTableModel();
        InitiatorApplication application = new InitiatorApplication();
        MessageStoreFactory messageStoreFactory = new FileStoreFactory(settings);
        LogFactory logFactory = new ScreenLogFactory(true, true, true, logHeartbeats);
        MessageFactory messageFactory = new DefaultMessageFactory();

        initiator = new SocketInitiator(application, messageStoreFactory, settings, logFactory, messageFactory);
       
        JmxExporter exporter = new JmxExporter();
        exporter.register(initiator);
        
        frame = new InitiatorFrame(orderTableModel, fillTableModel, priceTableModel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public void start() {
        if (!initiatorStarted) {
            try {
                initiator.start();
                initiatorStarted = true;
            } catch (Exception e) {
                log.error("Logon failed", e);
            }
        } else {
            Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
            while (sessionIds.hasNext()) {
                SessionID sessionId = (SessionID) sessionIds.next();
                Session.lookupSession(sessionId).logon();
            }
        }
    }

    public void stop() {
        Iterator<SessionID> sessionIds = initiator.getSessions().iterator();
        while (sessionIds.hasNext()) {
            SessionID sessionId = (SessionID) sessionIds.next();
            Session.lookupSession(sessionId).logout("user requested");
        }
        shutdownLatch.countDown();
    }

}
