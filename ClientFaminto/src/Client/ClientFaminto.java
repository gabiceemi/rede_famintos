package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Scanner;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class ClientFaminto implements Runnable {

	private String localhost = "127.0.0.1";
    private int portNumber = 2223;

	public static void main(String[] args) throws Throwable {
		ClientFaminto cf = new ClientFaminto();
		cf.run();
	}

	public ClientFaminto() {

	}

	public void run() {
		
		SSLContext sslContext = this.createSSLContext();
        
        try{
        	
            SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
             
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(this.localhost, this.portNumber);
            
            new ClientThread(sslSocket).start();
            
        } catch (Exception ex){
            ex.printStackTrace();
        }
	}

	private SSLContext createSSLContext() {
		try{
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(new FileInputStream("C:\\Users\\gabic\\keyFaminto.jks"),"password".toCharArray());
             
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
            keyManagerFactory.init(keyStore, "password".toCharArray());
            KeyManager[] km = keyManagerFactory.getKeyManagers();
             
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
            trustManagerFactory.init(keyStore);
            TrustManager[] tm = trustManagerFactory.getTrustManagers();
             
            SSLContext sslContext = SSLContext.getInstance("TLSv1");
            sslContext.init(km,  tm, null);
             
            return sslContext;
        } catch (Exception ex){
            ex.printStackTrace();
        }
         
        return null;
	}

	static class ClientThread extends Thread {
		private SSLSocket sslSocket = null;
		private String request;
		private String response;
		private Scanner reader;
		private boolean loop = true;

		ClientThread(SSLSocket sslSocket) {
			this.sslSocket = sslSocket;
		}

		public void run() {
			
	            sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());
	             
	            try{
	                sslSocket.startHandshake();

				DataOutputStream printWriter = new DataOutputStream(sslSocket.getOutputStream());
				DataInputStream in = new DataInputStream(sslSocket.getInputStream());

				reader = new Scanner(System.in);

				while (loop == true) {

					System.out.println(" \n \n ===============Menu Principal====================");
					System.out.println("\n -- \\comandos  \n -- \\trendFaminto \n -- \\receita  \n -- \\sair ");
					request = reader.nextLine();

					String ingredient[] = request.split("\\s+");
					String command = "\\comandos";
					
					if (ingredient.length >= 0) {
						command = ingredient[0];
					}

					switch (command) {
					case "\\comandos":
						response = "\\comandos";
						printWriter.writeUTF(response);
						printWriter.flush();
						response = in.readUTF(); 
		                System.out.println(response);

						break;

					case "\\trendFaminto":
						response = "\\trendFaminto";
						printWriter.writeUTF(response);
						printWriter.flush();	
						response = in.readUTF(); 
		                System.out.println(response);

						break;

					case "\\receita":
						response = "\\receita " + ingredient[1];
						printWriter.writeUTF(response);
						printWriter.flush();
						response = in.readUTF(); 
		                System.out.println(response);

						break;

					case "\\sair":
						response = "\\sair";
						printWriter.writeUTF(response);
						printWriter.flush();
						in.close();
						printWriter.close();
						sslSocket.close();
						loop = false;

						break;
						
					default:
						System.out.println("Desculpa, não entendemos o que você quiser dizer.");

					}          
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}
}
