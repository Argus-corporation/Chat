package net.argus.emessage.client;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import net.argus.emessage.Chat;
import net.argus.emessage.client.event.ChatEvent;
import net.argus.emessage.client.event.ChatListener;
import net.argus.emessage.client.event.EventChat;
import net.argus.emessage.client.gui.Connect;
import net.argus.emessage.client.gui.GUIClient;
import net.argus.emessage.pack.ChatPackagePrefab;
import net.argus.emessage.pack.ChatPackageType;
import net.argus.event.gui.frame.FrameEvent;
import net.argus.event.gui.frame.FrameListener;
import net.argus.event.net.socket.SocketEvent;
import net.argus.event.net.socket.SocketListener;
import net.argus.exception.InstanceException;
import net.argus.file.FileManager;
import net.argus.file.css.CSSEngine;
import net.argus.gui.OptionPane;
import net.argus.gui.TextField;
import net.argus.instance.CardinalProgram;
import net.argus.instance.Instance;
import net.argus.instance.Program;
import net.argus.lang.Lang;
import net.argus.lang.LangRegister;
import net.argus.net.client.Client;
import net.argus.net.pack.PackagePrefab;
import net.argus.plugin.InitializationPlugin;
import net.argus.plugin.PluginEvent;
import net.argus.plugin.PluginRegister;
import net.argus.system.InitializationSplash;
import net.argus.system.InitializationSystem;
import net.argus.util.ArrayManager;
import net.argus.util.Display;
import net.argus.util.FontManager;
import net.argus.util.ThreadManager;
import net.argus.util.debug.Debug;
import net.argus.util.Error;

@Program(instanceName = "client")
public class MainClient extends CardinalProgram {
	
	private static ChatClient client;
	
	private static EventChat event = new EventChat();
	
	private static Instance instanceClient;
	private static CardinalProgram program;
	
	public static void init() {
		ChatPackageType.init();
		ClientResources.init();
		GUIClient.init();

		GUIClient.addFastAction(getFastActionListener());
		GUIClient.addJoinAction(getJoinActionListener());
		GUIClient.addLeaveAction(getLeaveActionListener());
		
		GUIClient.addPreferenceAction(getPreferenceActionListener());
		
		GUIClient.addAboutAction(getAboutActionListener());
		
		GUIClient.addSendAction(getSendActionListener());
		
		GUIClient.addFrameListener(getFrameListener());
		
		addChatListener(getChatListener());
		
		PluginRegister.init(new PluginEvent(MainClient.class));
				
		InitializationSplash.getSplash().exit();
		while(!InitializationSplash.getSplash().isFinnish())
			GUIClient.setVisible(false);
			
		LangRegister.update();

		GUIClient.setVisible(true);

		PluginRegister.postInit(new PluginEvent(MainClient.class));
		
	}
	
	public static ActionListener getFastActionListener() {
		return (ActionEvent e) -> Instance.startThread(new Connect(true), instanceClient);
	}
	
	public static ActionListener getJoinActionListener() {
		return (ActionEvent e) -> Instance.startThread(new Connect(false), instanceClient);
	}
	
	public static ActionListener getLeaveActionListener() {
		return (ActionEvent e) -> {Instance.setThreadInstance(instanceClient); client.logOut();};
	}
	
	public static ActionListener getPreferenceActionListener() {
		return (ActionEvent e) -> {Instance.setThreadInstance(instanceClient); GUIClient.getConfigWindow().show();};
	}
	
	public static ActionListener getAboutActionListener() {
		return (ActionEvent e) -> {Instance.setThreadInstance(instanceClient); GUIClient.getAboutDialog().show();};
	}
	
