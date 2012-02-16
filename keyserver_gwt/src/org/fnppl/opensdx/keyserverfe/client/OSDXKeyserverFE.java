package org.fnppl.opensdx.keyserverfe.client;

import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

import org.fnppl.opensdx.keyserverfe.shared.FieldVerifier;
import org.fnppl.opensdx.keyserverfe.shared.KeyConnection;
import org.fnppl.opensdx.keyserverfe.shared.KeyInfo;
import org.fnppl.opensdx.keyserverfe.shared.NodeState;
import org.fnppl.opensdx.keyserverfe.shared.User;

import com.google.gwt.canvas.client.Canvas;
import com.google.gwt.canvas.dom.client.Context2d;
import com.google.gwt.canvas.dom.client.CssColor;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;


public class OSDXKeyserverFE implements EntryPoint {

	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	private final KeyserverServiceAsync keyserverService = GWT.create(KeyserverService.class);
	
	private static int UI_LOGIN = 0;  
	private static int UI_USER = 1; 
	
	private int uiState = UI_LOGIN;
	
	private User user = null;
	private VerticalPanel mainPanel = null;
	private FlexTable tableKeys = null;
	private Canvas graph = null;
	private Context2d graphContext = null;
	private Canvas graphOverlay = null;
	private Context2d graphOverlayContext = null;
	private int visibilityLevel = 1;
	private KeyDetailsPopupPanel keyDetails = new KeyDetailsPopupPanel();
	private Label userErrorLabel = null; 
	
	private int mouseX = 0;
	private int mouseY = 0;
//	private int mouseDownX = 0;
//	private int mouseDownY = 0;
//	private int mouseUpX = 0;
//	private int mouseUpY = 0;
	private boolean mouseDown = false;
	private boolean shiftDown = false;
	private KeyInfo selectedNode = null;
	private Vector<int[]> selectedFrom = new Vector<int[]>();
	private Vector<int[]> selectedTo = new Vector<int[]>();
	
	private Vector<CheckBox> checks = null;
	private ClickHandler checkBoxHandler = null;
	private CheckBox cbShowDetails = null;
	
	private HashMap<String, KeyInfo> keyid_key = new HashMap<String, KeyInfo>();
	private HashMap<String, Vector<KeyInfo>> keyid_connect_in = new HashMap<String, Vector<KeyInfo>>();
	private HashMap<String, Vector<KeyInfo>> keyid_connect_out = new HashMap<String, Vector<KeyInfo>>();
	private HashMap<String, Integer> connect_count = new HashMap<String, Integer>();
	private DateTimeFormat datetimeformat = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss zzz");
	
	private int w = 850;
	private int h = 600;
	private double fullCircle = Math.toRadians(360);
    private double phi = Math.toRadians(15);
    private double deg180 = Math.toRadians(180);
    
    
    private int radiusNode = 16;
    private int radiusCircle = 12;
    private int barb = 20;
    
    private static CssColor selectColor = CssColor.make(250,0,0);
    private static CssColor selectColor2 = CssColor.make(225,111, 27);
    
    private static CssColor validColor = CssColor.make(28, 185, 13);
    private static CssColor notValidColor = CssColor.make(250,0,0);
    
	private static CssColor[] nodeColors = new CssColor[] {CssColor.make(0,0,0),CssColor.make(44,95,200), CssColor.make(195,179,2), CssColor.make(173, 114, 198)};
	private static CssColor[] nodeTextColor = new CssColor[] {CssColor.make(255,255,255),CssColor.make(200,200,200),CssColor.make(0,0,0),CssColor.make(0,0,0)};
	private static CssColor[] edgeColors = new CssColor[7];
	static {
		edgeColors[KeyConnection.TYPE_UNKNOWN] = CssColor.make(0, 0,0);
		edgeColors[KeyConnection.TYPE_APPROVAL] = CssColor.make(28, 135, 13);
		edgeColors[KeyConnection.TYPE_APPROVAL_PENDING] = CssColor.make(94, 241, 75);
		edgeColors[KeyConnection.TYPE_DISAPPROVAL] = CssColor.make(200, 9, 104);
		edgeColors[KeyConnection.TYPE_REVOCATION] = CssColor.make(116, 27, 71);
		edgeColors[KeyConnection.TYPE_SUBKEY] = CssColor.make(54, 141, 142);
		edgeColors[KeyConnection.TYPE_REVOKEKEY] = CssColor.make(173, 114, 198);
	}
	
	public void onModuleLoad() {
		mainPanel = new VerticalPanel();
		RootPanel.get("mainPanel").add(mainPanel);
		
		updateUI(UI_LOGIN);
	}
	
	private ClickHandler buKeyLogHandler = null;
	
	public void updateUI(int newState) {
		if (newState == UI_LOGIN) {
			initLoginView();
		}
		else if (newState == UI_USER) {
			if (uiState != UI_USER) {
				initUserView();
				updateUserView();
				uiState = UI_USER;
			} else {
				updateUserView();
			}
		}
	}

	private void updateUserView() {
		updateKeyTable();
		layoutNodes();
		drawGraph();
	}

