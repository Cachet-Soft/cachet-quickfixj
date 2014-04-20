package jp.co.cachet.quickfix.ui.view;

import java.awt.*;

import javax.swing.*;

import jp.co.cachet.quickfix.ui.model.*;

@SuppressWarnings("serial")
public class InitiatorPanel extends JPanel {
	private OrderEntryPanel orderEntryPanel;
	private OrderPanel orderPanel;
	private FillPanel fillPanel;
	private PricePanel pricePanel;
	
	public InitiatorPanel(OrderTableModel orderTableModel, FillTableModel fillTableModel, PriceTableModel priceTableModel) {
        setName("BanzaiPanel");

        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.fill = GridBagConstraints.BOTH;
        constraints.weightx = 1;
        constraints.insets = new Insets(1, 5, 1, 5);

        orderEntryPanel = new OrderEntryPanel(orderTableModel);
        orderPanel = new OrderPanel(orderTableModel);
        fillPanel = new FillPanel(fillTableModel);
        pricePanel = new PricePanel(priceTableModel);

        JSplitPane mainPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JSplitPane topPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        JSplitPane bottomPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        
        mainPane.setTopComponent(topPane);
        mainPane.setBottomComponent(bottomPane);
        
        topPane.setLeftComponent(orderEntryPanel);
        topPane.setRightComponent(orderPanel);
        
        bottomPane.setLeftComponent(fillPanel);
        bottomPane.setRightComponent(pricePanel);
        
        add(mainPane, constraints);

	}

}
