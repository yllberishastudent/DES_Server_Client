package application;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Server {
    private Socket socket = null;
    private ServerSocket server = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    public Server(int port) {
        try {
            // Server Start
            server = new ServerSocket(port);
            System.out.println("Server started.");
            System.out.println("Waiting for client");
            
            // Client Connect
            socket = server.accept();
            System.out.println("Client accepted.\n");
          
            // Initialize input/output streams
            in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            out = new DataOutputStream(socket.getOutputStream());
            Scanner input = new Scanner(System.in);

            // Get key
            Path path = Paths.get("KeyFile.txt");
            byte[] key = Files.readAllBytes(path);
            SecretKey desKey = new SecretKeySpec(key, 0, key.length, "DES");

            // Initialize decrypter/encryptor
            Cipher decipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            decipher.init(Cipher.DECRYPT_MODE, desKey);

            Cipher ecipher = Cipher.getInstance("DES/ECB/PKCS5Padding");
            ecipher.init(Cipher.ENCRYPT_MODE, desKey);

            String decLine = "";
            String sentLine = "";

            while (!decLine.equals("Exit") || !sentLine.equals("Exit")) {
                try {
                    int length = in.readInt();

                    if(length > 0) {
                        System.out.println("\nClient message: ");
                        // Get message bytes
                        byte[] message = new byte[length];
                        in.readFully(message, 0, message.length);

                        // Decrypt it
                        byte[] decBytes = decipher.doFinal(message);
                        decLine = new String(decBytes);

                        System.out.println("--------------------------------------------------------------------");
                        System.out.println("Encrypted: " + new String(message));
                        System.out.println("Key: " + key);
                        System.out.println("Decrypted: " + decLine);
                        System.out.println("--------------------------------------------------------------------");
                         
                        if (decLine.equals("Over")) {
                            break;
                        }

                        // Server responds
                        System.out.print("\nEnter response: ");
                        sentLine = input.nextLine();

                        // Encrypt message
                        byte[] encLine = ecipher.doFinal(sentLine.getBytes());

                        System.out.println("--------------------------------------------------------------------");
                        System.out.println("Plaintext: " + sentLine);
                        System.out.println("Key: " + key);
                        System.out.println("Encrypted: " + new String(encLine));
                        System.out.println("--------------------------------------------------------------------");
                        
                        // Send to client
                        out.writeInt(encLine.length);
                        out.write(encLine);
                    }
                }
                catch(IOException i) {
                    System.out.println(i);
                } catch (BadPaddingException e) {
                    e.printStackTrace();
                } catch (IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("Closing connection.");

            socket.close();
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args) {
        Server server = new Server(5000);
    }
}