	private void updateKeyTable() {
		//update table
		Vector<KeyInfo> keys = user.getKeys();
		checks = new Vector<CheckBox>();
		tableKeys.removeAllRows();
			
		tableKeys.setHTML(0, 0, "no");
		tableKeys.setHTML(0, 1, "keyid short");
		tableKeys.setHTML(0, 2, "owner");
		tableKeys.setHTML(0, 3, "mnemonic");
		tableKeys.setHTML(0, 4, "level");
		tableKeys.setHTML(0, 5, "usage");
		tableKeys.setHTML(0, 6, "status");
		tableKeys.setHTML(0, 7, "selected");
		tableKeys.setHTML(0, 8, "logs");
		
		
		for (int i=0;i<keys.size();i++) {
			try { 
				KeyInfo ki = keys.get(i);
				CheckBox cb = new CheckBox();
				cb.addClickHandler(checkBoxHandler);
				cb.setStylePrimaryName("myTableChecks");
				checks.add(cb);
				int row = i+1;
				ki.setNo(row);
				
				tableKeys.setHTML(row, 0, ""+row);
				tableKeys.setHTML(row, 1, ki.getIdShort());
				tableKeys.setHTML(row, 2, ki.getOwner());
				tableKeys.setHTML(row, 3, ki.getMnemonic());
				tableKeys.setHTML(row, 4, ki.getLevel());
				tableKeys.setHTML(row, 5, ki.getUsage());
				tableKeys.setHTML(row, 6, ki.getStatusText());
				tableKeys.setWidget(row, 7, cb);
				
				Button kl = new Button("logs");
				kl.setTitle(ki.getId());
				kl.addClickHandler(buKeyLogHandler);
				kl.setStylePrimaryName("keylogButton");
				tableKeys.setWidget(row, 8, kl);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		
		//style
		tableKeys.getRowFormatter().setStylePrimaryName(0, "myTableHeader");
		
	}

	
	private void initUserView() {
		mainPanel.clear();
		
		initKeyLogHandler();
		initCheckBoxHandler();
		
		//init key table
		tableKeys = new FlexTable();
		tableKeys.setTitle("Keys");
		
		ScrollPanel scrollTable = new ScrollPanel(wrapInDiv("keytablePanel", tableKeys));
		scrollTable.addStyleName("scrollPanel");
		scrollTable.setWidth(w+"px");
		scrollTable.setHeight("300px");
		//mainPanel.add(wrapInDiv("keytablePanel", scrollTable));
		mainPanel.add(scrollTable);
		
		//init error label
		userErrorLabel = new Label();
		userErrorLabel.addStyleName("errorLabel");
		mainPanel.add(userErrorLabel);
		
		
		initAddKeyPanel();
		initSelectionPanel();
		initButtonsPanel();
		
		buildCanvas();
		layoutNodes();
		drawGraph();
	}

	private void initButtonsPanel() {
		//panel Buttons
		HorizontalPanel panelButtons = new HorizontalPanel();
		
		
		cbShowDetails = new CheckBox("show key details");
		cbShowDetails.setValue(true);
		panelButtons.add(cbShowDetails);
		
		final Button selectAll = new Button("select all");
		selectAll.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (checks==null) return;
				for (CheckBox cb : checks) {
					cb.setValue(true);
				}
				graphOverlayContext.clearRect(0,0,w,h);
				drawSelected();
			}
		});
		panelButtons.add(selectAll);
		