	public static ActionListener getSendActionListener() {
		return new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				TextField field = GUIClient.panChat.getTextField();
				String msg = field.getText();
				
				if(client != null && client.isConnected() && ArrayManager.isExist(msg.toCharArray(), 0)) {
					boolean com = msg.toCharArray()[0] == '/';
					if(com)
						client.send(PackagePrefab.genCommandPackage(msg));
					else
						client.send(ChatPackagePrefab.genMessagePackage(msg));
					
					event.startEvent(EventChat.SEND_MESSAGE, new ChatEvent(msg, null));
					
					if(!com)
						GUIClient.addMessage(msg);
				
					field.copyData();
					field.setText("");
				}
			}
		};
	
	}
	
	public static FrameListener getFrameListener() {
		return new FrameListener() {
			public void frameResizing(FrameEvent e) {}

			@SuppressWarnings("deprecation")
			@Override
			public void frameClosing(FrameEvent e) {
				if(client != null && client.isConnected()) {
					client.send(PackagePrefab.genLogOutPackage("Frame Closing"));
					
					client.getClientProcess().close();
				}
				
				wakeUp();
				GUIClient.getFrame().setVisible(false);
				Thread.currentThread().stop();
			}	
			public void frameMinimalized(FrameEvent e) {}
		};
	}
	
	public static SocketListener getSocketListener() {
		return new SocketListener() {

			@Override
			public void connect(SocketEvent e) {
				event.startEvent(EventChat.CONNECT, new ChatEvent(e.getArgument(), null));
				
				GUIClient.connect();
			}

			@Override
			public void disconnect(SocketEvent e) {
				GUIClient.leave();
				
				event.startEvent(EventChat.DISCONNECT, new ChatEvent(e.getArgument(), null));
			}

			@Override
			public void connectionRefused(SocketEvent e) {
				GUIClient.leave();
				event.startEvent(EventChat.DISCONNECT, new ChatEvent(e.getArgument(), true));
			}
		};
	}
	
	public static ChatListener getChatListener() {
		return new ChatListener() {
			
			@Override
			public void sendMessage(ChatEvent e) {}
			
			@Override
			public void receiveMessage(ChatEvent e) {}
			
			@Override
			public void disconnect(ChatEvent e) {
				if(e.isError())
					GUIClient.addSystemMessage(("You could not connect") + (e.getMessage()!=null?": " + e.getMessage():""));
				else
					GUIClient.addSystemMessage(("You are disconnected") + (e.getMessage()!=null?": " + e.getMessage():""));
			}
			
			@Override
			public void connect(ChatEvent e) {
				GUIClient.clearMessage();
				GUIClient.addSystemMessage("You are connected");
			}
			
			@Override
			public void addMessage(ChatEvent e) {}
		};
	}
	
	public static Instance getClientInstance() {return instanceClient;}
	
	
	/**--EVENT--**/
	public static void addChatListener(ChatListener listener) {event.addListener(listener);}
	public static void removeChatListener(ChatListener listener) {event.removeListener(listener);}
	
	public static EventChat getEvent() {return event;}
	
	
	/**----**/
	public static void connect(String host, String pseudo, String password) {
		client = new ChatClient(host, ClientResources.config.getInt("port"), pseudo);
		
		client.addSocketListener(getSocketListener());
		client.addProcessListener(new ClientChatProcess());

		try {client.connect(password);}
		catch(IOException e) {}
	}
	
	public static void wakeUp() {notify(program);}
	
	public int main(String[] args) throws InstanceException {
		try {
			InitializationSystem.initSystem(args, true, new InitializationSplash("res/logo.png", Display.getWidth() - 50, 0));
			InitializationPlugin.register();
	
			Debug.log("Program version: " + Chat.VERSION);
			Debug.log("Client version: " + Client.VERSION);
	
			instanceClient = getInstance();
			program = this;
			
			Lang.setLang(ClientResources.config);
			
			FontManager.registerFont(FontManager.loadFont(new File(FileManager.getMainPath() + "/res/font/Roboto.ttf")));
			Lang.updateCSS();
			
			CSSEngine.run("client", "bin/css");
			
			Debug.addBlackList(ThreadManager.THREAD_MANAGER);
			
			PluginRegister.preInit(new PluginEvent(Chat.getInfo()));

			MainClient.init();
		}catch(Throwable e) {Error.createErrorFileLog(e); OptionPane.showErrorDialog(null, Chat.NAME, e); notify(this);};
		wait(this);
		return 0;
	}
	
}
