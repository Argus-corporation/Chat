package net.argus.emessage.pack;

import net.argus.gui.bubble.BubblePanel;
import net.argus.net.pack.Package;
import net.argus.net.pack.PackageBuilder;

public class ChatPackagePrefab {
	
	public static final int LEFT = BubblePanel.LEFT;
	public static final int RIGHT = BubblePanel.RIGHT;
	
	public static Package genMessagePackage(String message) {
		PackageBuilder builder = new PackageBuilder(ChatPackageType.MESSAGE);
		
		builder.addKey("Message", message);
		
		return builder.genPackage();
	}
	
	public static Package genServerMessagePackage(String message, String pseudo) {
		return genServerMessagePackage(message, pseudo, LEFT);
	}
	
	public static Package genServerMessagePackage(String message, String pseudo, int pos) {
		PackageBuilder builder = new PackageBuilder(ChatPackageType.MESSAGE);
		
		builder.addKey("Message", message);
		builder.addKey("Pseudo", pseudo);
		builder.addKey("Position", Integer.toString(pos));
		
		return builder.genPackage();
	}

}
