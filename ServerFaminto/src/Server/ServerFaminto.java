package Server;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import org.json.JSONArray;
import org.json.JSONObject;


public class ServerFaminto implements Runnable {

	private int portNumber = 2223;
	private boolean isServerDone = false;
	private static int contador = 0;

	public static void main(String[] args) throws Throwable {
		ServerFaminto sf = new ServerFaminto();
		sf.run();
	}

	public ServerFaminto() {

	}

	public void run() {
		SSLContext sslContext = this.createSSLContext();

		try {

			SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();

			SSLServerSocket sslServerSocket = (SSLServerSocket) sslServerSocketFactory
					.createServerSocket(this.portNumber);

			System.out.println("Servidor Okey");
			while (!isServerDone) {
				SSLSocket sslSocket = (SSLSocket) sslServerSocket.accept();
				contador++;
				System.out.println("Clientes conectados: " + contador);
				new ServerThread(sslSocket).start();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private SSLContext createSSLContext() {
		try {

			KeyStore keyStore = KeyStore.getInstance("JKS");
			keyStore.load(new FileInputStream("C:\\Users\\gabic\\keyFaminto.jks"), "password".toCharArray());

			KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
			keyManagerFactory.init(keyStore, "password".toCharArray());
			KeyManager[] km = keyManagerFactory.getKeyManagers();

			TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance("SunX509");
			trustManagerFactory.init(keyStore);
			TrustManager[] tm = trustManagerFactory.getTrustManagers();

			SSLContext sslContext = SSLContext.getInstance("TLSv1");
			sslContext.init(km, tm, null);

			return sslContext;
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		return null;
	}

	// Thread de comunicação com o socket do cliente
	static class ServerThread extends Thread {
		private SSLSocket sslSocket = null;
		private String response;
		private String request;
		private String url = "https://food2fork.com/";
		private static String apiKey = "8856fbf69900b7052881bd93577a18af";
		private boolean loop = true;
		private final String USER_AGENT = "Mozilla/5.0";

		ServerThread(SSLSocket sslSocket) {
			this.sslSocket = sslSocket;
		}

		public void run() {

			sslSocket.setEnabledCipherSuites(sslSocket.getSupportedCipherSuites());

			try {

				sslSocket.startHandshake();

				DataOutputStream printWriter = new DataOutputStream(sslSocket.getOutputStream());
				DataInputStream in = new DataInputStream(sslSocket.getInputStream());

				while (loop == true) {

					while (((request = in.readUTF()) != null)) {

						String ingredient[] = request.split("\\s+");
						String command = "";

						if (ingredient.length >= 0) {
							command = ingredient[0];
						}

						switch (command) {
						case "\\comandos":
							response = "\n \\comandos \n  Lista os comandos disponíveis. \n \\trendFaminto  \n  Consulta a receita em destaque do Food2Fork. "
									+ "\n \\receita \n Consulta a receita mais popular que tenha o ingrediente que desejar.";
							printWriter.writeUTF(response);

							break;

						case "\\trendFaminto":
							response = searchRecipe();
							printWriter.writeUTF(response);

							break;

						case "\\receita":
							String requestedIngredient = ingredient[1];
							response = searchRecipeIngredient(requestedIngredient);
							printWriter.writeUTF(response);

							break;

						case "\\sair":
							in.close();
							printWriter.close();
							sslSocket.close();
							loop = false;

							break;
						}
					}
					sslSocket.close();
				}
			} catch (Exception ex) {
				ex.printStackTrace();
			} catch (Throwable e) {
				e.printStackTrace();
			}

		}

		public String searchRecipe() throws UnknownHostException, IOException {

			try {

				URL obj = new URL(url + "api/search?key=" + apiKey + "&rId=35382");
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethod("GET");

				con.setRequestProperty("User-Agent", USER_AGENT);

				int responseCode = con.getResponseCode();

				System.out.println("\n'GET' receita em destaque no food2fork");
				System.out.println("Response Code: " + responseCode);

				InputStream is = con.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader in = new BufferedReader(isr);
				StringBuilder inputLine = new StringBuilder();
				String line;
				while ((line = in.readLine()) != null) {
					inputLine.append(line);
					response = inputLine.toString();
					JSONObject objFaminto = new JSONObject(response);
					JSONArray recipes = objFaminto.getJSONArray("recipes");
					JSONObject recipe = recipes.getJSONObject(0);
					String link = recipe.getString("f2f_url");
					String recipeName = recipe.getString("title");
					response = "Nome da receita: " + recipeName + "\nVocê pode acessar ela pelo link: " + link;
					System.out.println(response);
				}
				in.close();
				con.disconnect();
			} catch (UnknownHostException e) {
				System.err.println("Não conectou com " + url);
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to " + url);
				System.exit(1);
			}
			return response;
		}

		public String searchRecipeIngredient(String ingredient) {
			try {
				URL obj = new URL(url + "api/search?key=" + apiKey + "&q=" + ingredient);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();

				con.setRequestMethod("GET");

				con.setRequestProperty("User-Agent", USER_AGENT);

				int responseCode = con.getResponseCode();

				System.out.println("\n'GET' receita que possui o ingrediente: " + ingredient);
				System.out.println("Response Code: " + responseCode);

				InputStream is = con.getInputStream();
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader in = new BufferedReader(isr);
				StringBuilder inputLine = new StringBuilder();
				String line;
				while ((line = in.readLine()) != null) {
					inputLine.append(line);
					response = inputLine.toString();
					JSONObject objFaminto = new JSONObject(response);
					JSONArray recipes = objFaminto.getJSONArray("recipes");
					JSONObject recipe = recipes.getJSONObject(0);
					String link = recipe.getString("f2f_url");
					String recipeName = recipe.getString("title");
					response = "Nome da receita: " + recipeName + "\nVocê pode acessar ela pelo link: " + link;
				}
				in.close();
				con.disconnect();
			} catch (UnknownHostException e) {
				System.err.println("Não conectou com " + url);
				System.exit(1);
			} catch (IOException e) {
				System.err.println("Couldn't get I/O for the connection to " + url);
				System.exit(1);
			}
			return response;
		}
	}
}