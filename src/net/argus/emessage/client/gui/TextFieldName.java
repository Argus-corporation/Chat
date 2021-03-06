package net.argus.emessage.client.gui;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import net.argus.event.change.ChangeListener;
import net.argus.gui.TextField;

public class TextFieldName extends TextField {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5283450673655876616L;
	
	public TextFieldName(int size, String name) {
		super(size, name);
		addFocusListener(getDefaultFocusListener());
		addKeyListener(getKeyListener());
		addChangeListener(getDefaultChangeListener());
	}
	
	public TextFieldName(int size) {
		this(size, null);
	}
	
	@Override
	public boolean isError() {
		getDefaultFocusListener().focusLost(null);
		return super.isError();
	}
	
	public ChangeListener getDefaultChangeListener() {
		return (n) -> getDefaultFocusListener().focusLost(null);
	}
	
	public FocusListener getDefaultFocusListener() {
		return new FocusListener() {
			public void focusLost(FocusEvent e) {
				String text = getText();
				if(text != null && text.length() == 0)
					setError();
				else
					unError();
			}
			public void focusGained(FocusEvent e) {}
		};
	}
	
	private KeyListener getKeyListener() {
		return new KeyListener() {
			public void keyTyped(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {getDefaultFocusListener().focusLost(null);}
			public void keyPressed(KeyEvent e) {}
		};
	}

}
