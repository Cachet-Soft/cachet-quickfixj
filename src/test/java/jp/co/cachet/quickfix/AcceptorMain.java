package jp.co.cachet.quickfix;

import java.io.*;

import jp.co.cachet.quickfix.application.AcceptorApplication;
import quickfix.*;

public class AcceptorMain {

	public static void main(String[] args) {
		try {
			InputStream inputStream = null;
			if (args.length == 0) {
				inputStream = AcceptorMain.class.getResourceAsStream("/acceptor.cfg");
			} else if (args.length == 1) {
				inputStream = new FileInputStream(args[0]);
			}
			if (inputStream == null) {
				System.out.println("usage: " + AcceptorMain.class.getName()	+ " [configFile].");
				return;
			}
			SessionSettings settings = new SessionSettings(inputStream);

			Application application = new AcceptorApplication();
			FileStoreFactory storeFactory = new FileStoreFactory(settings);
			LogFactory logFactory = new ScreenLogFactory(settings);
			SocketAcceptor acceptor = new SocketAcceptor(application, storeFactory, settings, logFactory, new DefaultMessageFactory());

			BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
			acceptor.start();
			while (true) {
				System.out.println("type #quit to quit");
				String value = in.readLine();
				if (value != null) {
					if (value.equals("#quit")) {
						break;
					}
				}
			}
			acceptor.stop();
			System.exit(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
