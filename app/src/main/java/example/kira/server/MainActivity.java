package example.kira.server;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

import example.kira.server.R;

public class MainActivity extends AppCompatActivity {

    Thread ConnectionThread = null;
    private EditText etInput;
    private TextView tvMsgArea;
    private Button btnSend;

    Socket socket;
    String line;
    String msgout;
    Thread handleServer;
    public static final int SERVERPORT = 5001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        etInput = findViewById(R.id.etInput);
        tvMsgArea = findViewById(R.id.tvMsgArea);
        btnSend = findViewById(R.id.btnSend);
        tvMsgArea.setText("Waiting for client's connection...");


        this.ConnectionThread = new Thread(new ConnectionThread());
        this.ConnectionThread.start();


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {

                            msgout = "Server: " + etInput.getText().toString().trim() + "\n";
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    etInput.setText("");
                                    tvMsgArea.setText(tvMsgArea.getText().toString().trim() + "\n" + msgout);
                                }
                            });

                            OutputStream outputStream = socket.getOutputStream();

                            outputStream.write(msgout.getBytes());


                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();


            }
        });


    }

    class ConnectionThread implements Runnable {
        public void run() {

            try {
                ServerSocket serverSocket=new ServerSocket(SERVERPORT);
                while(true){
                    socket = serverSocket.accept();

                    msgout = "Server: You have connected to the server..\n";
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            etInput.setText("");
                            tvMsgArea.setText(tvMsgArea.getText().toString().trim() + "\nClient has connected to the Server...\n");
                        }
                    });

                    OutputStream outputStream = socket.getOutputStream();

                    outputStream.write(msgout.getBytes());

                    HandleClientSocket handleClientSocket = new HandleClientSocket(socket);
                    handleServer=new Thread(handleClientSocket);
                    handleServer.start();
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class HandleClientSocket implements Runnable {
        private Socket clientSocket;
        private BufferedReader serverMsg;

        public HandleClientSocket(Socket clientSocket) {
            this.clientSocket = clientSocket;
            try {
                this.serverMsg = new BufferedReader(new InputStreamReader(this.clientSocket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        public void run() {

            // while (!Thread.currentThread().isInterrupted()) {

            try {

                while ((line = serverMsg.readLine()) != null) {
                    if ("quit".equalsIgnoreCase(line)) {
                        break;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            tvMsgArea.setText(tvMsgArea.getText().toString() +  line + "\n");
                        }
                    });


                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tvMsgArea.setText(tvMsgArea.getText().toString().trim() + "\nClient has disconnected, please restart the client...");
                    }

                });
                clientSocket.close();


            } catch (IOException e) {
                e.printStackTrace();
            }

            //   }


        }
    }
}
