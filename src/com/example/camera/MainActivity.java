package com.example.camera;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends Activity implements View.OnClickListener{

	@Override
	protected void onResume() {
		// TODO 自動生成されたメソッド・スタブ
		super.onResume();
		ImageView imageView1 =(ImageView)findViewById(R.id.imageView1);
		imageView1.setOnClickListener(this);

	}

	static final int REQUEST_CODE_CAMERA = 1; /* カメラを判定するコード */
	static final int REQUEST_CODE_GALLERY = 2; /* ギャラリーを判定するコード */

	private Bitmap bm;
	private Uri bitmapUri;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onClick(View v) {
		// TODO 自動生成されたメソッド・スタブ
		switch(v.getId()) {
		case R.id.imageView1:
		// アップロードボタンが押された時
		String[] str_items = {"カメラで撮影", "ギャラリーの選択", "キャンセル"};
			new AlertDialog.Builder(this)
			.setTitle("写真をアップロード")
			.setItems(str_items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO 自動生成されたメソッド・スタブ
					switch(which){
						case 0:
							wakeupCamera(); // カメラ起動
							break;
						case 1:
							wakeupGallery(); // ギャラリー起動
							break;
						default:
							// キャンセルを選んだ場合
							break;
								}
					}
			}).show();
		}

	}
	protected void wakeupCamera(){
		File mediaStorageDir = new File(
			Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES
			), "PictureSaveDir"
		);
		if (! mediaStorageDir.exists() & ! mediaStorageDir.mkdir()){
			return;
		}
		String timeStamp = new SimpleDateFormat("yyyMMddHHmmss").format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".JPG");
		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		bitmapUri = Uri.fromFile(mediaFile);
		i.putExtra(MediaStore.EXTRA_OUTPUT, bitmapUri); // 画像をmediaUriに書き込み
		startActivityForResult(i, REQUEST_CODE_CAMERA);
	}
	protected void wakeupGallery(){
		Intent i = new Intent();
		i.setType("image/*"); // 画像のみが表示されるようにフィルターをかける
		i.setAction(Intent.ACTION_GET_CONTENT); // 出0他を取得するアプリをすべて開く
		startActivityForResult(i, REQUEST_CODE_GALLERY);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == RESULT_OK){
			if (bm != null)
				bm.recycle(); // 直前のBitmapが読み込まれていたら開放する

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4; // 元の1/4サイズでbitmap取得

			switch(requestCode){
				case 1: // カメラの場合
					bm = BitmapFactory.decodeFile(bitmapUri.getPath(), options);
					// 撮影した画像をギャラリーのインデックスに追加されるようにスキャンする。
					// これをやらないと、アプリ起動中に撮った写真が反映されない
					String[] paths = {bitmapUri.getPath()};
					String[] mimeTypes = {"image/*"};
					MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, new OnScanCompletedListener(){
						@Override
						public void onScanCompleted(String path, Uri uri){
						}
					});
					break;
				case 2: // ギャラリーの場合
					try{
						ContentResolver cr = getContentResolver();
						String[] columns = {MediaStore.Images.Media.DATA};
						Cursor c = cr.query(data.getData(), columns, null, null, null);
						c.moveToFirst();
						bitmapUri = Uri.fromFile(new File(c.getString(0)));
						InputStream is = getContentResolver().openInputStream(data.getData());
						bm = BitmapFactory.decodeStream(is, null, options);
						is.close();
					}catch(Exception e){
						e.printStackTrace();
					}
				break;
			}
			ImageView imageView1 =(ImageView)findViewById(R.id.imageView1);
			imageView1.setImageBitmap(bm); // imgView（イメージビュー）を準備しておく
		}
	}

}
