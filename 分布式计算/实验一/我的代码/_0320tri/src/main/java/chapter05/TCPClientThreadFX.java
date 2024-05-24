package chapter05;

import chapter01.TextFileIO;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;


public class TCPClientThreadFX extends Application {
    private Button btnExit = new Button("退出");
    private Button btnSend = new Button("发送");
    private Button btnConnect = new Button("连接");


    Thread receiveThread; //定义成员变量，读取服务器信息的线程

//    private Button btnOpen = new Button("加载");
//    private Button btnSave = new Button("保存");
    //待发送信息的文本框



    private TextField tfip = new TextField();
    private TextField tfport = new TextField();

    //下面两行是3.26补充
    private RadioButton rbDot = new RadioButton("点乘");
    private RadioButton rbCross = new RadioButton("叉乘");
    //3.26注释
     private TextField tfSend = new TextField();

    //3.26
    private TextField tfVector1 = new TextField();
    private TextField tfVector2 = new TextField();


    //显示信息的文本区域
    private TextArea taDisplay = new TextArea();

    private TCPClient tcpClient;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        //    将TextFileIO类实例化为textFileIO //3.26注释
        //TextFileIO textFileIO = new TextFileIO();

        BorderPane mainPane = new BorderPane();

//        顶部的ip和端口输入框区域
        HBox topHBox = new HBox();
        topHBox.setSpacing(10);
        topHBox.setPadding(new Insets(10,20,10,20));
        topHBox.setAlignment(Pos.CENTER);
        tfip.setPromptText("输入 IP");//3.27
        tfport.setPromptText("输入端口");//3.27
        topHBox.getChildren().addAll(new Label("IP: "),tfip,new Label("端口号："),tfport,btnConnect);
        mainPane.setTop(topHBox);

        //下面3.27加
        // 信息显示区域
        taDisplay.setEditable(false);
        taDisplay.setWrapText(true);


        //内容显示区域
        VBox vBox = new VBox();
        vBox.setSpacing(10);//各控件之间的间隔
        //VBox面板中的内容距离四周的留空区域
        vBox.setPadding(new Insets(10,20,10,20));

        // 操作类型选择下面10行3.26加
        ToggleGroup operationGroup = new ToggleGroup();
        rbDot.setToggleGroup(operationGroup);
        rbCross.setToggleGroup(operationGroup);
        rbDot.setSelected(true); // 默认选择点乘

        HBox operationBox = new HBox(10, rbDot, rbCross);
        operationBox.setAlignment(Pos.CENTER);

        // 向量输入

        tfVector1.setPromptText("输入向量1");//3.27
        tfVector2.setPromptText("输入向量2");//3.27
        HBox vectorInputBox = new HBox(10, new Label("向量1:"), tfVector1, new Label("向量2:"), tfVector2);
        vectorInputBox.setAlignment(Pos.CENTER);

        // 将所有元素组合到 VBox 中 ——>327删
        //vBox.getChildren().addAll(new Label("信息显示区："), taDisplay,new Label("信息输入区："), tfSend);
       //换成了下面这行
        vBox.getChildren().addAll(new Label("信息显示区："), taDisplay, operationBox, vectorInputBox);

        //设置显示信息区的文本区域可以纵向自动扩充范围
        VBox.setVgrow(taDisplay, Priority.ALWAYS);
        mainPane.setCenter(vBox);


        //底部按钮区域
        HBox hBox = new HBox();
        hBox.setSpacing(10);
        hBox.setPadding(new Insets(10,20,10,20));
        hBox.setAlignment(Pos.CENTER_RIGHT);
//        hBox.getChildren().addAll(btnSend,btnSave,btnOpen,btnExit);
        hBox.getChildren().addAll(btnSend,btnExit);
        mainPane.setBottom(hBox);

        // 设置场景
        Scene scene = new Scene(mainPane,700,400);
        primaryStage.setScene(scene);
        primaryStage.setTitle("向量计算客户端");
        primaryStage.show();
//……
        //   --------事件处理代码部分--------
//……

//        连接
        btnConnect.setOnAction(event -> {
            String ip = tfip.getText().trim();
            String port = tfport.getText().trim();

            try {
                tcpClient = new TCPClient(ip,port); //tcpClient不是局部变量，是本程序定义的一个TCPClient类型的成员变量
                taDisplay.appendText("对话已连接" + "\n");
                receiveThread = new Thread(()->{
                    String msg = null;
                    while ((msg = tcpClient.receive()) != null) {
                        //runLater中的lambda表达式不能直接访问外部非final类型局部变量
                        //所以这里使用了一个临时常量，可以省略final，但本质还是会作为常量使用
                        final String msgTemp = msg; //msgTemp实质是final类型
                        Platform.runLater(()->{
                            taDisplay.appendText( msgTemp + "\n");
                        });
                    }
//跳出了循环，说明服务器已关闭，读取为null，提示对话关闭
                    Platform.runLater(()->{
                        taDisplay.appendText("对话已关闭！\n" );
                    });
                }, "my-readServerThread"); //给新线程取别名，方便识别
                receiveThread.start(); //启动线程
            } catch (Exception e) {
                taDisplay.appendText("服务器连接失败！" + e.getMessage() + "\n");
            }
        });

        btnExit.setOnAction(event -> {
            if(tcpClient != null){
                //向服务器发送关闭连接的约定信息
                tcpClient.send("bye");
                tcpClient.close();
            }
            System.exit(0);
        });

//        设置taDisplay自动换行
        taDisplay.setWrapText(true);
//        设置taDisplay只读
        taDisplay.setEditable(false);
//        退出按钮事件
//        btnExit.setOnAction(event -> {System.exit(0);});
        btnExit.setOnAction(event -> endSystem());
//        发送按钮事件
        btnSend.setOnAction(event -> {

            //3.26
            String operation = rbDot.isSelected() ? "dot" : "cross";
            String vector1 = tfVector1.getText().trim();
            String vector2 = tfVector2.getText().trim();
            String message = operation + ";" + vector1 + ";" + vector2+"\n";
            // String sendMsg = tfSend.getText();
            //if (sendMsg.equals("bye")){
            //  btnConnect.setDisable(false);
            //  btnSend.setDisable(true);
            //}
            if(tcpClient!=null){
            tcpClient.send(message);//向服务器发送一串字符,发送构造好的消息
            // tcpClient.send(sendMsg);//向服务器发送一串字符

            taDisplay.appendText("客户端发送：" + message + "\n");
            //taDisplay.appendText("客户端发送：" + sendMsg + "\n");;
            }
        });
    }
    private void endSystem(){
        if (tcpClient!=null){
            tcpClient.send("bye");
            tcpClient.close();
        }
        System.exit(0);
    }
}