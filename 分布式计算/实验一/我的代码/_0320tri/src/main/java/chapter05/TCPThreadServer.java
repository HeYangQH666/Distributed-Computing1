package chapter05;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import java.util.Arrays;


public class TCPThreadServer {
    private int port = 7777; //服务器监听端口
    private ServerSocket serverSocket; //定义服务器套接字

    private ExecutorService executorService;

    public TCPThreadServer() throws IOException {
        serverSocket = new ServerSocket(port);
        executorService = Executors.newCachedThreadPool();
        System.out.println("多用户服务器启动在" + port);
    }


    public void service() {
        while (true) {
            Socket socket = null;
            try {
                socket = serverSocket.accept(); //监听客户请求, 阻塞语句.
                //接受一个客户请求,从线程池中拿出一个线程专门处理该客户.
                executorService.execute(new Handler(socket));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    class Handler implements Runnable {
        private Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
//           System.out.println("New connection accepted:"+socket.getInetAddress());
            System.out.println("New connection accepted： " + socket.getInetAddress().getHostAddress());
            try {
                BufferedReader br = getReader(socket);
                PrintWriter pw = getWriter(socket);
                while (true) {
                    String msg = br.readLine(), res = "";
                    if("bye".equalsIgnoreCase(msg)) break;
                    String[] parts = msg.split(";");
                    if (parts.length == 3) { // 确保格式正确
                        String operation = parts[0].toLowerCase();
                        double[] vector1 = parseVector(parts[1]);
                        double[] vector2 = parseVector(parts[2]);
                        switch (operation) {
                            case "dot":
                                double dotResult = VectorOperations.dotProduct(vector1, vector2);
                                res = "Dot product result: " + dotResult;
                                break;
                            case "cross":
                                double[] crossResult = VectorOperations.crossProduct(vector1, vector2);
                                res = "Cross product result: " + Arrays.toString(crossResult);
                                break;
                            default:
                                res = "Unsupported operation: " + operation;
                                break;
                        }
                    } else {
                        res = "";
                    }
                    pw.println(res + "\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 解析向量的辅助方法
    private double[] parseVector(String vectorString) {
        //这个函数3.27改了
        try {
            return Arrays.stream(vectorString.replaceAll("[\\[\\]]", "").split(","))
                    .mapToDouble(Double::parseDouble)
                    .toArray();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Vector format is incorrect, expected format: [x,y,z]");
        }
    }


    private PrintWriter getWriter(Socket socket) throws IOException {
        //获得输出流缓冲区的地址
        OutputStream socketOut = socket.getOutputStream();
        //网络流写出需要使用flush，这里在PrintWriter构造方法中直接设置为自动flush
        return new PrintWriter(
                new OutputStreamWriter(socketOut, "utf-8"), true);
    }

    private BufferedReader getReader(Socket socket) throws IOException {
        //获得输入流缓冲区的地址
        InputStream socketIn = socket.getInputStream();
        return new BufferedReader(
                new InputStreamReader(socketIn, "utf-8"));
    }

    public static void main(String[] args) throws IOException {
        new TCPThreadServer().service();
    }
}

//添加VectorOperations类来实现点乘和叉乘的计算逻辑
