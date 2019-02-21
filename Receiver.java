import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Scanner;
import javax.swing.*;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Receiver extends JFrame implements ActionListener {
    static JPanel connectPanel = new JPanel();
    static JPanel clientPanel = new JPanel();
    static JButton connectButton = new JButton("CONNECT");
    static JButton disconnectButton = new JButton("DISCONNECT");
    static JLabel IPLabel = new JLabel("IP Address");
    static JLabel ackPortLabel = new JLabel("ACK Port Number");
    static JLabel dataPortLabel = new JLabel("Data Port Number");
    static JLabel fileNameLabel = new JLabel("File Name");
    static JTextField IPField = new JTextField("127.0.0.1");
    static JTextField ackPortField = new JTextField("400");
    static JTextField dataPortField = new JTextField("401");
    static JTextField fileNameField = new JTextField("");
    static JTextArea dataArea = new JTextArea("");
    static int packetSize;
    static int numPackets;
    static int leftOverByte;

    static DatagramSocket socket = null;
    static InetAddress address;
    
    static boolean connected = false;
    static boolean acknowledged[];

    Thread receiving = new Thread() {
        public void run() {
            try {
               
                String packetInfo[] = handshake().split(" ");
                packetSize = Integer.parseInt(packetInfo[0]);
                numPackets = Integer.parseInt(packetInfo[1]);
                leftOverByte = Integer.parseInt(packetInfo[2]);
                byte[][] file = new byte[numPackets][packetSize];
                acknowledged = new boolean[numPackets];
                boolean transmitting = true;
                byte[] buffer = new byte[packetSize];
                DatagramPacket packet = new DatagramPacket(buffer,packetSize);
                while (transmitting) {
                    
                    socket.receive(packet);
                    byte[] sequenceNumberByte = Arrays.copyOfRange(buffer, 0, 4);
                    byte[] filePortionByte;
                    int sequenceNumber = java.nio.ByteBuffer.wrap(sequenceNumberByte).getInt();
                    if(sequenceNumber==(numPackets-1)){
                    filePortionByte = Arrays.copyOfRange(buffer, 4, leftOverByte);
                    }else{
                    filePortionByte = Arrays.copyOfRange(buffer, 4, buffer.length);
                    }
                    if (sequenceNumber == -1) {
                        transmitting = false;
                        FileOutputStream out = new FileOutputStream("file.txt");
                        for (int i = 0; i < numPackets; i++) {
                            out.write(file[i]);
                        }
                    } else {
                        file[sequenceNumber] = filePortionByte;
                        
                        acknowledged[sequenceNumber] = true;
                        
                        if(sequenceNumber==numPackets){}
                        DatagramPacket pSend = new DatagramPacket(sequenceNumberByte,4);
                        socket.send(pSend);
                        
                    }
                }

            } catch (Exception e) {
                System.out.println("this"+ e + "this");
            }
        }
    };

    public Receiver() {
        this.setLocation(200, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
        connectPanelInit();
    }

    public String handshake() {
        try {
           
            byte[] buf = new byte[64]; // 2^8
            DatagramPacket p = new DatagramPacket(buf, 64);
            p.setData(buf);
            socket.receive(p);
            
            String handshake = new String(buf);
            return handshake;
        } catch (Exception e) {
            System.out.println(e);
        }
        return "";
    }

    public void actionPerformed(ActionEvent ae) {
        String action = ae.getActionCommand();
        try {
            if (action.equals("CONNECT")) {
                connectionInit();
                clientPanelInit();
                receiving.start();
            } else if (action.equals("DISCONNECT")) {
                socket.close();
                clientPanel.setVisible(false);
                connected = false;
                connectPanelInit();
                //receiving.stop();
            }
        } catch (Exception e) {
            System.out.println("eror");
            dataArea.setText("Input error");
        }
    }

    public void connectionInit() {
        try {
            socket = new DatagramSocket(Integer.parseInt(ackPortField.getText()));
            address = InetAddress.getByName(IPField.getText());
            socket.connect(address, Integer.parseInt(dataPortField.getText()));
            connectPanel.setVisible(false);
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void connectPanelInit() {
        add(connectPanel);
        connectPanel.setLayout(null);
        this.setSize(270, 170);
        this.setTitle("Connect");

        connectPanel.add(connectButton);
        connectPanel.add(IPLabel);
        connectPanel.add(ackPortLabel);
        connectPanel.add(dataPortLabel);
        connectPanel.add(IPField);
        connectPanel.add(ackPortField);
        connectPanel.add(dataPortField);
        connectPanel.setVisible(true);

        connectButton.setBounds(130, 80, 100, 25);
        connectButton.setVisible(true);

        IPField.setBounds(130, 30, 100, 25);
        IPField.setVisible(true);

        ackPortField.setBounds(20, 30, 100, 25);
        ackPortField.setVisible(true);

        dataPortField.setBounds(20, 80, 100, 25);
        dataPortField.setVisible(true);

        IPLabel.setBounds(130, 10, 80, 20);
        IPLabel.setVisible(true);

        ackPortLabel.setBounds(20, 10, 80, 20);
        ackPortLabel.setVisible(true);

        dataPortLabel.setBounds(20, 60, 80, 20);
        dataPortLabel.setVisible(true);

        connectButton.addActionListener(this);
    }

    public void clientPanelInit() throws Exception {
        add(clientPanel);
        clientPanel.setLayout(null);
        this.setSize(575, 400);
        this.setTitle("Client");

        clientPanel.setLayout(null);
        clientPanel.add(disconnectButton);
        clientPanel.setVisible(true);

        dataArea.setBounds(10, 200, 350, 150);
        dataArea.setVisible(true);
        dataArea.setEditable(false);
        dataArea.setLineWrap(true);

        disconnectButton.setBounds(380, 320, 170, 30);
        disconnectButton.setVisible(true);

        disconnectButton.addActionListener(this);
    }

    public static void main(final String[] args) throws Exception {
        Receiver mainView = new Receiver();
    }
}