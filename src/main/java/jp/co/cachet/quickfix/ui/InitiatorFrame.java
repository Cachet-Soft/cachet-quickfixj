package jp.co.cachet.quickfix.ui;

import java.awt.BorderLayout;

import javax.swing.JFrame;

import jp.co.cachet.quickfix.ui.model.*;
import jp.co.cachet.quickfix.ui.view.InitiatorPanel;

@SuppressWarnings("serial")
public class InitiatorFrame extends JFrame {
	public InitiatorFrame(OrderTableModel orderTableModel, FillTableModel fillTableModel, PriceTableModel priceTableModel) {
        super();
        setTitle("Initiator Client");
        setSize(1024, 768);

        getContentPane().add(new InitiatorPanel(orderTableModel, fillTableModel, priceTableModel), BorderLayout.CENTER);
        setVisible(true);

	}
}
