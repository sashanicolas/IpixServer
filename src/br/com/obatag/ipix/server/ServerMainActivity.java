package br.com.obatag.ipix.server;

import java.io.BufferedOutputStream;
import java.io.File;
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
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.ipixserver.R;

public class ServerMainActivity extends Activity {

	private ServerSocket serverSocket;
	public static final int SERVERPORT = 6000;
	Thread serverThread = null;
	Handler toastHandler;
	Socket socket = null;

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
			try {
				serverSocket = new ServerSocket(SERVERPORT);
			} catch (IOException e) {
				e.printStackTrace();
			}
			while (!Thread.currentThread().isInterrupted()) {
				try {
					socket = serverSocket.accept();

					String msg = "Conectado";
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
		
		int bufferSize = 0;
		if (is != null) {
			FileOutputStream fos = null;
			BufferedOutputStream bos = null;
			try {
				bufferSize = socket.getReceiveBufferSize();
				
				File f = new File("/storage/Pictures/imagem.jpg");
				f.mkdirs(); 
				f.createNewFile();
				
				if (!f.exists()) {
					Log.w("myApp", "Arquivo nao encotrado");
					return;
				}
				
				fos = new FileOutputStream(f);
				bos = new BufferedOutputStream(fos);
				
				byte[] bytes = new byte[bufferSize];
				int count;
				
				while ((count = is.read(bytes)) > 0) {
					bos.write(bytes, 0, count);
				}
				
				bos.flush();
				bos.close();
				is.close();
				socket.close();
				serverSocket.close();

			} catch (IOException ex) {
				
			}
		}
	}

}