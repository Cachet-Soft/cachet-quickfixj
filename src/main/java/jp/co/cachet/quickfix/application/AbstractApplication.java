package jp.co.cachet.quickfix.application;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import quickfix.DoNotSend;
import quickfix.FieldNotFound;
import quickfix.IncorrectDataFormat;
import quickfix.IncorrectTagValue;
import quickfix.Message;
import quickfix.MessageCracker;
import quickfix.RejectLogon;
import quickfix.SessionID;
import quickfix.UnsupportedMessageType;

public class AbstractApplication extends MessageCracker implements quickfix.Application {
	private static final Logger log = LoggerFactory.getLogger(AbstractApplication.class);

	public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {
	}

	public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
		// crack()実行で、各メッセージタイプに対応したonMessage()へ処理が委譲される。
        crack(message, sessionId);
	}

	public void onCreate(SessionID sessionId) {
	}

	public void onLogon(SessionID sessionId) {
	}

	public void onLogout(SessionID sessionId) {
	}

	public void toAdmin(Message message, SessionID sessionId) {
	}

	public void toApp(Message message, SessionID sessionId) throws DoNotSend {
	}
	
	public void onMessage(quickfix.fix42.BusinessMessageReject message, SessionID sessionId) {
		log.warn("onBusinessMessageReject {}", message);
	}

}
