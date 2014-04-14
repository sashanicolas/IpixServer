package br.com.obatag.ipix.server;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ipixserver.R;

public class ServerMainActivity extends Activity {

	private ServerSocket serverSocket;
	public static final int SERVERPORT = 6000;
	Thread serverThread = null;
	Handler toastHandler;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragment_main);
		ImageView i;
		Bitmap bm;
		try {
			bm = getBitmapFromAsset("obatag.jpg");
			i = (ImageView) findViewById(R.id.backGroundImage);
			i.setImageBitmap(bm);
		} catch (IOException e) {
			e.printStackTrace();
		}

		toastHandler = new Handler();

		this.serverThread = new Thread(new ServerThread());
		this.serverThread.start();

	}

	private Bitmap getBitmapFromAsset(String strName) throws IOException {
		AssetManager assetManager = getAssets();
		InputStream istr = assetManager.open(strName);
		Bitmap bitmap = BitmapFactory.decodeStream(istr);
		return bitmap;
	}

	class ServerThread implements Runnable {

		public void run() {
			Socket socket = null;
			try {
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {
				try {
					socket = serverSocket.accept();

					String msg = "Conectou";
					toastHandler.post(new ShowToast(msg));
					
					fileReceived(socket.getInputStream());
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	class ShowToast implements Runnable {
		private String msg;

		public ShowToast(String str) {
			this.msg = str;
		}

		@Override
		public void run() {
			Toast.makeText(ServerMainActivity.this, msg, Toast.LENGTH_LONG)
					.show();
		}
	}

	public void fileReceived(InputStream is) throws FileNotFoundException,
			IOException {

		if (is != null) {
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				fos = new FileOutputStream("/storage/emulated/0/Pictures/imagem.jpg");
				bos = new BufferedOutputStream(fos);
				byte[] aByte = new byte[100000];
				int bytesRead;

				while ((bytesRead = is.read(aByte)) != -1) {
					bos.write(aByte, 0, bytesRead);
				}
				bos.flush();
				bos.close();

			} catch (IOException ex) {
				// Do exception handling
			}
		}
	}

}