		final Button selectNone = new Button("select none");
		selectNone.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				if (checks==null) return;
				for (CheckBox cb : checks) {
					cb.setValue(false);
				}
				graphOverlayContext.clearRect(0,0,w,h);
				drawSelected();
			}
		});
		panelButtons.add(selectNone);
		mainPanel.add(wrapInDiv("buttonsPanel",panelButtons));
	}

	private void initSelectionPanel() {
		//panel Selection
		HorizontalPanel panelSelection = new HorizontalPanel();
		
		final Button buConnectIn = new Button("add incoming keylogs");
		buConnectIn.setTitle("Requests alll keylogs and related keys that are incoming to the selected keys.");
		buConnectIn.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addKeysAndLogsForSelection(true,false,userErrorLabel);
			}
		});
		
		final Button buConnectOut = new Button("add outgoing keylogs");
		buConnectOut.setTitle("Requests all keylogs and related keys that are outgoinig from the selected keys.");
		buConnectOut.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addKeysAndLogsForSelection(false,true,userErrorLabel);
			}
		});
		
		final Button buConnectInOut = new Button("add incoming/outgoing keylogs");
		buConnectInOut.setTitle("Requests all keylogs and related keys that are incoming to or outgoinig from the selected keys.");
		buConnectInOut.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				addKeysAndLogsForSelection(true,true,userErrorLabel);
			}
		});
		
		final Button buRemove = new Button("remove keys and logs");
		buRemove.setTitle("Removes all selected keys and related keylogs.");
		buRemove.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				removeSelectedKeys(userErrorLabel);
			}
		});
		
		panelSelection.add(new Label("Selection:"));
		panelSelection.add(buConnectIn);
		panelSelection.add(buConnectOut);
		panelSelection.add(buConnectInOut);
		panelSelection.add(buRemove);
		
		mainPanel.add(wrapInDiv("selectionsPanel",panelSelection));
	}

	private void initAddKeyPanel() {
		HorizontalPanel panelAdd = new HorizontalPanel();
		panelAdd.addStyleName("hPanel");
		
		final TextBox txKeyid = new TextBox();
		txKeyid.setText("3C:02:09:27:0B:DD:99:3F:44:DD:D0:27:1A:5C:38:CE:AF:04:F5:4E@localhost");
		txKeyid.setWidth("600px");
		
		final Button buAdd = new Button("add");
		buAdd.addStyleName("sendButton");
		buAdd.setWidth("80px");
		buAdd.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				String addKeyid = txKeyid.getText();
				if (!FieldVerifier.isValidKeyId(addKeyid)) {
					userErrorLabel.setVisible(true);
					userErrorLabel.setText("Please enter a valid keyid");
					Timer t = new Timer() {
						public void run() {
							userErrorLabel.setVisible(false);
							userErrorLabel.setText("");
						}
					};
					t.schedule(4000);
					return;
				}
				Vector<String> keyids = new Vector<String>();
				keyids.add(addKeyid);
				boolean inLogs = true;
				boolean outLogs = true;
				
				keyserverService.updateKeyInfoAndLogs(user.getName(), getCurrentNodeStates(), keyids, inLogs, outLogs, 
						new AsyncCallback<User>() {
							public void onFailure(Throwable caught) {
								userErrorLabel.setVisible(true);
								userErrorLabel.setText(SERVER_ERROR);
								Timer t = new Timer() {
									public void run() {
										userErrorLabel.setVisible(false);
										userErrorLabel.setText("");
									}
								};
								t.schedule(4000);
							}
							
							public void onSuccess(User result) {
								if (result!=null) {
									for (KeyInfo ki : result.getKeys()) {
										user.addKey(ki);
									}
									for (KeyConnection kc : result.getConnections()) {
										user.addConnection(kc);
									}
									updateUI(UI_USER);
								}
							}
				});
			}
		});
		
		panelAdd.add(new Label("Key id:"));
		panelAdd.add(txKeyid);
		panelAdd.add(buAdd);
		mainPanel.add(wrapInDiv("addPanel",panelAdd));
	}

	private void initCheckBoxHandler() {
		if (checkBoxHandler == null) {
			checkBoxHandler = new ClickHandler() {
				public void onClick(ClickEvent event) {
					graphOverlayContext.clearRect(0,0,w,h);
					drawSelected();
				}
			};
		}
	}

	private void initKeyLogHandler() {
		//build user ui
		if (buKeyLogHandler == null) {
			buKeyLogHandler = new ClickHandler() {
				public void onClick(ClickEvent event) {
					String keyid = "unknown";
					Object srcObj= event.getSource();
					if (srcObj instanceof Button) {
						Button b = (Button)srcObj;
						keyid = b.getTitle();
						if (keyid!=null && keyid.length()>0) {
							Vector<KeyConnection> logs = user.getLogs(keyid);
							
							if (logs.size()==0) {
								String msg = "No KeyLogs found.";
								DialogBox box = buildMessageBox("Keylogs for Key: "+keyid, msg);
								box.setPopupPosition(event.getClientX()-400, event.getClientY()+10);
								box.show();
							} else {
								FlexTable tableLogs = new FlexTable();
								tableLogs.setTitle("KeyLogs");
								tableLogs.setHTML(0, 0, "from");
								tableLogs.setHTML(0, 1, "to");
								tableLogs.setHTML(0, 2, "type");
								tableLogs.setHTML(0, 3, "date");
								
								for (int i=0;i<logs.size();i++) {
									KeyConnection kc = logs.get(i);
									int row = i+1;
									KeyInfo kiFrom = keyid_key.get(kc.getFromId());
									KeyInfo kiTo = keyid_key.get(kc.getToId());
									if (kiFrom==null) {
										tableLogs.setHTML(row, 0, kc.getFromId());	
									} else {
										tableLogs.setHTML(row, 0, kiFrom.getIdShort()+"<br/>"+kiFrom.getOwner());
									}
									
									if (kiTo==null) {
										tableLogs.setHTML(row, 1, kc.getToId());	
									} else {
										tableLogs.setHTML(row, 1, kiTo.getIdShort()+"<br/>"+kiTo.getOwner());
									}
									tableLogs.setHTML(row, 2, kc.getTypeText());
									tableLogs.setHTML(row, 3, datetimeformat.format(new Date(kc.getDate())));
								}
								//style
								tableLogs.getRowFormatter().setStylePrimaryName(0, "myTableHeader");
								
								ScrollPanel scroll = new ScrollPanel(wrapInDiv("keylogPanel",tableLogs));
								scroll.addStyleName("scrollPanel");
								
								DialogBox box = buildBox("Keylogs for Key: "+keyid, scroll);
								box.setPopupPosition(event.getClientX()-400, event.getClientY()+10);
								box.show();
							}
						}
					}
				}
			};
		}
	}

	private void initLoginView() {
		mainPanel.clear();
		mainPanel.setSize("700px", "150px");
		
		//panel Login
		
		final VerticalPanel panelLogin = new VerticalPanel();
		final Button buLogin = new Button("Login");
		buLogin.addStyleName("sendButton");
		
		buLogin.setWidth("150px");
		final TextBox txUser = new TextBox();
		txUser.setText("debug@it-is-awesome.de");
		txUser.setWidth("300px");
		final PasswordTextBox txPW = new PasswordTextBox();
		txPW.setText("");
		txPW.setWidth("300px");
		final Label errorLabel1 = new Label();
		errorLabel1.addStyleName("errorLabel");

		FlexTable tabL = new FlexTable();
		tabL.setTitle("Start anonymouse session");
		tabL.setWidget(0,0,new Label("Email:"));
		tabL.setWidget(0,1,txUser);
		tabL.setWidget(1,0,new Label("Password:"));
		tabL.setWidget(1,1,txPW);
		tabL.setWidget(2,1,buLogin);
		
		panelLogin.add(new HTML("<h3>Login</h3>"));
		panelLogin.add(tabL);
		panelLogin.add(errorLabel1);
		panelLogin.addStyleName("innerPanel");
		
		class LoginHandler implements ClickHandler, KeyUpHandler {
			public void onClick(ClickEvent event) {
				login();
			}
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					login();
				}
			}
			
			private void login() {
				//errorLabel1.setText("Login not implemented");
				errorLabel1.setText("");
				String username = txUser.getText();
				String password = txPW.getText();
				if (username.length()==0) {
					errorLabel1.setText("Please enter a correct email address");
					return;
				}
				if (password.length()==0) {
					errorLabel1.setText("Please enter a password");
					return;
				}
				buLogin.setEnabled(false);
				keyserverService.login(username, password,
					new AsyncCallback<User>() {
						public void onFailure(Throwable caught) {
							errorLabel1.setText(SERVER_ERROR);
							buLogin.setEnabled(true);
						}
						public void onSuccess(User result) {
							if (result == null) {
								errorLabel1.setText("Wrong email or password");
								buLogin.setEnabled(true);
							} else {
								user = result;
								updateUI(UI_USER);
							}
						}
					}
				);
			}
		}
		LoginHandler lHandler = new LoginHandler();
		buLogin.addClickHandler(lHandler);
		txPW.addKeyUpHandler(lHandler);

		//panel Anonymous
		final VerticalPanel panelAnonymous = new VerticalPanel();
		final Button buStart = new Button("Start");
		buStart.addStyleName("sendButton");
		buStart.setWidth("100px");
		final TextBox txKeyid = new TextBox();
		txKeyid.setText("81:9A:BC:86:49:2B:0A:17:82:52:2A:D2:34:1E:B0:34:22:04:52:38@it-is-awesome.de");
		txKeyid.setWidth("600px");
		
		final Label errorLabel2 = new Label();
		errorLabel2.addStyleName("errorLabel");

		FlexTable tab = new FlexTable();
		tab.setTitle("Start anonymous session");
		tab.setWidget(0,0,new Label("Key ID:"));
		tab.setWidget(0,1,txKeyid);
		tab.setWidget(1,1,buStart);
		
		panelAnonymous.add(new HTML("<h3>Start anonymous session</h3>"));
		panelAnonymous.add(tab);
		panelAnonymous.add(errorLabel2);
		panelAnonymous.addStyleName("innerPanel");
		
		mainPanel.add(wrapInDiv("loginPanel", panelLogin));
		mainPanel.add(wrapInDiv("anonymousPanel", panelAnonymous));
		
		txKeyid.setFocus(true);
		txKeyid.selectAll();

		class AnonymousHandler implements ClickHandler, KeyUpHandler {
			public void onClick(ClickEvent event) {
				loginAnonymous();
			}
			public void onKeyUp(KeyUpEvent event) {
				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
					loginAnonymous();
				}
			}
			
			private void loginAnonymous() {
				buStart.setEnabled(false);
				errorLabel2.setText("");
				String textToServer = txKeyid.getText();
				if (!FieldVerifier.isValidKeyId(textToServer)) {
					errorLabel2.setText("Please enter a valid keyid");
					return;
				}
				
				keyserverService.loginAnonymous(textToServer,
					new AsyncCallback<User>() {
						public void onFailure(Throwable caught) {
							errorLabel2.setText(SERVER_ERROR);
						}
						public void onSuccess(User result) {
							user = result;
							updateUI(UI_USER);
						}
					}
				);
			}
		}

		AnonymousHandler aHandler = new AnonymousHandler();
		buStart.addClickHandler(aHandler);
		txKeyid.addKeyUpHandler(aHandler);
	}
	
	private HTMLPanel wrapInDiv(String id, Widget w) {
		HTMLPanel pan = new HTMLPanel("");
		pan.getElement().setId(id);
		pan.add(w);
		return pan;
	}
	
	private void removeSelectedKeys(final Label errorLabel) {
		Vector<String> keyids = getSelectedKeyIds();
		for (String keyid : keyids) {
			user.removeKey(keyid);
		}
		updateUI(UI_USER);
		
		// 13:1E:17:1A:A6:55:87:FA:20:0B:20:43:F7:79:DE:0C:09:6D:89:C8@localhost
	}
	private void addKeysAndLogsForSelection(boolean inLogs, boolean outLogs, final Label errorLabel) {
		
		Vector<String> keyids = getSelectedKeyIds();
		if (keyids.size()==0) {
			errorLabel.setVisible(true);
			errorLabel.setText("Please select a key first.");
			Timer t = new Timer() {
				public void run() {
					errorLabel.setVisible(false);
					errorLabel.setText("");
				}
			};
			t.schedule(3000);
			return;
		}

		keyserverService.updateKeyInfoAndLogs(user.getName(), getCurrentNodeStates(), keyids, inLogs, outLogs, 
				new AsyncCallback<User>() {
					public void onFailure(Throwable caught) {
						errorLabel.setVisible(true);
						errorLabel.setText(SERVER_ERROR);
						Timer t = new Timer() {
							public void run() {
								errorLabel.setVisible(false);
								errorLabel.setText("");
							}
						};
						t.schedule(4000);
					}
					
					public void onSuccess(User result) {
						if (result!=null) {
							for (KeyInfo ki : result.getKeys()) {
								user.addKey(ki);
							}
							for (KeyConnection kc : result.getConnections()) {
								user.addConnection(kc);
							}
							updateUI(UI_USER);
						}
					}
		});
	}
	
	private Vector<String> getSelectedKeyIds() {
		Vector<String> sel = new Vector<String>();
		for (int i=0;i<checks.size();i++) {
			if (checks.get(i).getValue()) {
				sel.add(user.getKeys().get(i).getId());
			}
		}
		return sel;
	}
	
	private DialogBox buildMessageBox(String title, String msgHtml) {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText(title);
		dialogBox.setAnimationEnabled(true);
		final Button buClose = new Button("Close");
		final HTML msg = new HTML(msgHtml);
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.add(msg);
		vPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vPanel.add(buClose);
		dialogBox.setWidget(vPanel);
		buClose.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		return dialogBox;
	}
	
	private DialogBox buildBox(String title, Widget widget) {
		final DialogBox dialogBox = new DialogBox();
		dialogBox.setText(title);
		dialogBox.setAnimationEnabled(true);
		final Button buClose = new Button("Close");
		VerticalPanel vPanel = new VerticalPanel();
		vPanel.add(widget);
		vPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
		vPanel.add(buClose);
		dialogBox.setWidget(vPanel);
		buClose.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				dialogBox.hide();
			}
		});
		return dialogBox;
	}

	public void buildCanvas() {
		graph = Canvas.createIfSupported();
		if (graph == null) {
			RootPanel.get().insert(new Label("Your browser does NOT support HTML5 Canvas!!"), 0);
			return;
		}		
		//graph.setStyleName("mainCanvas");
		
		graph.setWidth(w+"px");
		graph.setCoordinateSpaceWidth(w);
		
		graph.setHeight(h+"px");
		graph.setCoordinateSpaceHeight(h);
		graphContext = graph.getContext2d();
		
		
		graphOverlay = Canvas.createIfSupported();
		graphOverlay.setWidth(w+"px");
		graphOverlay.setCoordinateSpaceWidth(w);
		graphOverlay.setHeight(h+"px");
		graphOverlay.setCoordinateSpaceHeight(h);
		
		graphOverlayContext = graphOverlay.getContext2d();
		graphOverlayContext.clearRect(0,0,w,h);
		graphOverlayContext.setLineWidth(2);
		graphOverlayContext.setFillStyle(CssColor.make(0,0,0));
		
		AbsolutePanel absPanel = new AbsolutePanel();
		absPanel.setWidth(w+"px");
		absPanel.setHeight(h+"px");
		absPanel.add(graph);
		absPanel.add(graphOverlay);
		absPanel.getElement().setId("graphPanel");
		
		absPanel.setWidgetPosition(graph, 0, 0);
		absPanel.setWidgetPosition(graphOverlay, 0, 0);
		
		mainPanel.add(absPanel);

		graphOverlay.addMouseMoveHandler(new MouseMoveHandler() {
			public void onMouseMove(MouseMoveEvent ev) {
				mouseX = ev.getX();
				mouseY = ev.getY();
				if (mouseDown && selectedNode!=null) {
					
					graphOverlayContext.clearRect(0,0,w,h);
					//graphOverlayContext.fillText(mouseX+", "+mouseY, mouseX, mouseY);
					
					graphOverlayContext.setFillStyle(selectColor);
					graphOverlayContext.setStrokeStyle(selectColor);
					
					drawCircle(graphOverlayContext, selectedNode.getPosX(), selectedNode.getPosY(), radiusNode);
					drawCircle(graphOverlayContext, mouseX, mouseY, radiusNode);
					
					for (int[] pos : selectedTo) {
						drawArrow(graphOverlayContext, pos[0], pos[1], mouseX, mouseY,1);
					}
					for (int[] pos : selectedFrom) {
						drawArrow(graphOverlayContext, mouseX, mouseY, pos[0], pos[1],1);
					}
					drawNumberOutline(graphOverlayContext, selectedNode.getNo(), selectedNode.getPosX(), selectedNode.getPosY());
					drawNumberOutline(graphOverlayContext, selectedNode.getNo(), mouseX, mouseY);
					
				} else {
					keyDetails.hide();
					graphOverlayContext.clearRect(0,0,w,h);
					drawSelected();
					
					KeyInfo ki = checkForKeyInfo(mouseX, mouseY);
					if (ki!=null) {
						graphOverlayContext.setFillStyle(selectColor);
						int x = ki.getPosX();
						int y = ki.getPosY();
						drawCircle(graphOverlayContext, x, y, radiusNode+2);
						drawNumberOutline(graphOverlayContext, ki.getNo(),x,y);
						if (cbShowDetails.getValue()) {
							int ox = 30;
							if (mouseX>500) ox = -500;
							int oy = 30;
							if (mouseY>350) oy = -350;
							int top = graphOverlay.getAbsoluteTop();
							int left = graphOverlay.getAbsoluteLeft();
							keyDetails.setPopupPosition(left+mouseX+ox, top+mouseY+oy);
							keyDetails.update(ki);
							keyDetails.show();
						}
					} else {
						keyDetails.hide();
					}
				}
			}
		});
		graphOverlay.addMouseDownHandler(new MouseDownHandler() {
			public void onMouseDown(MouseDownEvent ev) {
//				mouseDownX = ev.getX();
//				mouseDownY = ev.getY();
				mouseDown = true;
				shiftDown = ev.isShiftKeyDown();
				
				graphOverlayContext.clearRect(0,0,w,h);
				//graphOverlayContext.fillText(mouseX+", "+mouseY, mouseX, mouseY);

				selectedNode = checkForKeyInfo(mouseX, mouseY);
				if (selectedNode!=null) {
					selectConnections(selectedNode.getId());
					if (shiftDown) {
						toggleSelected(selectedNode.getNo());
					}
				} else {
					if (shiftDown) {
						//remove selection
						for (CheckBox cb : checks) {
							cb.setValue(false);
						}
					}
				}
				drawSelected();
				
			}
		});
		graphOverlay.addMouseUpHandler(new MouseUpHandler() {
			public void onMouseUp(MouseUpEvent ev) {
//				mouseUpX = ev.getX();
//				mouseUpY = ev.getY();
				if (mouseDown && selectedNode!=null) {
					graphOverlayContext.clearRect(0,0,w,h);
					selectedNode.setPosX(mouseX);
					selectedNode.setPosY(mouseY);
					drawGraph();
					drawSelected();
				}
				mouseDown = false;
			}
		});
	
	}
	
	private void drawSelected() {
		//selected
		Vector<String> select = getSelectedKeyIds();
		for (String sel : select) {
			KeyInfo ki = keyid_key.get(sel);
			if (ki!=null) {
				int x = ki.getPosX();
				int y = ki.getPosY();
				graphOverlayContext.setFillStyle(selectColor);
				graphOverlayContext.fillRect(x-radiusCircle, y-radiusCircle/2, 2*radiusCircle, radiusCircle);
				//drawCircle(graphOverlayContext, x, y, radiusCircle-3);
				drawNumberOutline(graphOverlayContext, ki.getNo(),x,y);
			}
		}
	}
	
	public void handleGetConnectionsToSelected(boolean in, boolean out) {
		Vector<String> selected = new Vector<String>();
		Vector<KeyInfo> keys = user.getKeys();
		for (int i=0; i<checks.size();i++) {
			if (checks.get(i).getValue()) {
				selected.add(keys.get(i).getId());
			}
		}
		
		keyserverService.updateKeyInfoAndLogs(user.getName(), getCurrentNodeStates(), selected, in, out,
			new AsyncCallback<User>() {
				public void onFailure(Throwable caught) {
					
				}
				
				public void onSuccess(User result) {
					String msg = "";
					
					DialogBox box = buildMessageBox("Result Message", msg);
					box.setPopupPosition(100, 100);
					box.show();
					//updateUI(UI_USER);
				}
			});
		
	}
	
	
	private Vector<NodeState> getCurrentNodeStates() {
		Vector<NodeState> states = new Vector<NodeState>();
		Vector<KeyInfo> kilist = user.getKeys();
		for (KeyInfo ki : kilist) {
			states.add(new NodeState(ki.getId(), ki.getPosX(), ki.getPosY(), ki.isIncomingLogs(), ki.isOutgoingLogs(), ki.isMyKey(), ki.isDirectTrust(), ki.getVisibilityLevel()));
		}
		return states;
	}
	
	private boolean isSelected(int keyNo) {
		return checks.get(keyNo-1).getValue().booleanValue();
	}
	private void setSelected(int keyNo, boolean value) {
		checks.get(keyNo-1).setValue(value);
	}
	private void toggleSelected(int keyNo) {
		CheckBox cb = checks.get(keyNo-1);
		cb.setValue(!cb.getValue().booleanValue());
	}
	 
	private void drawNumberOutline(Context2d g, int no, double px, double py) {
		 int ox = -2;
         if (no<10) {
        	ox = -2;
         }
         else if (no<100) {
        	ox = -6;
         } else {
        	 ox = -10;
         }
         g.setStrokeStyle(CssColor.make("white"));
    	 g.setFillStyle(CssColor.make("white"));
    	 g.strokeText(""+no, px+ox, py+3);
    	 
    	 g.setStrokeStyle(CssColor.make("black"));
    	 g.setFillStyle(CssColor.make("black"));
    	 g.fillText(""+no, px+ox, py+3);
	}
	
	public KeyInfo checkForKeyInfo(int x, int y) {
		Vector<KeyInfo> keys = user.getKeys();
		for (KeyInfo ki : keys) {
			if (ki.getVisibilityLevel() >= visibilityLevel) {
				if (Math.abs(ki.getPosX()-x)<radiusNode && Math.abs(ki.getPosY()-y)<radiusNode) {
					return ki;
				}
			}
		}
		return null;
	}
	
	private void selectConnections(String keyid) {
		selectedFrom.removeAllElements();
		selectedTo.removeAllElements();
		Vector<KeyInfo> in = keyid_connect_in.get(keyid);
		if (in!=null) {
			for (KeyInfo ki : in) {
				selectedTo.add(new int[]{ki.getPosX(), ki.getPosY()});
			}	
		}
		Vector<KeyInfo> out = keyid_connect_out.get(keyid);
		if (out!=null) {
			for (KeyInfo ki : out) {
				selectedFrom.add(new int[]{ki.getPosX(), ki.getPosY()});
			}
		}
	}
	
	public void layoutNodes() {
		if (user==null) return;
		Vector<KeyInfo> keys = user.getKeys();
		if (keys.size()==0) return;
		Vector<KeyInfo> layouted = new Vector<KeyInfo>();
		Vector<KeyInfo> notLayouted = new Vector<KeyInfo>();
		for (KeyInfo ki : keys) {
			if (ki.getVisibilityLevel() >= visibilityLevel) {
				if (ki.getPosX()==-1 && ki.getPosY()==-1) {
					notLayouted.add(ki);
				}  else {
					layouted.add(ki);
				}
			}
		}
		
		if (layouted.size()<3) {
			//circular
			int mX = w/2;
			int mY = h/2;
			int radius = (int)Math.rint(Math.min(mX,mY)*0.95);
			notLayouted.addAll(layouted);
			int nodecount = notLayouted.size();
			
			double winkel = 2.0 * Math.PI / nodecount;
			for (int i = 0; i < nodecount; i++) {
				int x = (int)Math.rint(mX + Math.cos(i*winkel)*radius);
				int y = (int)Math.rint(mY + Math.sin(i*winkel)*radius);
				notLayouted.get(i).setPosX(x);
				notLayouted.get(i).setPosY(y);
			}
		} else {
			//random layout
			for (KeyInfo ki : notLayouted) {
				int x = (int)Math.rint(Math.random()*(w-40))+20;
				int y = (int)Math.rint(Math.random()*(h-40))+20;
				ki.setPosX(x);
				ki.setPosY(y);
			}
		}
	}
	
	public void drawGraph() {
		if (user==null || graph==null) return;
		Context2d g = graphContext;
		
		g.clearRect(0,0,w,h);
		g.setLineWidth(2);
		
		Vector<KeyInfo> keys = user.getKeys();
		keyid_key.clear();
		keyid_connect_in.clear();
		keyid_connect_out.clear();
		connect_count.clear();
		if (keys.size()==0) return;
		
		for (KeyInfo ki : keys) {
			if (ki.getVisibilityLevel() >= visibilityLevel) {
				keyid_key.put(ki.getId(), ki);				
			}
		}
		
		//draw arrows
		Vector<KeyConnection> connections = user.getConnections();
		for (KeyConnection c : connections) {
			KeyInfo fromK = keyid_key.get(c.getFromId());
			if (fromK != null) {
				KeyInfo toK = keyid_key.get(c.getToId());
				if (toK != null) {
					String cname = fromK.getId()+toK.getId();
					Integer cc = connect_count.get(cname);
					int cCount = 1;
					if (cc==null) {
						connect_count.put(cname, 1);
					} else {
						cCount = cc.intValue()+1;
						connect_count.put(cname, cCount);
					}
					
					Vector<KeyInfo> in = keyid_connect_in.get(toK.getId());
					if (in == null) {
						in = new Vector<KeyInfo>();
						keyid_connect_in.put(toK.getId(),in);
					}
					in.add(fromK);
						
					Vector<KeyInfo> out = keyid_connect_out.get(fromK.getId());
					if (out == null) {
						out = new Vector<KeyInfo>();
						keyid_connect_out.put(fromK.getId(),out);
					}
					out.add(toK);
					
					int x0 = fromK.getPosX();
					int y0 = fromK.getPosY();
					int x1 = toK.getPosX();
					int y1 = toK.getPosY();
							
					g.setStrokeStyle(edgeColors[c.getType()]);
					g.setFillStyle(edgeColors[c.getType()]);
					drawArrow(g, x0,y0,x1,y1, cCount);
				}
			}
		}
		
		//draw keys
		for (KeyInfo ki : keyid_key.values()) {
			int noCol = 0;			
	         if (ki.getLevel().equals("MASTER")) {
	        	noCol = 1; 
	         }
	         else if (ki.getLevel().equals("SUB")) {
	        	 noCol = 2;
	         }
	         else if (ki.getLevel().equals("REVOKE")) {
	        	 noCol = 3;
	         }
	         
	         //valid color -> border
	         if (ki.isValid()) {
	        	 g.setFillStyle(validColor);
	         } else{
	        	 g.setFillStyle(notValidColor);
	         }
	         drawCircle(g, ki.getPosX(), ki.getPosY(), radiusCircle+3);
	         
	         //level color -> fill
	         g.setFillStyle(nodeColors[noCol]);
	         drawCircle(g, ki.getPosX(), ki.getPosY(), radiusCircle);
	         
	         g.setStrokeStyle(nodeTextColor[1]);
	         g.setFillStyle(nodeTextColor[0]);
	         int no = ki.getNo();
	         int ox = -2;
	         if (no<10) {
	        	ox = -2;
	         }
	         else if (no<100) {
	        	ox = -6;
	         } else {
	        	 ox = -10;
	         }
	         g.fillText(""+no, ki.getPosX()+ox, ki.getPosY()+3);	         
		}
		
	}
	
    private void drawCircle(Context2d g, double x, double y, double radius) {
    	g.beginPath();
    	g.arc(x, y, radius, 0, fullCircle);
    	g.closePath();
    	g.fill();
    }
    
    private void drawArrow(Context2d g, double x0, int y0, int x1, int y1, int cCount) {
    	if (x0==x1 && y0 == y1) {
    		//self reference
    		double theta0 = Math.toRadians(0+(cCount-1)*20);
    		double theta1 = Math.toRadians(90+(cCount-1)*20);
    		
    		double fromX = x0 + radiusNode * Math.cos(theta0);
            double fromY = y0 + radiusNode * Math.sin(theta0);
            double toX = x1 - radiusNode * Math.cos(theta1);
            double toY = y1 - radiusNode * Math.sin(theta1);
            drawCircle(g, fromX, fromY, 4);
            drawCircle(g, toX, toY, 4);
            
            g.beginPath();
    		g.moveTo(fromX, fromY);
    		
            double cp1x = fromX+50;
            double cp1y = fromY-50;
            
            double cp2x = toX;
            double cp2y = toY;
            
    		g.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, toX, toY);
    	    g.stroke();
    	    
    	    g.beginPath();
    		g.moveTo(toX, toY);
    		
    		double theta2 = Math.toRadians(148);
    		
    	    double x, y;
            x = toX - barb * Math.cos(theta2+phi);
            y = toY - barb * Math.sin(theta2+phi);
            //drawLine(g, toX, toY, x, y);
            g.lineTo(x, y);
    		
            x = toX - barb * Math.cos(theta2-phi);
            y = toY - barb * Math.sin(theta2-phi); 
            //drawLine(g, toX, toY, x, y);
            g.lineTo(x, y);
    		g.closePath();
    		g.fill();
    		return;
    	}
    	
    	double theta = Math.atan2(y1 - y0, x1 - x0);
    	
    	double fromX = x0 + radiusNode * Math.cos(theta);
        double fromY = y0 + radiusNode * Math.sin(theta);
        double toX = x1 - radiusNode * Math.cos(theta);
        double toY = y1 - radiusNode * Math.sin(theta);
        
        drawCircle(g, fromX, fromY, 4);
        drawCircle(g, toX, toY, 4);
              
        //drawLine(g, fromX, fromY, toX, toY);
        double cBarb = (0.25+0.05*cCount)*Math.sqrt((fromX-toX)*(fromX-toX)+(fromY-toY)*(fromY-toY));
        double cPhi = Math.toRadians(11+(4*cCount));
        
        g.beginPath();
		g.moveTo(fromX, fromY);
		
        double cp1x = fromX - cBarb * Math.cos(theta+deg180-cPhi);
        double cp1y = fromY - cBarb * Math.sin(theta+deg180-cPhi);
        
        double cp2x = toX - cBarb * Math.cos(theta+cPhi);
        double cp2y = toY - cBarb * Math.sin(theta+cPhi);
        
		g.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, toX, toY);
	    g.stroke();
	    
	    
	    double theta2 = Math.atan2(toY - cp2y, toX - cp2x);

	    g.beginPath();
		g.moveTo(toX, toY);
		
	    double x, y;
        x = toX - barb * Math.cos(theta2+phi);
        y = toY - barb * Math.sin(theta2+phi);
        //drawLine(g, toX, toY, x, y);
        g.lineTo(x, y);
		
        x = toX - barb * Math.cos(theta2-phi);
        y = toY - barb * Math.sin(theta2-phi); 
        //drawLine(g, toX, toY, x, y);
        g.lineTo(x, y);
		g.closePath();
		g.fill();
    }
    
//	private void drawLine(Context2d g, double x0, double y0, double x1, double y1) {
//		g.beginPath();
//		g.moveTo(x0, y0);
//		g.lineTo(x1, y1);
//		g.closePath();
//	    g.stroke();
//	}
//	
//	private void drawCurve(Context2d g, double x0, double y0, double x1, double y1, double phi, double barb) {
//		g.beginPath();
//		g.moveTo(x0, y0);
//		
//		double theta = Math.atan2(y1 - y0, x1 - x0);
//        
//        double x, y;
//        double cp1x = x0 - barb * Math.cos(theta+phi);
//        double cp1y = y0 - barb * Math.sin(theta+phi);
//        
//        double cp2x = x1 - barb * Math.cos(theta+phi);
//        double cp2y = y1 - barb * Math.sin(theta+phi);
//        
//		g.bezierCurveTo(cp1x, cp1y, cp2x, cp2y, x1, y1);
//	    g.stroke();
//	}
	
}
